package com.mineblock11.foglooksgoodnow.config.adapters;

import com.google.gson.*;

import java.awt.*;
import java.lang.reflect.Type;

public class ColorAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
    @Override
    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            // RRGGBBAA
            String hexColor = json.getAsString().toLowerCase().replace("#", "");
            return fromHex(hexColor);
        } catch (Exception e) {
            throw new JsonParseException("Invalid color: " + json.getAsString());
        }
    }

    public static Color fromHex(String hexColor) {
        try {
            // RRGGBBAA
            hexColor = hexColor.toLowerCase().replace("#", "");

            return new Color(
                    Integer.valueOf(hexColor.substring(0, 2), 16),
                    Integer.valueOf(hexColor.substring(2, 4), 16),
                    Integer.valueOf(hexColor.substring(4, 6), 16),
                    Integer.valueOf(hexColor.substring(6, 8), 16)
            );
        } catch (Exception e) {
            throw new JsonParseException("Invalid color: " + hexColor);
        }
    }

    @Override
    public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(String.format("#%02x%02x%02x%02x", src.getRed(), src.getGreen(), src.getBlue(), src.getAlpha()).toUpperCase());
    }
}
