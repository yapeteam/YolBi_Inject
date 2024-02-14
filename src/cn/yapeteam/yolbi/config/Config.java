package cn.yapeteam.yolbi.config;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class Config {
    private final String name;

    public abstract void save(JsonObject content);

    public abstract void load(JsonObject content);
}
