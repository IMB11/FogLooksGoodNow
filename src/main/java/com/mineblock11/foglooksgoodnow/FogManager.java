package com.mineblock11.foglooksgoodnow;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.mineblock11.foglooksgoodnow.config.BiomeFogOverride;
import com.mineblock11.foglooksgoodnow.config.FLGConfig;
import com.mineblock11.foglooksgoodnow.utils.MathUtils;
import net.minecraft.util.Pair;
import org.antlr.v4.misc.Graph;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
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
    private static FogManager INSTANCE;
    private final InterpolatedValue fogEndRain;

    public static FogManager instance() {
        if(INSTANCE == null) {
            INSTANCE = new FogManager();
        }

        return INSTANCE;
    }

    public static Optional<FogManager> getInstanceOptional() {
        return Optional.ofNullable(INSTANCE);
    }

    private final MinecraftClient client;
    public final InterpolatedValue fogStart;
    public final InterpolatedValue fogEnd;
    public final InterpolatedValue currentSkyLight;
    public final InterpolatedValue currentBlockLight;
    public final InterpolatedValue currentLight;
    public final InterpolatedValue fogStartRain;
    public final InterpolatedValue undergroundness;
    public final InterpolatedValue darkness;
    public final InterpolatedValue[] caveFogColors;

    private Map<String, BiomeFogOverride> overrideMap;

    public boolean useCaveFog = true;
    public double caveFogMultiplier = 1.0;

    public FogManager() {
        this.client = MinecraftClient.getInstance();

        FLGConfig config = FLGConfig.get();

        this.fogStart = new InterpolatedValue(1 - config.fogStart);
        this.fogStartRain = new InterpolatedValue(1 - config.fogStartRain);

        this.fogEnd = new InterpolatedValue(1f - config.fogVisibility);
        this.fogEndRain = new InterpolatedValue(1f - config.fogVisibilityRain);

        this.currentSkyLight = new InterpolatedValue(16.0F);
        this.currentBlockLight = new InterpolatedValue(16.0F);
        this.currentLight = new InterpolatedValue(16.0F);
        this.undergroundness = new InterpolatedValue(0.0F, 0.02f);

        this.darkness = new InterpolatedValue(0.0F, 0.1f);

        this.caveFogColors = new InterpolatedValue[3];
        this.caveFogColors[0] = new InterpolatedValue(config.caveFogColor.getRed() / 255f);
        this.caveFogColors[1] = new InterpolatedValue(config.caveFogColor.getRed() / 255f);
        this.caveFogColors[2] = new InterpolatedValue(config.caveFogColor.getRed() / 255f);

        this.overrideMap = new HashMap<>();
        for (BiomeFogOverride override : config.biomeFogOverrides) {
            this.overrideMap.put(override.biome().toString(), override);
        }
    }

    public void setToConfig() {
        FLGConfig config = FLGConfig.get();

        this.fogStart.setDefaultValue(1 - config.fogStart);
        this.fogStart.set(1 - config.fogStart);

        this.fogStartRain.setDefaultValue(1 - config.fogStartRain);
        this.fogStartRain.set(1 - config.fogStartRain);

        this.fogEnd.setDefaultValue(2f - config.fogVisibility);
        this.fogEnd.set(2f - config.fogVisibility);

        this.fogEndRain.setDefaultValue(1f + config.fogVisibilityRain);
        this.fogEndRain.set(1f + config.fogVisibilityRain);

        this.useCaveFog = config.enableCaveFog;
        this.caveFogMultiplier = config.caveFogVisibility;

        this.caveFogColors[0].setDefaultValue(config.caveFogColor.getRed() / 255f);
        this.caveFogColors[0].set(config.caveFogColor.getRed() / 255f);

        this.caveFogColors[1].setDefaultValue(config.caveFogColor.getGreen() / 255f);
        this.caveFogColors[1].set(config.caveFogColor.getGreen() / 255f);

        this.caveFogColors[2].setDefaultValue(config.caveFogColor.getBlue() / 255f);
        this.caveFogColors[2].set(config.caveFogColor.getBlue() / 255f);

        this.overrideMap.clear();
        for (BiomeFogOverride override : config.biomeFogOverrides) {
            this.overrideMap.put(override.biome().toString(), override);
        }
    }

    public void tick() {
        BlockPos pos = this.client.gameRenderer.getCamera().getBlockPos();
        Biome biome = this.client.world.getBiome(pos).value();
        Identifier key = this.client.world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome);

        if (key == null)
            return;

        BiomeFogOverride currentDensity = overrideMap.get(key.toString());
        boolean isFogDense = this.client.world.getDimensionEffects().useThickFog(pos.getX(), pos.getZ()) || this.client.inGameHud.getBossBarHud().shouldThickenFog();
        float density = isFogDense? 0.9F : 1.0F;

        float[] darknessAffectedFog;

        if(client.world.isRaining() && !shouldRenderCaveFog()){
            this.fogStartRain.interpolate(this.fogStartRain.defaultValue);
        }else{
            this.fogStartRain.interpolate(1, 0.05f);
        }

        if (currentDensity != null) {
            darknessAffectedFog = getDarknessEffectedFog(currentDensity.fogStart(), currentDensity.fogDensity() * density);
            Vec3d caveFogColor = Vec3d.unpackRgb(currentDensity.caveFogColor().getRGB());
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

        this.currentSkyLight.interpolate(client.world.getLightLevel(LightType.SKY, pos));
        this.currentBlockLight.interpolate(client.world.getLightLevel(LightType.BLOCK, pos));
        this.currentLight.interpolate(client.world.getBaseLightLevel(pos, 0));

        boolean isAboveGround =  pos.getY() > client.world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ()) || pos.getY() > client.world.getSeaLevel();
        if (isAboveGround) { this.undergroundness.interpolate(0.0F, 0.05f); } else { this.undergroundness.interpolate(1.0F); }
    }

    public float getUndergroundFactor(float partialTick) {
        float y = (float) client.cameraEntity.getY();
        float yFactor = MathHelper.clamp(MathUtils.mapRange(client.world.getSeaLevel() - 32.0F, client.world.getSeaLevel() + 32.0F, 1, 0, y), 0.0F, 1.0F);
        //FLG.LOGGER.info("" + yFactor);
        return MathHelper.lerp(yFactor, 1 - this.undergroundness.get(partialTick), this.currentSkyLight.get(partialTick) / 16.0F);
    }

    public static Vec3d getCaveFogColor() {
        MinecraftClient mc = MinecraftClient.getInstance();

        InterpolatedValue[] cfc = INSTANCE.caveFogColors;
        return new Vec3d(cfc[0].get(mc.getLastFrameDuration()), cfc[1].get(mc.getLastFrameDuration()), cfc[2].get(mc.getLastFrameDuration()));
    }

    private static final Cache<Pair<Integer, Integer>, Boolean> EXPOSED_CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(Duration.ofSeconds(120))
            .build();
    private static boolean isPlayerExposed() {
        MinecraftClient client = MinecraftClient.getInstance();

        if(client.world == null || client.player == null) {
            return false;
        }

        int minY = client.player.getBlockY();
        int x = client.player.getBlockX();
        int z = client.player.getBlockZ();

        // Check Cache
        Boolean cached = EXPOSED_CACHE.getIfPresent(new Pair<>(x, z));
        if(cached != null) {
            return cached;
        }

        // Check if all blocks above the player are air.
        for(int y = minY; y < 256; y++) {
            if(!client.world.getBlockState(new BlockPos(x, y, z)).isAir()) {
                // Cache result.
                EXPOSED_CACHE.put(new Pair<>(x, z), false);
                return false;
            }
        }

        // Cache result.
        EXPOSED_CACHE.put(new Pair<>(x, z), true);
        return true;
    }

    public static boolean shouldRenderCaveFog() {
        boolean isNormalSky = MinecraftClient.getInstance().world.getDimensionEffects().getSkyType() == DimensionEffects.SkyType.NORMAL;
        boolean isSubmerged = MinecraftClient.getInstance().gameRenderer.getCamera().getSubmersionType() != CameraSubmersionType.NONE;

        return isNormalSky && INSTANCE.useCaveFog && isSubmerged && isPlayerExposed();
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
                    float factor = this.client.options.getDarknessEffectScale().getValue().floatValue();
                    float intensity = effect.getFactorCalculationData().get().lerp(e, mc.getLastFrameDuration()) * factor;
                    float darkness = 1 - (calculateDarknessScale(e, effect.getFactorCalculationData().get().lerp(e, mc.getLastFrameDuration()), mc.getLastFrameDuration()));
                    FogLooksGoodNow.LOGGER.info("" + intensity);
                    fogStart = ((8.0F * 16) / renderDistance) * darkness;
                    fogEnd = ((15.0F * 16) / renderDistance);
                    darknessValue = effect.getFactorCalculationData().get().lerp(e, mc.getLastFrameDuration());
                }
            }
        }

        return new float[]{fogStart, fogEnd, darknessValue};
    }

    private float calculateDarknessScale(LivingEntity pEntity, float darknessFactor, float partialTicks) {
        float factor = this.client.options.getDarknessEffectScale().getValue().floatValue();
        float f = 0.45F * darknessFactor;
        return Math.max(0.0F, MathHelper.cos(((float)pEntity.age - partialTicks) * (float)Math.PI * 0.025F) * f) * factor;
    }


    public void close() {
        if(INSTANCE == this) {
            INSTANCE = null;
        }
    }

    public static class InterpolatedValue {
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