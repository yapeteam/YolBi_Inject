package cn.yapeteam.yolbi.module.impl.misc.notebot;

import lombok.Getter;

@Getter
public class MusicNote {
    int note;
    int time;
    int inst;

    public MusicNote(int note, int time, int inst) {
        this.note = note;
        this.time = time;
        this.inst = inst;
    }
}
