package cn.yapeteam.injector;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.io.File;

public class Main {
    public static final File YolBi_Dir = new File(System.getProperty("user.home"), ".yolbi");
    public static final String dllName = "injection.dll", agentName = "agent.jar";

    public static void main(String[] args) throws Exception {
        Utils.unzip(Main.class.getResourceAsStream("/injection.zip"), YolBi_Dir);

        UIManager.setLookAndFeel(new FlatDarkLaf());
        new MainFrame().setVisible(true);
    }
}
