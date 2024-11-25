package net.enderkitty.mixin;

import net.enderkitty.FireHud;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.entity.Entity;
import org.joml.Vector4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    @Unique private static final FireHudConfig config = FireHud.getConfig();
    
    @Redirect(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSpectator()Z"))
    private static boolean applyFog(Entity entity) {
        return config.renderLavaFog == FireHudConfig.LavaFogOptions.LIGHT_FOG;
    }
    
    @Redirect(method = "applyFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/BackgroundRenderer$FogData;fogEnd:F", ordinal = 0, opcode = Opcodes.PUTFIELD))
    private static void viewDistFogEndFix(BackgroundRenderer.FogData fogData, float value) {
        if (config.renderLavaFog == FireHudConfig.LavaFogOptions.LIGHT_FOG) {
            fogData.fogEnd = config.lightFogDist;
        }
    }
    @Redirect(method = "applyFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/BackgroundRenderer$FogData;fogStart:F", ordinal = 0, opcode = Opcodes.PUTFIELD))
    private static void viewDistFogStartFix(BackgroundRenderer.FogData fogData, float value) {
        if (config.renderLavaFog == FireHudConfig.LavaFogOptions.LIGHT_FOG) {
            fogData.fogStart = 0;
        }
    }
    
    
    @Inject(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSpectator()Z", ordinal = 0), cancellable = true)
    private static void applyFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
        if (FireHud.getConfig().renderLavaFog == FireHudConfig.LavaFogOptions.NO_FOG) {
            cir.setReturnValue(Fog.DUMMY);
        }
    }
}
