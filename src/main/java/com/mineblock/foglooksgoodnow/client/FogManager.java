package com.mineblock.foglooksgoodnow.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FogManager {
    @Nullable
    public static FogManager densityManager;
    public static FogManager getDensityManager() {
        return Objects.requireNonNull(densityManager, "Attempted to call getDensityManager before it finished loading!");
    }
    public static Optional<FogManager> getDensityManagerOptional() {
        return Optional.ofNullable(densityManager);
    }

    private final Minecraft mc;
    public InterpolatedValue fogStart;
    public InterpolatedValue fogEnd;
    public InterpolatedValue currentSkyLight;
    public InterpolatedValue currentBlockLight;
    public InterpolatedValue currentLight;
    public InterpolatedValue undergroundness;
    public InterpolatedValue darkness;
    public InterpolatedValue[] caveFogColors;

    private Map<String, BiomeFogDensity> configMap;

    public boolean useCaveFog = true;
    public double caveFogMultiplier = 1.0;

    public FogManager() {
        this.mc = Minecraft.getInstance();
        this.fogStart = new InterpolatedValue(0.0F);
        this.fogEnd = new InterpolatedValue(1.0F);

        this.currentSkyLight = new InterpolatedValue(16.0F);
        this.currentBlockLight = new InterpolatedValue(16.0F);
        this.currentLight = new InterpolatedValue(16.0F);
        this.undergroundness = new InterpolatedValue(0.0F, 0.02f);
        this.darkness = new InterpolatedValue(0.0F, 0.1f);
        this.caveFogColors = new InterpolatedValue[3];
        this.caveFogColors[0] =  new InterpolatedValue(1.0F);
        this.caveFogColors[1] =  new InterpolatedValue(1.0F);
        this.caveFogColors[2] =  new InterpolatedValue(1.0F);

        this.configMap = new HashMap<>();
    }

    public void initializeConfig() {
        FLG.LOGGER.info("Initialized Config Values");
        this.fogStart.setDefaultValue(FLG.CONFIG.defaultFogStart());
        this.fogEnd.setDefaultValue(FLG.CONFIG.defaultFogDensity());
        this.useCaveFog = FLG.CONFIG.useCaveFog();
        this.caveFogMultiplier = FLG.CONFIG.caveFogDensity();
        this.configMap = new HashMap<>();

        Vec3 caveFogColor = Vec3.fromRGB24(FLG.CONFIG.caveFogColor());
        this.caveFogColors[0].setDefaultValue(caveFogColor.x);
        this.caveFogColors[1].setDefaultValue(caveFogColor.y);
        this.caveFogColors[2].setDefaultValue(caveFogColor.z);

        List<Pair<String, BiomeFogDensity>> densityConfigs = FLG.CONFIG.biomeFogs();
        for (Pair<String, BiomeFogDensity> densityConfig : densityConfigs) {
            this.configMap.put(densityConfig.getFirst(), densityConfig.getSecond());
        }
    }

    public void tick() {
        BlockPos pos = this.mc.gameRenderer.getMainCamera().getBlockPosition();
        Biome biome = this.mc.level.getBiome(pos).value();
        ResourceLocation key = this.mc.level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome);
        if (key == null)
            return;

        BiomeFogDensity currentDensity = configMap.get(key.toString());
        boolean isFogDense = this.mc.level.effects().isFoggyAt(pos.getX(), pos.getZ()) || this.mc.gui.getBossOverlay().shouldCreateWorldFog();
        float density = isFogDense? 0.9F : 1.0F;

        float[] darknessAffectedFog;

        if (currentDensity != null) {
            darknessAffectedFog = getDarknessEffectedFog(currentDensity.fogStart(), currentDensity.fogDensity() * density);
            Vec3 caveFogColor = Vec3.fromRGB24(currentDensity.caveFogColor);
            this.caveFogColors[0].interpolate(caveFogColor.x);
            this.caveFogColors[1].interpolate(caveFogColor.y);
            this.caveFogColors[2].interpolate(caveFogColor.z);
        } else {
            darknessAffectedFog = getDarknessEffectedFog(this.fogStart.defaultValue, this.fogEnd.defaultValue * density);
            this.caveFogColors[0].interpolate();
            this.caveFogColors[1].interpolate();
            this.caveFogColors[2].interpolate();
        }

        this.darkness.interpolate(darknessAffectedFog[2]);
        this.fogStart.interpolate(darknessAffectedFog[0]);
        this.fogEnd.interpolate(darknessAffectedFog[1]);

        this.currentSkyLight.interpolate(mc.level.getBrightness(LightLayer.SKY, pos));
        this.currentBlockLight.interpolate(mc.level.getBrightness(LightLayer.BLOCK, pos));
        this.currentLight.interpolate(mc.level.getRawBrightness(pos, 0));

        boolean isAboveGround =  pos.getY() > mc.level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) || pos.getY() > mc.level.getSeaLevel();
        if (isAboveGround) { this.undergroundness.interpolate(0.0F, 0.05f); } else { this.undergroundness.interpolate(1.0F); }
    }

    public float getUndergroundFactor(float partialTick) {
        float y = (float) mc.cameraEntity.getY();
        float yFactor = Mth.clamp(MathUtils.mapRange(mc.level.getSeaLevel() - 32.0F, mc.level.getSeaLevel() + 32.0F, 1, 0, y), 0.0F, 1.0F);
        //FLG.LOGGER.info("" + yFactor);
        return Mth.lerp(yFactor, 1 - this.undergroundness.get(partialTick), this.currentSkyLight.get(partialTick) / 16.0F);
    }

    public static Vec3 getCaveFogColor() {
        Minecraft mc = Minecraft.getInstance();

        InterpolatedValue[] cfc = densityManager.caveFogColors;
        return new Vec3(cfc[0].get(mc.getDeltaFrameTime()), cfc[1].get(mc.getDeltaFrameTime()), cfc[2].get(mc.getDeltaFrameTime()));
    }

    public static boolean shouldRenderCaveFog() {
        return Minecraft.getInstance().level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL && densityManager.useCaveFog && Minecraft.getInstance().gameRenderer.getMainCamera().getFluidInCamera() == FogType.NONE;
    }

    public float[] getDarknessEffectedFog(float fs, float fd) {
        Minecraft mc = Minecraft.getInstance();
        float renderDistance = mc.gameRenderer.getRenderDistance() * 16;

        Entity entity = mc.cameraEntity;
        float fogStart = fs;
        float fogEnd = fd;
        float darknessValue = 0.0F;
        this.fogEnd.interpolationSpeed = 0.05f;
        this.fogStart.interpolationSpeed = 0.05f;
        if (entity instanceof LivingEntity e) {
            if (e.hasEffect(MobEffects.BLINDNESS)) {
                fogStart = (4 * 16) / renderDistance;
                fogEnd = (8 * 16) / renderDistance;
                darknessValue = 1.0F;
            } else if (e.hasEffect(MobEffects.DARKNESS)) {
                MobEffectInstance effect = e.getEffect(MobEffects.DARKNESS);
                if (!effect.getFactorData().isEmpty()) {
                    float factor = this.mc.options.darknessEffectScale().get().floatValue();
                    float intensity = effect.getFactorData().get().getFactor(e, mc.getDeltaFrameTime()) * factor;
                    float darkness = 1 - (calculateDarknessScale(e, effect.getFactorData().get().getFactor(e, mc.getDeltaFrameTime()), mc.getDeltaFrameTime()));
                    FLG.LOGGER.info("" + intensity);
                    fogStart = ((8.0F * 16) / renderDistance) * darkness;
                    fogEnd = ((15.0F * 16) / renderDistance);
                    darknessValue = effect.getFactorData().get().getFactor(e, mc.getDeltaFrameTime());
                }
            }
        }

        return new float[]{fogStart, fogEnd, darknessValue};
    }

    private float calculateDarknessScale(LivingEntity pEntity, float darknessFactor, float partialTicks) {
        float factor = this.mc.options.darknessEffectScale().get().floatValue();
        float f = 0.45F * darknessFactor;
        return Math.max(0.0F, Mth.cos(((float)pEntity.tickCount - partialTicks) * (float)Math.PI * 0.025F) * f) * factor;
    }


    public void close() {}

    public record BiomeFogDensity(float fogStart, float fogDensity, int caveFogColor) {};

    public class InterpolatedValue {
        public float defaultValue;

        private float interpolationSpeed;
        private float previousValue;
        private float currentValue;

        public InterpolatedValue(float defaultValue, float interpolationSpeed) {
            this.defaultValue = defaultValue;
            this.currentValue = defaultValue;
            this.interpolationSpeed = interpolationSpeed;
        }

        public InterpolatedValue(float defaultValue) {
            this(defaultValue, 0.05f);
        }

        public void set(float value) {
            this.previousValue = this.currentValue;
            this.currentValue = value;
        }
        public void set(double value) {
            this.previousValue = this.currentValue;
            this.currentValue = (float) value;
        }

        public void setDefaultValue(float value) {
            this.defaultValue = value;
        }
        public void setDefaultValue(double value) {
            this.defaultValue = (float)value;
        }

        public void interpolate(float value, float interpolationSpeed) {
            this.set(Float.isNaN(value) ? Mth.lerp(interpolationSpeed, currentValue, defaultValue) : Mth.lerp(interpolationSpeed, currentValue, value));
        }
        public void interpolate(double value, float interpolationSpeed) {
            this.set(Double.isNaN(value) ? Mth.lerp(interpolationSpeed, currentValue, defaultValue) : Mth.lerp(interpolationSpeed, currentValue, value));
        }
        public void interpolate(float value) {
            this.interpolate(value, this.interpolationSpeed);
        }
        public void interpolate(double value) {
            this.interpolate(value, this.interpolationSpeed);
        }
        public void interpolate() {
            this.set(Mth.lerp(interpolationSpeed, currentValue, defaultValue));
        }

        public float get(float partialTick) {
            return Mth.lerp(partialTick, previousValue, currentValue);
        }
    }

}