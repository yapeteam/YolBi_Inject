package cn.yapeteam.loader;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JarMapper {
    private static void copyStream(OutputStream os, InputStream is) throws IOException {
        copyStream(os, is, 0);
    }

    private static void copyStream(OutputStream os, InputStream is, int bufsize) throws IOException {
        if (bufsize <= 0) bufsize = 4096;
        int len;
        byte[] bytes = new byte[bufsize];
        while ((len = is.read(bytes)) != -1) {
            os.write(bytes, 0, len);
        }
    }

    private static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1)
            outStream.write(buffer, 0, len);
        outStream.close();
        return outStream.toByteArray();
    }

    public static void dispose(File file, File out) throws Throwable {
        ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(out.toPath()));
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(file.toPath()))) {
            ZipEntry se;
            while ((se = zis.getNextEntry()) != null) {
                if (!se.isDirectory() && se.getName().endsWith(".class")) {
                    byte[] bytes = readStream(zis);
                    bytes = ClassMapper.map(bytes);
                    ZipEntry de = newEntry(se, bytes);
                    zos.putNextEntry(de);
                    zos.write(bytes);
                    zos.closeEntry();
                } else {
                    ZipEntry de = new ZipEntry(se);
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
        ZipEntry de = new ZipEntry(se.getName());
        if (se.getLastModifiedTime() != null)
            de.setLastModifiedTime(se.getLastModifiedTime());
        if (se.getLastAccessTime() != null)
            de.setLastAccessTime(se.getLastAccessTime());
        if (se.getCreationTime() != null)
            de.setCreationTime(se.getCreationTime());
        de.setSize(bytes.length);
        int method = se.getMethod();
        if (!(method != ZipEntry.STORED && method != ZipEntry.DEFLATED))
            de.setMethod(method);
        de.setExtra(se.getExtra());
        de.setComment(se.getComment());
        return de;
    }
}
