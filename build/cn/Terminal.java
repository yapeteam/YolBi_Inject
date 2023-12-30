package cn;

import java.io.*;

public class Terminal {
    private boolean reading = false;

    public void run(String[] cmd, File dir) throws InterruptedException, IOException {
        Process proc = Runtime.getRuntime().exec(cmd, null, dir);
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown") {
            @Override
            public void run() {
                proc.destroy();
            }
        });
        StringBuilder builder = new StringBuilder();
        for (String s : cmd)
            builder.append(s).append(' ');
        System.out.println(">" + builder);
        reading = true;
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), "gbk"));
                String line;
                while (reading) {
                    line = reader.readLine();
                    if (line != null) System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getErrorStream(), "gbk"));
                String line;
                while (reading) {
                    line = reader.readLine();
                    if (line != null) System.err.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        int exitVal = proc.waitFor();
        reading = false;
        System.out.println(exitVal == 0 ? "" : "Failed with code " + exitVal);
    }
}
