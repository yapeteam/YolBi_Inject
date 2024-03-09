package cn.yapeteam.injector;

import com.sun.jna.platform.win32.WinNT;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    private JPanel panel;
    private JButton inject;
    private JComboBox<String> method;
    private JComboBox<String> process;
    private JProgressBar progressBar;

    @Setter
    private volatile float value = 0;
    private ArrayList<Pair<String, Integer>> targets = new ArrayList<>();

    public MainFrame() {
        super("inject Your YolBi Lite");
        float width = 500, height = width * 0.618f;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        int[] size = {(int) (width / 1920 * screenWidth), (int) (height / 1080 * screenHeight)};
        setSize(size[0], size[1]);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        add(panel);
        progressBar.setVisible(false);
        inject.addActionListener(e -> {
            if (!targets.isEmpty() && process.getSelectedIndex() != -1 && method.getSelectedIndex() != -1) {
                int pid = targets.get(process.getSelectedIndex()).b;
                if (method.getSelectedIndex() == 0) {
                    WinNT.HANDLE hdl = Utils.kernel32.OpenProcess(0x1F1FFB, false, pid);
                    Utils.loadLibrary(hdl, new File(Main.YolBi_Dir, Main.dllName).getAbsolutePath());
                } else {
                    try {
                        VirtualMachine virtualMachine = VirtualMachine.attach(String.valueOf(pid));
                        new Thread(() -> {
                            try {
                                virtualMachine.loadAgent(new File(Main.YolBi_Dir, Main.agentName).getAbsolutePath());
                            } catch (Throwable ignored) {
                            }
                        }).start();
                    } catch (AttachNotSupportedException | IOException ignored) {
                        return;
                    }
                }
                process.setVisible(false);
                method.setVisible(false);
                inject.setVisible(false);
                progressBar.setVisible(true);
                //todo progress display
                System.exit(0);
            }
        });

        new Thread(() -> {
            float cache = 0;
            while (true) {
                long time = System.currentTimeMillis();
                while (true) if (time + 20 <= System.currentTimeMillis()) break;
                cache += (value - cache) / 10f;
                progressBar.setValue((int) cache);
            }
        }).start();
        new Thread(() -> {
            while (true) {
                ArrayList<Pair<String, Integer>> minecraftProcesses = Utils.getMinecraftProcesses();
                process.removeAllItems();
                for (Pair<String, Integer> minecraftProcess : minecraftProcesses)
                    process.addItem(minecraftProcess.a);
                targets = minecraftProcesses;
                long time = System.currentTimeMillis();
                while (true) if (System.currentTimeMillis() - time >= 100) break;
            }
        }).start();
    }
}
