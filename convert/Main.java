import cn.yapeteam.loader.Mapper;
import cn.yapeteam.loader.utils.ASMUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;

public class Main {
    @Getter
    @AllArgsConstructor
    public static class Map {
        private final String owner, name, desc, obf, obf_desc, obf_owner;
        private final Mapper.Type type;
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    public static ArrayList<Map> readMappings(InputStream stream) throws Exception {
        ArrayList<Map> mappings = new ArrayList<>();

        String joined = new String(readStream(Objects.requireNonNull(stream)), StandardCharsets.UTF_8);

        for (String line : joined.split("\n")) {
            line = line.replace(String.valueOf((char) 13), "");
            String[] values = line.substring(4).split(" ");
            String[] obf, friendly;
            switch (line.substring(0, 2)) {
                case "CL":
                    mappings.add(new Map(null, values[1], null, values[0], null, null, Mapper.Type.Class));
                    break;
                case "FD":
                    if (values.length == 4) {
                        obf = ASMUtils.split(values[0], "/");
                        friendly = ASMUtils.split(values[2], "/");
                        mappings.add(new Map(
                                values[2].replace("/" + friendly[friendly.length - 1], ""),
                                friendly[friendly.length - 1],
                                values[3],
                                obf[obf.length - 1],
                                null, obf[0],
                                Mapper.Type.Field
                        ));
                    } else if (values.length == 2) {
                        obf = ASMUtils.split(values[0], "/");
                        friendly = ASMUtils.split(values[1], "/");
                        mappings.add(new Map(
                                values[1].replace("/" + friendly[friendly.length - 1], ""),
                                friendly[friendly.length - 1],
                                null,
                                obf[obf.length - 1],
                                null, obf[0],
                                Mapper.Type.Field
                        ));
                    }
                    break;
                case "MD":
                    obf = ASMUtils.split(values[0], "/");
                    friendly = ASMUtils.split(values[2], "/");
                    mappings.add(new Map(
                            values[2].replace("/" + friendly[friendly.length - 1], ""),
                            friendly[friendly.length - 1],
                            values[3],
                            obf[obf.length - 1],
                            values[1], obf[0],
                            Mapper.Type.Method
                    ));
            }
        }
        return mappings;
    }

    //Vanilla: MD: ave/s                                       ()V net/minecraft/client/Minecraft/func_71407_l ()V
    //Forge  : MD: ave/s                                       ()V net/minecraft/client/Minecraft/runTick      ()V
    //Result : MD: net/minecraft/client/Minecraft/func_71407_l ()V net/minecraft/client/Minecraft/runTick      ()V
    public static void main(String[] args) throws Exception {
        val vanilla = readMappings(Main.class.getResourceAsStream("/vanilla.srg"));
        val forge = readMappings(Main.class.getResourceAsStream("/forge.srg"));
        val result = new ArrayList<Map>();
        for (Map va : vanilla) {
            if (va.getType() == Mapper.Type.Class) {
                result.add(va);
                continue;
            }
            Map fo = forge.stream().filter(m -> (m.type == Mapper.Type.Class || m.obf_owner.equals(va.obf_owner)) && m.obf.equals(va.obf) && (m.obf_desc == null || m.obf_desc.equals(va.obf_desc)) && m.type == va.type).findFirst().orElse(null);
            if (fo != null) {
                result.add(new Map(va.owner, va.name, va.desc, fo.name, fo.desc, fo.owner, fo.type));
            } else {
                System.out.println(va.getOwner() + "/" + va.getName());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (Map map : result) {
            switch (map.getType()) {
                case Class:
                    // sb.append("CL: ").append(map.obf).append(' ').append(map.name).append('\n');
                    break;
                case Field:
                    sb.append("FD: ").append(map.obf_owner).append('/').append(map.obf).append(' ').append(map.owner).append('/').append(map.name).append('\n');
                    break;
                case Method:
                    sb.append("MD: ").append(map.obf_owner).append('/').append(map.obf).append(' ').append(map.obf_desc).append(' ').append(map.owner).append('/').append(map.name).append(' ').append(map.desc).append('\n');
                    break;
            }
        }
        Files.write(new File("van.srg").toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
