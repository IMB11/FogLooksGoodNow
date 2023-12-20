package com.mineblock11.foglooksgoodnow.config.adapters;

import com.google.gson.*;
import com.mineblock11.foglooksgoodnow.config.BiomeFogOverride;
import com.mojang.serialization.JsonOps;

import java.lang.reflect.Type;

public class BiomeFogOverrideAdapter implements JsonSerializer<BiomeFogOverride>, JsonDeserializer<BiomeFogOverride> {

    @Override
    public BiomeFogOverride deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return BiomeFogOverride.CODEC.decode(JsonOps.INSTANCE, json)
                .result()
                .orElseThrow()
                .getFirst();
    }

    @Override
    public JsonElement serialize(BiomeFogOverride src, Type typeOfSrc, JsonSerializationContext context) {
        return BiomeFogOverride.CODEC.encodeStart(JsonOps.INSTANCE, src)
                .result()
                .orElseThrow();
    }
}
