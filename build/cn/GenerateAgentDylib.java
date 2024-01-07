package cn;

import cn.yapeteam.yolbi.a_pretoload.utils.ASMUtils;
import lombok.AllArgsConstructor;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class GenerateAgentDylib {
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

    private static ArrayList<String> convertToHex(InputStream is) throws IOException {
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
    static class NameComparator implements Comparator<Group<String, ArrayList<String>, ArrayList<String>>> {
        @Override
        public int compare(GenerateAgentDylib.Group<String, ArrayList<String>, ArrayList<String>> s1, GenerateAgentDylib.Group<String, ArrayList<String>, ArrayList<String>> s2) {
            if (s1.a.contains("Z_Final") && s2.a.contains("a_pretoload")){
                return 1;
            }else if (s2.a.contains("Z_Final") && s1.a.contains("a_pretoload")){
                return -1;
            }else if (s1.a.contains("org.objectweb.asm") && !s2.a.contains("org.objectweb.asm")){
                return -1;
            }else if (s2.a.contains("org.objectweb.asm") && !s1.a.contains("org.objectweb.asm")) {
                return 1;
            }else if (s1.a.contains("a_pretoload") && !s2.a.contains("a_pretoload")){
                return -1;
            }else if (s2.a.contains("a_pretoload") && !s1.a.contains("a_pretoload")) {
                return 1;
            }else if (s1.a.equals("cn.yapeteam.yolbi.YolBi") && !s2.a.equals("cn.yapeteam.yolbi.YolBi")){
                return 1;
            }else if (s2.a.equals("cn.yapeteam.yolbi.YolBi") && !s1.a.equals("cn.yapeteam.yolbi.YolBi")) {
                return -1;
            }else if (s1.a.contains("cn.yapeteam.yolbi.injections") && !s2.a.contains("cn.yapeteam.yolbi.injections")){
                return 1;
            }else if (s2.a.contains("cn.yapeteam.yolbi.injections") && !s1.a.contains("cn.yapeteam.yolbi.injections")) {
                return -1;
            }
            return 0;
        }
    }

    public static void main(String[] args) throws Throwable {
        File classes = new File("classes");
        File packageDir = new File("out/production/YolBi_Inject/cn");
        File resources = new File("resources");
        //classes
        ArrayList<Group<String, ArrayList<String>, ArrayList<String>>> classList = new ArrayList<>();
        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                byte[] bytes = Files.readAllBytes(file);
                String name = ASMUtils.readClassName(bytes).replace('/', '.');
                classList.add(new Group<>(
                        name,
                        ClinitParser.parse(new ByteArrayInputStream(bytes), new ArrayList<>()),
                        convertToHex(new ByteArrayInputStream(bytes))
                ));
                //System.out.println(name);
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(classes.toPath(), visitor);

        Files.walkFileTree(packageDir.toPath(), visitor);
        classList.sort(new NameComparator());
        //classList.sort((m1, m2) -> -Integer.compare(m2.a.charAt(0), m1.a.charAt(0)));
        classList.forEach((it)->{
            System.out.println(it.a);
        });
        System.out.println("结束");
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
        ArrayList<Pair<String, ArrayList<String>>> ResourceList = new ArrayList<>();
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
        String[] sources = new String[]{"jplis", "InstrumentationManager", "utils", "classes", "resources", "agentMain"};
        for (String s : sources)
            buildCppToObj(s, cpp);
        buildDylib(sources, "YolBi_Lite", cpp);
        for (String s : sources)
            new File(cpp, s + ".o").delete();
    }

    private static void buildCppToObj(String name, File dir) throws IOException, InterruptedException {
        new Terminal().run(new String[]{"g++", "-c", name + ".cpp", "-o", name + ".o","-std=c++14"}, dir);
    }

    private static void buildDylib(String[] objs, String name, File dir) throws IOException, InterruptedException {
        ArrayList<String> list = new ArrayList<>();
        list.add("g++");
        list.add("-dynamiclib");
        list.add("-shared");
        for (String obj : objs)
            list.add(obj + ".o");
        list.add("-o");
        list.add(name + ".dylib");
        String[] cmd = list.toArray(new String[0]);
        new Terminal().run(cmd, dir);
    }
}
