package cn.yapeteam.loader.logger;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class Logger {
    @Getter
    private static File log;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public static void init() {
        try {
            log = new File(new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".log");
            boolean ignored = log.createNewFile();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    writeCache();
                } catch (IOException e) {
                    exception(e);
                }
            }));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize logger", e);
        }
    }

    private static final CopyOnWriteArrayList<String> cache = new CopyOnWriteArrayList<>();

    public static void writeCache() throws IOException {
        FileWriter writer = new FileWriter(log, true);
        List<String> temp = (List<String>) cache.clone();
        cache.removeAll(temp);
        for (String s : temp) {
            s = deleteColorCode(s);
            writer.write(s.endsWith("\n") ? s : s + "\n");
        }
        cache.clear();
        writer.close();
    }

    @NotNull
    private static String deleteColorCode(String s) {
        StringBuilder builder = new StringBuilder();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\033') {
                while (i < chars.length - 1 && chars[i] != 'm') i++;
                i++;
            }
            if (i < chars.length)
                builder.append(chars[i]);
        }
        return builder.toString();
    }


    public static void log(String str, String color, Object... o) {
        for (Object o1 : o)
            if (o1 != null)
                str = replaceFirst(str, "{}", o1.toString());
            else str = replaceFirst(str, "{}", "null");
        str = "[" + dateFormat.format(new Date()) + "]" + " " + color + str + ConsoleColors.RESET;
        System.out.println(str);
        cache.add(str);
        if (cache.size() >= 50)
            try {
                writeCache();
            } catch (IOException e) {
                exception(e);
            }
    }

    public static void error(String str, Object... o) {
        log(str, ConsoleColors.RED, o);
    }

    public static void info(String str, Object... o) {
        log(str, ConsoleColors.CYAN, o);
    }

    public static void warn(String str, Object... o) {
        log(str, ConsoleColors.YELLOW, o);
    }

    public static void success(String str, Object... o) {
        log(str, ConsoleColors.GREEN, o);
    }

    public static void exception(Throwable ex) {
        StringBuilder builder = new StringBuilder();
        builder.append(ConsoleColors.YELLOW).append(ex.getClass().getName()).append(' ').append("thrown!").append('\n');
        builder.append("           ").append(ConsoleColors.CYAN).append("Message: ").append(ex.getMessage()).append('\n');
        builder.append("           ").append(ConsoleColors.GREEN).append("StackTrace:").append('\n');
        for (StackTraceElement stackTraceElement : ex.getStackTrace())
            builder.append("           ").append("    ").append(ConsoleColors.RED_BOLD).append(stackTraceElement).append('\n');
        log(builder.toString(), ConsoleColors.NONE);
    }

    public static String replaceFirst(String string, CharSequence target, CharSequence replacement) {
        return Pattern.compile(target.toString(), Pattern.LITERAL).matcher(string).replaceFirst(Matcher.quoteReplacement(replacement.toString()));
    }
}
