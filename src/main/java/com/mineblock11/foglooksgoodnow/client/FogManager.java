package com.mineblock11.foglooksgoodnow.client;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

public class FogManager {
    @Nullable
    public static FogManager densityManager;
    public static FogManager getDensityManager() {
        return Objects.requireNonNull(densityManager, "Attempted to call getDensityManager before it finished loading!");
    }
    public static Optional<FogManager> getDensityManagerOptional() {
        return Optional.ofNullable(densityManager);
    }

    private final MinecraftClient mc;
    public InterpolatedValue fogStart;
    public InterpolatedValue fogEnd;
    public InterpolatedValue currentSkyLight;
    public InterpolatedValue currentBlockLight;
    public InterpolatedValue currentLight;
    public InterpolatedValue fogRain;
    public InterpolatedValue undergroundness;
    public InterpolatedValue darkness;
    public InterpolatedValue[] caveFogColors;

    private Map<String, BiomeFogDensity> configMap;

    public boolean useCaveFog = true;
    public double caveFogMultiplier = 1.0;

    public FogManager() {
        this.mc = MinecraftClient.getInstance();
        this.fogStart = new InterpolatedValue(0.0F);
        this.fogEnd = new InterpolatedValue(1.0F);
        this.fogRain = new InterpolatedValue(0.2F);

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
        this.fogRain.setDefaultValue(FLG.CONFIG.defaultRainFogStart());
        this.fogStart.setDefaultValue(FLG.CONFIG.defaultFogStart());
        this.fogEnd.setDefaultValue(FLG.CONFIG.defaultFogDensity());
        this.useCaveFog = FLG.CONFIG.useCaveFog();
        this.caveFogMultiplier = FLG.CONFIG.caveFogDensity();
        this.configMap = new HashMap<>();

        Vec3d caveFogColor = Vec3d.unpackRgb(FLG.CONFIG.caveFogColor());
        this.caveFogColors[0].setDefaultValue(caveFogColor.x);
        this.caveFogColors[1].setDefaultValue(caveFogColor.y);
        this.caveFogColors[2].setDefaultValue(caveFogColor.z);

        List<Pair<String, BiomeFogDensity>> densityConfigs = FLG.CONFIG.biomeFogs();
        for (Pair<String, BiomeFogDensity> densityConfig : densityConfigs) {
            this.configMap.put(densityConfig.getFirst(), densityConfig.getSecond());
        }
    }

    public void tick() {
        BlockPos pos = this.mc.gameRenderer.getCamera().getBlockPos();
        Biome biome = this.mc.world.getBiome(pos).value();
        Identifier key = this.mc.world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome);
        if (key == null)
            return;

        BiomeFogDensity currentDensity = configMap.get(key.toString());
        boolean isFogDense = this.mc.world.getDimensionEffects().useThickFog(pos.getX(), pos.getZ()) || this.mc.inGameHud.getBossBarHud().shouldThickenFog();
        float density = isFogDense? 0.9F : 1.0F;

        float[] darknessAffectedFog;

        if(mc.world.isRaining()){
            this.fogRain.interpolate(this.fogRain.defaultValue);
        }else{
            this.fogRain.interpolate(1, 0.5f);
        }

