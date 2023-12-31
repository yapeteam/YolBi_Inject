package cn.yapeteam.yolbi.command;

import cn.yapeteam.yolbi.command.impl.CommandBind;
import cn.yapeteam.yolbi.command.impl.CommandToggle;
import cn.yapeteam.yolbi.command.impl.CommandValue;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventChat;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommandManager {
    private final ArrayList<AbstractCommand> commands = new ArrayList<>();

    public CommandManager() {
        commands.add(new CommandToggle());
        commands.add(new CommandBind());
        commands.add(new CommandValue());
    }

    @Listener
    private void onChat(EventChat e) {
        String message = e.getMessage();
        if (message.startsWith("$")) {
            e.setCancelled(true);
            message = message.substring(1);
            String[] args = parseMessage(message);
            commands.forEach(c -> c.process(args));
        }
        if (message.startsWith("@"))
            e.setMessage(message.substring(1));
    }

    private static String[] parseMessage(String message) {
        List<String> strings = new ArrayList<>();
        char[] chars = message.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ' ') continue;
            StringBuilder builder = new StringBuilder();
            while (chars[i] != ' ') {
                builder.append(chars[i++]);
                if (!(i < chars.length)) break;
            }
            strings.add(builder.toString());
        }
        String[] args = strings.toArray(new String[0]);
        if (args.length > 1) {
            String[] result = new String[args.length - 1];
            System.arraycopy(args, 1, result, 0, result.length);
            return result;
        }
        return args;
    }
}
