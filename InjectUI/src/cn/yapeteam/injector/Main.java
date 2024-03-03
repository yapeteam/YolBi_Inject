package cn.yapeteam.injector;

import java.io.File;

public class Main {
    public static final File YolBi_Dir = new File(System.getProperty("user.home"), ".yolbi");
    public static final String dllName = "injection.dll", agentName = "agent.jar";

    public static void main(String[] args) throws Exception {
        Utils.unzip(Main.class.getResourceAsStream("injection.zip"), YolBi_Dir);
        new MainFrame().setVisible(true);
    }
}
