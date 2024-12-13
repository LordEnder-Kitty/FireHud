package net.enderkitty.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.enderkitty.FireHud;
import net.enderkitty.SoulFireRenderStateAccessor;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Unique private static final FireHudConfig config = FireHud.getConfig();
    @Unique private static final SpriteIdentifier SOUL_FIRE_0 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.of("block/soul_fire_0"));
    @Unique private static final SpriteIdentifier SOUL_FIRE_1 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.of("block/soul_fire_1"));
    
    @Inject(method = "renderFire", at = @At(value = "HEAD"), cancellable = true)
    private void renderThirdPersonFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, EntityRenderState renderState, Quaternionf rotation, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.player.isOnFire()) {
            if ((!config.renderThirdPersonFireInLava && client.player.isInLava())) ci.cancel();
            if ((!config.renderWithFireResistance && client.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))) ci.cancel();
            if (!config.renderThirdPersonFire) ci.cancel();
        }
    }
    
    @Inject(method = "renderFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"))
    private void getSprite0(MatrixStack matrices, VertexConsumerProvider vertexConsumers, EntityRenderState renderState, Quaternionf rotation, CallbackInfo ci, @Local(ordinal = 0) LocalRef<Sprite> localRef) {
        localRef.set(config.renderSoulFire && ((SoulFireRenderStateAccessor) renderState).fireHud$onSoulFire() ? SOUL_FIRE_0.getSprite() : localRef.get());
    }
    @Inject(method = "renderFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"))
    private void getSprite1(MatrixStack matrices, VertexConsumerProvider vertexConsumers, EntityRenderState renderState, Quaternionf rotation, CallbackInfo ci, @Local(ordinal = 1) LocalRef<Sprite> localRef) {
        localRef.set(config.renderSoulFire && ((SoulFireRenderStateAccessor) renderState).fireHud$onSoulFire() ? SOUL_FIRE_1.getSprite() : localRef.get());
    }
}
