package net.enderkitty.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.enderkitty.FireHud;
import net.enderkitty.SoulFireHolder;
import net.enderkitty.SoulFireSprites;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(FlameFeatureRenderer.class)
public abstract class FlameFeatureRendererMixin {

    /**
     * 26.2 hoists the two fire sprites out of the per entity loop in {@code buildGroup}, so they can no longer be
     * swapped with a captured local without recolouring every burning entity at once. Wrapping the per submit
     * {@code prepare} call keeps the swap (and the cancel) scoped to a single entity.
     */
    @WrapOperation(method = "buildGroup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FlameFeatureRenderer;prepare(Lnet/minecraft/client/renderer/feature/FlameFeatureRenderer$Submit;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void fireHud$prepare(FlameFeatureRenderer instance, FlameFeatureRenderer.Submit submit, VertexConsumer buffer,
                                 TextureAtlasSprite fire1, TextureAtlasSprite fire2, Operation<Void> original,
                                 @Local(argsOnly = true) FeatureFrameContext context) {
        FireHudConfig config = FireHud.getConfig();
        Minecraft client = Minecraft.getInstance();

        if (client.player != null && client.player.isOnFire()
                && (!config.renderThirdPersonFire || FireHud.suppressed(client.player, config.renderThirdPersonFireInLava))) {
            return;
        }

        if (config.renderSoulFire && ((SoulFireHolder) submit.entityRenderState()).fireHud$isOnSoulFire()) {
            original.call(instance, submit, buffer,
                    context.atlasManager().get(SoulFireSprites.FIRE_0), context.atlasManager().get(SoulFireSprites.FIRE_1));
        } else {
            original.call(instance, submit, buffer, fire1, fire2);
        }
    }
}
