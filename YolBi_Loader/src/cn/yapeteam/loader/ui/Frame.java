package cn.yapeteam.loader.ui;

import cn.yapeteam.loader.ResourceManager;
import cn.yapeteam.loader.logger.Logger;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

@Getter
public class Frame extends JFrame {
    public Frame() {
        super("YolBi Lite");
    }

    private JProgressBar progressBar = null;
    @Setter
    private volatile float value = 0;

    private final Thread process = new Thread(() -> {
        float cache = 0;
        while (true) {
            long time = System.currentTimeMillis();
            while (true) if (time + 20 <= System.currentTimeMillis()) break;
            cache += (value - cache) / 10f;
            progressBar.setValue((int) cache);
        }
    });

    public void display() {
        float width = 500, height = width * 0.618f;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        int[] size = {(int) (width / 1920 * screenWidth), (int) (height / 1080 * screenHeight)};
        setSize(size[0], size[1]);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel main_panel = new JPanel(new GridBagLayout());

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("YolBi");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        try {
            label.setFont(Font.createFont(Font.PLAIN,
                            Objects.requireNonNull(ResourceManager.resources.getStream("fonts/JelloLight.ttf")))
                    .deriveFont(Font.PLAIN, width / 10)
            );
        } catch (FontFormatException | IOException e) {
            Logger.exception(e);
        }
        panel.add(label, BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(50);
        panel.add(progressBar, BorderLayout.SOUTH);

        main_panel.add(panel);

        add(main_panel);

        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setVisible(true);
        process.start();
    }

    public void close() {
        setVisible(false);
    }
}
