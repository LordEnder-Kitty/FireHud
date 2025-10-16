package net.enderkitty.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.enderkitty.FireHud;
import net.enderkitty.SoulFireRenderStateAccessor;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.FireCommandRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(FireCommandRenderer.class)
public abstract class FireCommandRendererMixin {
    @Unique private static final FireHudConfig config = FireHud.getConfig();
    @Unique private static final SpriteIdentifier SOUL_FIRE_0 = TexturedRenderLayers.BLOCK_SPRITE_MAPPER.mapVanilla("soul_fire_0");
    @Unique private static final SpriteIdentifier SOUL_FIRE_1 = TexturedRenderLayers.BLOCK_SPRITE_MAPPER.mapVanilla("soul_fire_1");
    
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/entity/state/EntityRenderState;Lorg/joml/Quaternionf;Lnet/minecraft/client/texture/AtlasManager;)V", at = @At(value = "HEAD"), cancellable = true)
    private void renderThirdPersonFire(MatrixStack.Entry matricesEntry, VertexConsumerProvider vertexConsumers, EntityRenderState renderState, Quaternionf rotation, AtlasManager atlasManager, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        if (client.player != null && client.player.isOnFire()) {
            if ((!config.renderThirdPersonFireInLava && client.player.isInLava())) ci.cancel();
            if ((!config.renderWithFireResistance && client.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))) ci.cancel();
            if (!config.renderThirdPersonFire) ci.cancel();
        }
    }
    
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/entity/state/EntityRenderState;Lorg/joml/Quaternionf;Lnet/minecraft/client/texture/AtlasManager;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack$Entry;scale(FFF)V"))
    private void getSprite0(MatrixStack.Entry matricesEntry, VertexConsumerProvider vertexConsumers, EntityRenderState renderState, Quaternionf rotation, AtlasManager atlasManager, CallbackInfo ci, @Local(ordinal = 0) LocalRef<Sprite> localRef) {
        localRef.set(config.renderSoulFire && ((SoulFireRenderStateAccessor) renderState).fireHud$onSoulFire() ? atlasManager.getSprite(SOUL_FIRE_0) : localRef.get());
    }
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/entity/state/EntityRenderState;Lorg/joml/Quaternionf;Lnet/minecraft/client/texture/AtlasManager;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack$Entry;scale(FFF)V"))
    private void getSprite1(MatrixStack.Entry matricesEntry, VertexConsumerProvider vertexConsumers, EntityRenderState renderState, Quaternionf rotation, AtlasManager atlasManager, CallbackInfo ci, @Local(ordinal = 1) LocalRef<Sprite> localRef) {
        localRef.set(config.renderSoulFire && ((SoulFireRenderStateAccessor) renderState).fireHud$onSoulFire() ? atlasManager.getSprite(SOUL_FIRE_1) : localRef.get());
    }
}
