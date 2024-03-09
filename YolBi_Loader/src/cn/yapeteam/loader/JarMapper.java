package cn.yapeteam.loader;

import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.utils.ASMUtils;
import lombok.val;
import lombok.var;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("SameParameterValue")
public class JarMapper {
    private static void copyStream(OutputStream os, InputStream is) throws IOException {
        copyStream(os, is, 0);
    }

    private static void copyStream(OutputStream os, InputStream is, int bufsize) throws IOException {
        if (bufsize <= 0) bufsize = 4096;
        int len;
        val bytes = new byte[bufsize];
        while ((len = is.read(bytes)) != -1)
            os.write(bytes, 0, len);
    }

    private static byte[] readStream(InputStream inStream) throws Exception {
        val outStream = new ByteArrayOutputStream();
        val buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1)
            outStream.write(buffer, 0, len);
        outStream.close();
        return outStream.toByteArray();
    }

    public static void dispose(File file, File out) throws Throwable {
        SocketSender.send("S1");
        var all = 0;
        try (val zis = new ZipInputStream(Files.newInputStream(file.toPath()))) {
            while (zis.getNextEntry() != null) all++;
        }
        val zos = new ZipOutputStream(Files.newOutputStream(out.toPath()));
        try (val zis = new ZipInputStream(Files.newInputStream(file.toPath()))) {
            ZipEntry se;
            int count = 0;
            while ((se = zis.getNextEntry()) != null) {
                count++;
                int finalCount = count;
                int finalAll = all;
                new Thread(() -> SocketSender.send("P1" + " " + (float) finalCount / finalAll * 100f)).start();
                if (!se.isDirectory() && se.getName().endsWith(".class")) {
                    var bytes = readStream(zis);
                    bytes = ClassMapper.map(bytes);
                    val node = ASMUtils.node(bytes);
                    if (node.visibleAnnotations != null && node.visibleAnnotations.stream().anyMatch(a -> a.desc.contains(ASMUtils.slash(Mixin.class.getName())))) {
                        System.out.println(se.getName());
                        ResourceManager.resources.res.put(se.getName().replace(".class", "").replace('/', '.'), bytes);
                    }
                    val de = newEntry(se, bytes);
                    zos.putNextEntry(de);
                    zos.write(bytes);
                    zos.closeEntry();
                } else {
                    val de = new ZipEntry(se);
                    de.setCompressedSize(-1);
                    zos.putNextEntry(de);
                    copyStream(zos, zis);
                    zos.closeEntry();
                }
            }
        }
        zos.close();
    }

    private static ZipEntry newEntry(ZipEntry se, byte[] bytes) {
        val de = new ZipEntry(se.getName());
        if (se.getLastModifiedTime() != null)
            de.setLastModifiedTime(se.getLastModifiedTime());
        if (se.getLastAccessTime() != null)
            de.setLastAccessTime(se.getLastAccessTime());
        if (se.getCreationTime() != null)
            de.setCreationTime(se.getCreationTime());
        de.setSize(bytes.length);
        val method = se.getMethod();
        if (!(method != ZipEntry.STORED && method != ZipEntry.DEFLATED))
            de.setMethod(method);
        de.setExtra(se.getExtra());
        de.setComment(se.getComment());
        return de;
    }
}
