package cn.yapeteam.yolbi.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Priority {
    HIGHER(0), HIGH(1), NORMAL(2), LOW(3), LOWER(4), MIN(-999), MAX(999);
    private final int level;
}
