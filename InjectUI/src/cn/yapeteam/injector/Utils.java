package cn.yapeteam.injector;

import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {
    public static final Kernel32 kernel32 = Kernel32.INSTANCE;
    public static final User32 user32 = User32.INSTANCE;

    public static String getWindowText(WinDef.HWND hWND) {
        char[] text = new char[1024];
        user32.GetWindowText(hWND, text, text.length);
        StringBuilder sb = new StringBuilder();
        for (char c : text) if (c != 0) sb.append(c);
        return sb.toString();
    }


    public static ArrayList<Pair<String, Integer>> getMinecraftProcesses() {
        ArrayList<Pair<String, Integer>> list = new ArrayList<>();
        WinDef.HWND hWND = user32.FindWindow("LWJGL", null);
        while (hWND != null) {
            if (hWND.getPointer() != Pointer.NULL) {
                IntByReference pid = new IntByReference(-1);
                String title = getWindowText(hWND);
                user32.GetWindowThreadProcessId(hWND, pid);
                list.add(new Pair<>(title, pid.getValue()));
            }
            hWND = user32.FindWindowEx(null, hWND, "LWJGL", null);
        }
        return list;
    }

    public static void loadLibrary(WinNT.HANDLE hdl, String path) {
        Memory pathMemory = new Memory((long) path.length() + 1L);
        pathMemory.setString(0L, path);
        BaseTSD.SIZE_T pathSize = new BaseTSD.SIZE_T(pathMemory.size());
        Pointer pathRemote = kernel32.VirtualAllocEx(hdl, null, pathSize, 12288, 4);
        if (pathRemote == Pointer.NULL)
            throw new IllegalStateException("failed to allocate DLL path.");
        if (!kernel32.WriteProcessMemory(hdl, pathRemote, pathMemory, pathSize.intValue(), null))
            throw new IllegalStateException("could not write DLL path to process.");
        Function loadLibrary = Function.getFunction("kernel32", "LoadLibraryA");
        WinNT.HANDLE hThread = kernel32.CreateRemoteThread(hdl, null, 0, loadLibrary, pathRemote, 0, null);
        if (kernel32.WaitForSingleObject(hThread, -1) != 0)
            throw new IllegalStateException("WaitForSingleObject failed.");
        kernel32.VirtualFreeEx(hdl, pathMemory, new BaseTSD.SIZE_T(0L), 32768);
    }

    public static void unzip(InputStream zipFile, File desDir) throws Exception {
        boolean ignored = desDir.mkdir();
        ZipInputStream zipInputStream = new ZipInputStream(zipFile);
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while (zipEntry != null) {
            String unzipFilePath = desDir.getAbsolutePath() + File.separator + zipEntry.getName();
            if (zipEntry.isDirectory())
                mkdir(new File(unzipFilePath));
            else {
                File file = new File(unzipFilePath);
                mkdir(file.getParentFile());
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get(unzipFilePath)));
                byte[] bytes = new byte[1024];
                int readLen;
                while ((readLen = zipInputStream.read(bytes)) != -1)
                    bufferedOutputStream.write(bytes, 0, readLen);
                bufferedOutputStream.close();
            }
            zipInputStream.closeEntry();
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
    }

    public static void mkdir(File file) {
        if (null == file || file.exists())
            return;
        mkdir(file.getParentFile());
        boolean ignored = file.mkdir();
    }

    public static byte[] readStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try (InputStream input = inStream;
             ByteArrayOutputStream output = outStream) {
            while ((len = input.read(buffer)) != -1)
                output.write(buffer, 0, len);
            return output.toByteArray();
        }
    }
}
