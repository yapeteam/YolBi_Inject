package cn.yapeteam.yolbi.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class AbstractCommand {
    private final String key;

    public abstract void process(String[] args);
}
