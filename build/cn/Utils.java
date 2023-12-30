package cn;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class Utils {
    public interface Connect {
        FileVisitResult run(Path path, boolean isFile, boolean isPre) throws IOException;
    }


    public static void WalkOff(Path path, Connect connect) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                return connect.run(file, true, false);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                connect.run(dir, false, false);
                return connect.run(dir, false, false);
            }
        });
    }

    public static void DeleteDir(Path path) throws IOException {
        WalkOff(path, ((dir, isFile, isPre) -> {
            if (isFile) {
                if (dir.toFile().exists())
                    Files.delete(dir);
            } else if (!isPre) {
                if (dir.toFile().exists())
                    Files.delete(dir);
            }
            return FileVisitResult.CONTINUE;
        }));
    }

    public static String slash(String s) {
        return s.replace('.', '/');
    }

    public static ClassNode node(byte[] bytes) {
        if (bytes != null && bytes.length != 0) {
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            return node;
        }

        return null;
    }

    public static String[] split(String str, String splitter) {
        if (!str.contains(splitter))
            return new String[]{};
        ArrayList<String> result = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder passed = new StringBuilder();
        for (int i = 0; i < str.length() - (splitter.length() - 1); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + splitter.length(); j++)
                sb.append(str.charAt(j));
            if (sb.toString().equals(splitter)) {
                result.add(stringBuilder.toString());
                passed.append(stringBuilder);
                passed.append(splitter);
                stringBuilder = new StringBuilder();
                i += splitter.length();
            }
            if (i < str.length() - 1)
                stringBuilder.append(str.charAt(i));
        }
        String last = str.replace(passed.toString(), "");
        if (!last.isEmpty())
            result.add(last);
        return result.toArray(new String[0]);
    }

    public static byte[] rewriteClass(ClassNode node) {
        ClassWriter writer = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static Object getAnnotationValue(AnnotationNode node, String name) {
        for (int i = 0; i < node.values.size(); i++)
            if (node.values.get(i).equals(name))
                return node.values.get(i + 1);
        return null;
    }
}
