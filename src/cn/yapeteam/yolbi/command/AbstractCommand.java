package cn.yapeteam.yolbi.command;

public abstract class AbstractCommand {
    private final String key;

    public AbstractCommand(String key) {
        this.key = key;
    }

    public abstract void process(String[] args);
}
