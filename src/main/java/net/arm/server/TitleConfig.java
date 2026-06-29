package net.arm.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class TitleConfig {
    private static final Map<String, TitleDataHolder> TITLES_MAP = new HashMap<>();

    public record TitleDataHolder(String text, String color, boolean bold, boolean italic, boolean underlined) {}

    public static void load() {
        TITLES_MAP.clear();
        try {
            InputStream stream = TitleConfig.class.getResourceAsStream("/assets/capesandtitls/gui_data.json");
            if (stream == null) {
                System.out.println("[CapesAndTitles] Ошибка: Файл gui_data.json не найден в ресурсах!");
                return;
            }

            JsonObject json = new Gson().fromJson(new InputStreamReader(stream), JsonObject.class);
            if (json != null && json.has("titles")) {
                JsonArray titlesArray = json.getAsJsonArray("titles");
                for (JsonElement element : titlesArray) {
                    JsonObject titleObj = element.getAsJsonObject();
                    String id = titleObj.get("id").getAsString();
                    String text = titleObj.get("text").getAsString();
                    String color = titleObj.has("color") ? titleObj.get("color").getAsString() : "#FFFFFF";

                    boolean bold = titleObj.has("bold") && titleObj.get("bold").getAsBoolean();
                    boolean italic = titleObj.has("italic") && titleObj.get("italic").getAsBoolean();
                    boolean underlined = titleObj.has("underlined") && titleObj.get("underlined").getAsBoolean();

                    TITLES_MAP.put(id, new TitleDataHolder(text, color, bold, italic, underlined));
                }
            }
            System.out.println("[CapesAndTitles] Успешно загружено титулов из JSON: " + TITLES_MAP.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TitleDataHolder getTitle(String id) {
        return TITLES_MAP.get(id);
    }
}