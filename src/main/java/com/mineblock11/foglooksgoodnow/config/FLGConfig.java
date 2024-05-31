package com.mineblock11.foglooksgoodnow.config;

import com.mineblock11.foglooksgoodnow.FogManager;
import com.mineblock11.foglooksgoodnow.config.adapters.BiomeFogOverrideAdapter;
import com.mineblock11.foglooksgoodnow.config.adapters.ColorAdapter;
import com.mineblock11.mru.config.YACLHelper;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FLGConfig {
    private static final YACLHelper.NamespacedHelper HELPER = new YACLHelper.NamespacedHelper("foglooksgoodnow");
    private static final ConfigClassHandler<FLGConfig> GSON = HELPER.createHandler(FLGConfig.class, new ArrayList<>(List.of(
            builder -> builder.registerTypeAdapter(Identifier.class, new Identifier.Serializer()),
            builder -> builder.registerTypeAdapter(Color.class, new ColorAdapter()),
            builder -> builder.registerTypeAdapter(BiomeFogOverride.class, new BiomeFogOverrideAdapter())
    )));

    @SerialEntry
    public boolean disableAll = false;

    @SerialEntry
    public float fogStart = 1F;
    @SerialEntry
    public float fogEnd = 1.20f;

    @SerialEntry
    public float fogStartRain = 0.6F;
    @SerialEntry
    public float fogEndRain = 0.6f;

    @SerialEntry
    public boolean enableCaveFog = true;
    @SerialEntry
    public float caveFogVisibility = 0.75F;
    @SerialEntry
    public Color caveFogColor = new Color(51, 51, 51, 255);

    @SerialEntry
    public List<BiomeFogOverride> biomeFogOverrides = List.of(
            new BiomeFogOverride(Identifier.of("minecraft", "the_end"), 0.5F, 0.5F, 0.8F, 0.8F, ColorAdapter.fromHex("#333333FF"), 1.8F)
    );

    public static void load() {
        GSON.load();
    }

    public static FLGConfig get() {
        return GSON.instance();
    }

    public static YetAnotherConfigLib getInstance() {
        final ValueFormatter<Float> percentFormatter = (val) -> Text.of(String.format("%.2f", val * 100) + "%");
        final ValueFormatter<Float> multiplierFormatter = (val) -> Text.of(String.format("%.2f", val) + "x");
        return YetAnotherConfigLib.create(GSON,
                (defaults, config, builder) -> {

                    var ref = new Object() {
                        Optional<Option<Float>> fogStartOption = Optional.empty();
                        Optional<Option<Float>> fogEndOption = Optional.empty();
                    };
                    ref.fogStartOption = Optional.of(Option.<Float>createBuilder()
                            .name(Text.translatable("flgn.config.fogStart.name"))
                            .description(OptionDescription.createBuilder()
                                    .text(Text.translatable("flgn.config.fogStart.desc")).build())
                            .binding(defaults.fogStart, () -> config.fogStart, (val) -> config.fogStart = val)
                            .controller(opt -> FloatSliderControllerBuilder.create(opt).formatValue(percentFormatter).step(0.02f).range(0.40f, 2f))
                            .build());

                    ref.fogEndOption = Optional.of(Option.<Float>createBuilder()
                            .name(Text.translatable("flgn.config.fogDensity.name"))
                            .description(OptionDescription.createBuilder()
                                    .text(Text.translatable("flgn.config.fogDensity.desc")).build())
                            .binding(defaults.fogEnd, () -> config.fogEnd, (val) -> config.fogEnd = val)
                            .controller(opt -> FloatSliderControllerBuilder.create(opt).step(0.02f).formatValue(percentFormatter).range(0f, 1.5f))
                            .build());

                    var fogStartRainOption = Option.<Float>createBuilder()
                            .name(Text.translatable("flgn.config.fogStartRain.name"))
                            .description(OptionDescription.createBuilder()
                                    .text(Text.translatable("flgn.config.fogStartRain.desc")).build())
                            .binding(defaults.fogStartRain, () -> config.fogStartRain, (val) -> config.fogStartRain = val)
                            .controller(opt -> FloatFieldControllerBuilder.create(opt).formatValue(multiplierFormatter).range(0f, 1f))
                            .build();

                    var fogDensityRainOption = Option.<Float>createBuilder()
                            .name(Text.translatable("flgn.config.fogDensityRain.name"))
                            .description(OptionDescription.createBuilder()
                                    .text(Text.translatable("flgn.config.fogDensityRain.desc")).build())
                            .binding(defaults.fogEndRain, () -> config.fogEndRain, (val) -> config.fogEndRain = val)
                            .controller(opt -> FloatFieldControllerBuilder.create(opt).range(0f, 2f).formatValue(multiplierFormatter))
                            .build();

                    var caveFogDensityOption = Option.<Float>createBuilder()
                            .name(Text.translatable("flgn.config.caveFogDensity.name"))
                            .description(OptionDescription.createBuilder()
                                    .text(Text.translatable("flgn.config.caveFogDensity.desc")).build())
                            .binding(defaults.caveFogVisibility, () -> config.caveFogVisibility, (val) -> config.caveFogVisibility = val)
                            .controller(opt -> FloatSliderControllerBuilder.create(opt).step(0.05f).formatValue(percentFormatter).range(0f, 2f))
                            .build();

                    var caveFogColorOption = Option.<Color>createBuilder()
                            .name(Text.translatable("flgn.config.caveFogColor.name"))
                            .description(OptionDescription.createBuilder()
                                    .text(Text.translatable("flgn.config.caveFogColor.desc")).build())
                            .binding(defaults.caveFogColor, () -> config.caveFogColor, (val) -> config.caveFogColor = val)
                            .controller(ColorControllerBuilder::create)
                            .build();

                    var enableCaveFogOption = Option.<Boolean>createBuilder()
                            .name(Text.translatable("flgn.config.enableCaveFog.name"))
                            .description(OptionDescription.createBuilder()
                                    .text(Text.translatable("flgn.config.enableCaveFog.desc")).build())
                            .binding(defaults.enableCaveFog, () -> config.enableCaveFog, (val) -> config.enableCaveFog = val)
                            .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true).trueFalseFormatter())
                            .listener((opt, val) -> {
                                caveFogColorOption.setAvailable(val);
                                caveFogDensityOption.setAvailable(val);
                            })
                            .build();

                    var disableAllOption = Option.<Boolean>createBuilder()
                            .name(Text.translatable("flgn.config.disableAll.name"))
                            .description(OptionDescription.createBuilder().text(Text.translatable("flgn.config.disableAll.desc")).build())
                            .binding(defaults.disableAll, () -> config.disableAll, (val) -> config.disableAll = val)
                            .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true).trueFalseFormatter())
                            .listener((opt, val) -> {
                                ref.fogStartOption.get().setAvailable(!val);
                                ref.fogEndOption.get().setAvailable(!val);
                                fogStartRainOption.setAvailable(!val);
                                fogDensityRainOption.setAvailable(!val);

                                enableCaveFogOption.setAvailable(!val);
                                caveFogDensityOption.setAvailable(!val);
                                caveFogColorOption.setAvailable(!val);
                            }).build();

                    return builder
                            .category(ConfigCategory.createBuilder()
                                    .name(Text.translatable("flgn.config.surface"))
                                    .option(ref.fogStartOption.get())
                                    .option(ref.fogEndOption.get())
                                    .option(fogStartRainOption)
                                    .option(fogDensityRainOption)
                                    .build())
                            .category(ConfigCategory.createBuilder()
                                    .name(Text.translatable("flgn.config.cave"))
                                    .option(enableCaveFogOption)
                                    .option(caveFogDensityOption)
                                    .option(caveFogColorOption)
                                    .build())
                            .category(ConfigCategory.createBuilder()
                                    .name(Text.translatable("flgn.config.global"))
                                    .option(disableAllOption)
                                    .build())
                            .save(() -> {
                                GSON.save();
                                FogManager.getInstanceOptional().ifPresent(FogManager::setToConfig);
                            })
                            .title(Text.of("Fog Looks Good Now"));
                }
        );
    }
}
