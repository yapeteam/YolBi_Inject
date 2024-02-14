package cn.yapeteam.yolbi.config;

import cn.yapeteam.yolbi.YolBi;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class ConfigManager {
    public static final File file = new File(YolBi.YOLBI_DIR, "config.json");
    private final ArrayList<Config> configs = new ArrayList<>();

    public void registerConfig(Config config) {
        configs.add(config);
    }

    public void save() throws IOException {
        JsonObject jsonObject = new JsonObject();
        for (Config config : configs) {
            JsonObject e = new JsonObject();
            config.save(e);
            jsonObject.add(config.getName(), e);
        }
        Files.write(file.toPath(), jsonObject.toString().getBytes(StandardCharsets.UTF_8));
    }

    public void load() throws IOException {
        if (file.exists()) {
            JsonObject jsonObject = new JsonParser().parse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
            for (Config config : configs) {
                JsonObject e = jsonObject.getAsJsonObject(config.getName());
                if (e != null) config.load(e);
            }
        } else save();
    }
}
