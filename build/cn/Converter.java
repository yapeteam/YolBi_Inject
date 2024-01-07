package cn;

import cn.yapeteam.yolbi.a_pretoload.utils.ASMUtils;
import lombok.AllArgsConstructor;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Converter {
    @AllArgsConstructor
    static class Group<A, B, C> {
        public A a;
        public B b;
        public C c;
    }

    @AllArgsConstructor
    static class Pair<A, B> {
        public A a;
        public B b;
    }

    public static ArrayList<String> convertToHex(InputStream is) throws IOException {
        ArrayList<String> list = new ArrayList<>();
        int value;
        StringBuilder string = new StringBuilder();
        while ((value = is.read()) != -1) {
            string.append(String.format("%02x", value));
            if (string.toString().length() == 4096) {
                list.add(string.toString());
                string = new StringBuilder();
            }
        }
        list.add(string.toString());
        is.close();
        return list;
    }

    public static void main(String[] args) throws Throwable {
        File classes = new File("classes");
        File packageDir = new File("out/production/YolBi_Inject/cn");
        File resources = new File("resources");
        ArrayList<Group<String, ArrayList<String>, ArrayList<String>>> classList = new ArrayList<>();
        ArrayList<Pair<String, ArrayList<String>>> ResourceList = new ArrayList<>();
        //classes
        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                byte[] bytes = Files.readAllBytes(file);
                String name = ASMUtils.readClassName(bytes).replace('/', '.');
                ArrayList<String> hex = convertToHex(new ByteArrayInputStream(bytes));
                classList.add(new Group<>(
                        name,
                        ClinitParser.parse(new ByteArrayInputStream(bytes), new ArrayList<>()),
                        hex
                ));
                System.out.println(name);
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(classes.toPath(), visitor);
        Files.walkFileTree(packageDir.toPath(), visitor);
        File source = new File("injector/classesSrc.cpp");
        File classesCpp = new File("injector/classes.cpp");
        classesCpp.delete();
        StringBuilder finalStr = new StringBuilder();
        try (FileInputStream fs = new FileInputStream(source)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(fs));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("$SIZE"))
                    finalStr.append("map classList[").append(classList.size()).append("];").append("\n");
                else if (line.contains("$DATA"))
                    for (int i = 0; i < classList.size(); i++) {
                        finalStr.append("    ").append("classList").append("[").append(i).append("]").append(".name").append(" = ").append("AY_OBFUSCATE(\"").append(classList.get(i).a).append("\")").append(";").append("\n");
                        for (String s : classList.get(i).b) {
                            finalStr.append("    ").append("classList").append("[").append(i).append("]").append(".preToLoad").append(".push_back(").append("string(AY_OBFUSCATE(\"").append(s).append("\")));").append("\n");
                        }
                        finalStr.append("    ").append("classList").append("[").append(i).append("]").append(".hex").append(" = ").append("\"\";").append("\n");
                        for (String s : classList.get(i).c) {
                            finalStr.append("    ").append("classList").append("[").append(i).append("]").append(".hex").append(".append(").append(/*new Random((long) (Math.random() * 114514)).nextInt(5) % 5 == 0*/classList.get(i).a.startsWith("cn") ? "AY_OBFUSCATE" : "").append("(\"").append(s).append("\"));").append("\n");
                        }
                    }
                else finalStr.append(line).append("\n");
            }
        }
        Files.write(classesCpp.toPath(), Collections.singleton(finalStr));
        //resources
        Files.walkFileTree(resources.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Pair<String, ArrayList<String>> aResource = new Pair<>(
                        file.toFile().getName(),
                        convertToHex(Files.newInputStream(file.toFile().toPath()))
                );
                ResourceList.add(aResource);
                System.out.println(file.toFile().getName());
                return FileVisitResult.CONTINUE;
            }
        });
        File resourcesCpp = new File("injector/resources.cpp");
        resourcesCpp.delete();
        source = new File("injector/resourcesSrc.cpp");
        finalStr = new StringBuilder();
        try (FileInputStream fs = new FileInputStream(source)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(fs));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("$SIZE"))
                    finalStr.append("res resourceList[").append(ResourceList.size()).append("];").append("\n");
                else if (line.contains("$DATA"))
                    for (int i = 0; i < ResourceList.size(); i++) {
                        finalStr.append("    ").append("resourceList").append("[").append(i).append("]").append(".name").append(" = ").append("AY_OBFUSCATE(\"").append(ResourceList.get(i).a).append("\")").append(";").append("\n");
                        finalStr.append("    ").append("resourceList").append("[").append(i).append("]").append(".hex").append(" = ").append("\"\";").append("\n");
                        for (String s : ResourceList.get(i).b) {
                            finalStr.append("    ").append("resourceList").append("[").append(i).append("]").append(".hex").append(".append(").append(new Random((long) (Math.random() * 114514)).nextInt(5) % 5 == 0 ? "/*AY_OBFUSCATE*/" : "").append("(\"").append(s).append("\"));").append("\n");
                        }
                    }
                else finalStr.append(line).append("\n");
            }
        }
        Files.write(resourcesCpp.toPath(), Collections.singleton(finalStr));
        //BUILD DLL
        File cpp = new File("injector");
        String[] sources = new String[]{"jplis", "InstrumentationManager", "utils", "classes", "resources", "main"};
        for (String s : sources)
            buildCppToObj(s, cpp);
        buildDLL(sources, "YolBi_Lite", cpp);
        for (String s : sources)
            new File(cpp, s + ".o").delete();
    }

    private static void buildCppToObj(String name, File dir) throws IOException, InterruptedException {
        new Terminal().run(new String[]{"g++", "-c", name + ".cpp", "-o", name + ".o"}, dir);
    }

    private static void buildDLL(String[] objs, String name, File dir) throws IOException, InterruptedException {
        ArrayList<String> list = new ArrayList<>();
        list.add("g++");
        list.add("-shared");
        for (String obj : objs)
            list.add(obj + ".o");
        list.add("-o");
        list.add(name + ".dll");
        String[] cmd = list.toArray(new String[0]);
        new Terminal().run(cmd, dir);
    }
}