        if (currentDensity != null) {
            darknessAffectedFog = getDarknessEffectedFog(currentDensity.fogStart(), currentDensity.fogDensity() * density);
            Vec3d caveFogColor = Vec3d.unpackRgb(currentDensity.caveFogColor);
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

        this.currentSkyLight.interpolate(mc.world.getLightLevel(LightType.SKY, pos));
        this.currentBlockLight.interpolate(mc.world.getLightLevel(LightType.BLOCK, pos));
        this.currentLight.interpolate(mc.world.getBaseLightLevel(pos, 0));

        boolean isAboveGround =  pos.getY() > mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ()) || pos.getY() > mc.world.getSeaLevel();
        if (isAboveGround) { this.undergroundness.interpolate(0.0F, 0.05f); } else { this.undergroundness.interpolate(1.0F); }
    }

    public float getUndergroundFactor(float partialTick) {
        float y = (float) mc.cameraEntity.getY();
        float yFactor = MathHelper.clamp(MathUtils.mapRange(mc.world.getSeaLevel() - 32.0F, mc.world.getSeaLevel() + 32.0F, 1, 0, y), 0.0F, 1.0F);
        //FLG.LOGGER.info("" + yFactor);
        return MathHelper.lerp(yFactor, 1 - this.undergroundness.get(partialTick), this.currentSkyLight.get(partialTick) / 16.0F);
    }

    public static Vec3d getCaveFogColor() {
        MinecraftClient mc = MinecraftClient.getInstance();

        InterpolatedValue[] cfc = densityManager.caveFogColors;
        return new Vec3d(cfc[0].get(mc.getLastFrameDuration()), cfc[1].get(mc.getLastFrameDuration()), cfc[2].get(mc.getLastFrameDuration()));
    }

    public static boolean shouldRenderCaveFog() {
        return MinecraftClient.getInstance().world.getDimensionEffects().getSkyType() == DimensionEffects.SkyType.NORMAL && densityManager.useCaveFog && MinecraftClient.getInstance().gameRenderer.getCamera().getSubmersionType() == CameraSubmersionType.NONE;
    }

    public float[] getDarknessEffectedFog(float fs, float fd) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float renderDistance = mc.gameRenderer.getViewDistance() * 16;

        Entity entity = mc.cameraEntity;
        float fogStart = fs;
        float fogEnd = fd;
        float darknessValue = 0.0F;
        this.fogEnd.interpolationSpeed = 0.05f;
        this.fogStart.interpolationSpeed = 0.05f;
        if (entity instanceof LivingEntity e) {
            if (e.hasStatusEffect(StatusEffects.BLINDNESS)) {
                fogStart = (4 * 16) / renderDistance;
                fogEnd = (8 * 16) / renderDistance;
                darknessValue = 1.0F;
            } else if (e.hasStatusEffect(StatusEffects.DARKNESS)) {
                StatusEffectInstance effect = e.getStatusEffect(StatusEffects.DARKNESS);
                if (!effect.getFactorCalculationData().isEmpty()) {
                    float factor = this.mc.options.getDarknessEffectScale().getValue().floatValue();
                    float intensity = effect.getFactorCalculationData().get().lerp(e, mc.getLastFrameDuration()) * factor;
                    float darkness = 1 - (calculateDarknessScale(e, effect.getFactorCalculationData().get().lerp(e, mc.getLastFrameDuration()), mc.getLastFrameDuration()));
                    FLG.LOGGER.info("" + intensity);
                    fogStart = ((8.0F * 16) / renderDistance) * darkness;
                    fogEnd = ((15.0F * 16) / renderDistance);
                    darknessValue = effect.getFactorCalculationData().get().lerp(e, mc.getLastFrameDuration());
                }
            }
        }

        return new float[]{fogStart, fogEnd, darknessValue};
    }

    private float calculateDarknessScale(LivingEntity pEntity, float darknessFactor, float partialTicks) {
        float factor = this.mc.options.getDarknessEffectScale().getValue().floatValue();
        float f = 0.45F * darknessFactor;
        return Math.max(0.0F, MathHelper.cos(((float)pEntity.age - partialTicks) * (float)Math.PI * 0.025F) * f) * factor;
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
            this.set(Float.isNaN(value) ? MathHelper.lerp(interpolationSpeed, currentValue, defaultValue) : MathHelper.lerp(interpolationSpeed, currentValue, value));
        }
        public void interpolate(double value, float interpolationSpeed) {
            this.set(Double.isNaN(value) ? MathHelper.lerp(interpolationSpeed, currentValue, defaultValue) : MathHelper.lerp(interpolationSpeed, currentValue, value));
        }
        public void interpolate(float value) {
            this.interpolate(value, this.interpolationSpeed);
        }
        public void interpolate(double value) {
            this.interpolate(value, this.interpolationSpeed);
        }
        public void interpolate() {
            this.set(MathHelper.lerp(interpolationSpeed, currentValue, defaultValue));
        }

        public float get(float partialTick) {
            return MathHelper.lerp(partialTick, previousValue, currentValue);
        }
    }

}