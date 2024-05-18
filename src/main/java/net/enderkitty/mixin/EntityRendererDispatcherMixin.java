package net.enderkitty.mixin;

import net.enderkitty.FireHud;
import net.enderkitty.SoulFireAccessor;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public class EntityRendererDispatcherMixin {
    @Unique private static final FireHudConfig config = FireHud.getConfig();
    @Unique private static final SpriteIdentifier SOUL_FIRE_0 = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("block/soul_fire_0"));
    @Unique private static final SpriteIdentifier SOUL_FIRE_1 = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("block/soul_fire_1"));
    
    @Inject(method = "renderFire", at = @At(value = "HEAD"), cancellable = true)
    private void renderThirdPersonFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, Quaternionf rotation, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.player.isOnFire()) {
            if ((!config.renderThirdPersonFireInLava && client.player.isInLava())) ci.cancel();
            if ((!config.renderWithFireResistance && client.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))) ci.cancel();
            if (!config.renderThirdPersonFire) ci.cancel();
        }
    }
    
    @Redirect(method = "renderFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SpriteIdentifier;getSprite()Lnet/minecraft/client/texture/Sprite;", ordinal = 0))
    private Sprite getSprite0(SpriteIdentifier obj, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity) {
        if (config.renderSoulFire && ((SoulFireAccessor)entity).isRenderSoulFire()) {
            return SOUL_FIRE_0.getSprite();
        }
        return obj.getSprite();
    }
    
    @Redirect(method = "renderFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SpriteIdentifier;getSprite()Lnet/minecraft/client/texture/Sprite;", ordinal = 1))
    private Sprite getSprite1(SpriteIdentifier obj, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity) {
        if (config.renderSoulFire && ((SoulFireAccessor)entity).isRenderSoulFire()) {
            return SOUL_FIRE_1.getSprite();
        }
        return obj.getSprite();
    }
}
