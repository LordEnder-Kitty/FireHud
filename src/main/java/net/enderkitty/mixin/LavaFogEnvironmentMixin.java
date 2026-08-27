package net.enderkitty.mixin;

import net.enderkitty.FireHud;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LavaFogEnvironment.class)
public class LavaFogEnvironmentMixin {
    @Unique private static final double LIGHT_FOG_MAX_DIST = 100.0;

    /**
     * The old {@code BackgroundRenderer#applyFog} hooks are gone; 26.2 hands each fog source its own
     * {@link net.minecraft.client.renderer.fog.environment.FogEnvironment}, so we let vanilla pick its
     * start/end for the current camera and then replace them at the tail. The render distance fog that
     * {@code FogRenderer} appends afterwards is left alone, it is the same fog you get out of lava.
     */
    @Inject(method = "setupFog", at = @At("TAIL"))
    private void fireHud$lavaFog(FogData data, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
        // Looked up per call rather than cached in a static: this class is initialised from FogRenderer's
        // static block, which is not ordered against the mod's own client entrypoint
        FireHudConfig config = FireHud.getConfig();
        if (config.renderLavaFog == FireHudConfig.LavaFogOptions.VANILLA) return;

        boolean noFog = config.renderLavaFog == FireHudConfig.LavaFogOptions.NO_FOG;
        data.environmentalStart = noFog ? Float.MAX_VALUE : 0.0f;
        data.environmentalEnd = noFog ? Float.MAX_VALUE : lightFogDist(config.lightFogDist);
        data.skyEnd = data.environmentalEnd;
        data.cloudEnd = data.environmentalEnd;
    }

    /**
     * Maps the 0 - 100 slider onto a fog distance exponentially. A lava pool only ever has a few blocks of
     * anything to look at, so past roughly 20 blocks everything in sight already sits under so little haze
     * that raising the distance changes nothing, and a linear slider spent four fifths of its travel there.
     * On this curve 0 is about as thick as vanilla, 50 reaches ten blocks, and the useful range takes up
     * the first two thirds of the slider.
     */
    @Unique
    private static float lightFogDist(int slider) {
        // 100.0 is the slider's own maximum, see FireHudConfig#lightFogDist
        return (float) Math.pow(LIGHT_FOG_MAX_DIST, slider / 100.0);
    }
}
