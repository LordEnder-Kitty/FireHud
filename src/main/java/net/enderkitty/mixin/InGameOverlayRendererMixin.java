package net.enderkitty.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.enderkitty.FireHud;
import net.enderkitty.SoulFireEntityAccessor;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
    @Shadow @Final private VertexConsumerProvider vertexConsumers;
    @Unique private static final FireHudConfig config = FireHud.getConfig();
    @Unique private static final SpriteIdentifier SOUL_FIRE_1 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.of("block/soul_fire_1"));
    
    @Inject(method = "renderOverlays", at = @At("TAIL"))
    private void renderOverlays(boolean sleeping, float tickProgress, CallbackInfo ci, @Local MatrixStack matrices) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && !player.isSpectator() && player.isOnFire() && config.sideFire &&
                !(!config.renderFireInLava && player.isInLava()) && !(!config.renderWithFireResistance && player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))) {
            renderSideFireOverlay(matrices, this.vertexConsumers);
        }
    }
    
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderVanillaHud(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (!config.renderVanillaHud) ci.cancel();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.player.isOnFire()) {
            if ((!config.renderFireInLava && client.player.isInLava())) ci.cancel();
            if ((!config.renderWithFireResistance && client.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))) ci.cancel();
        }
    }
    
    @ModifyArg(method = "renderFireOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;color(FFFF)Lnet/minecraft/client/render/VertexConsumer;"), index = 3)
    private static float fireOpacity(float red) {
        return config.fireOpacity;
    }
    
    @ModifyArg(method = "renderFireOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"), index = 1)
    private static float firePos(float y) {
        return -1.0f + config.firePos;
    }
    
    @Redirect(method = "renderFireOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SpriteIdentifier;getSprite()Lnet/minecraft/client/texture/Sprite;"))
    private static Sprite getSprite(SpriteIdentifier instance) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (config.renderSoulFire && client.player != null && ((SoulFireEntityAccessor) client.player).fireHud$isOnSoulFire()) return SOUL_FIRE_1.getSprite();
        return instance.getSprite();
    }
    
    @Unique
    private static void renderSideFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        MinecraftClient client = MinecraftClient.getInstance();
        Sprite sprite = (config.renderSoulFire && client.player != null && ((SoulFireEntityAccessor) client.player).fireHud$isOnSoulFire() ? SOUL_FIRE_1.getSprite() : ModelBaker.FIRE_1.getSprite());
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getFireScreenEffect(sprite.getAtlasId()));
        float f = sprite.getMinU();
        float g = sprite.getMaxU();
        float h = (f + g) / 2.0F;
        float i = sprite.getMinV();
        float j = sprite.getMaxV();
        float k = (i + j) / 2.0F;
        float l = sprite.getUvScaleDelta();
        float m = MathHelper.lerp(l, f, h);
        float n = MathHelper.lerp(l, g, h);
        float o = MathHelper.lerp(l, i, k);
        float p = MathHelper.lerp(l, j, k);
        
        for (int r = 0; r < 2; r++) {
            matrices.push();
            matrices.translate(-(r * 2 - 1) * 0.24F, -1.0f + config.firePos, -0.2F); // -0.3f, 0.0f
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(0.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((r * 2 - 1) * 70.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((r == 1 ? -10.0f : 10.0f)));
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            vertexConsumer.vertex(matrix4f, -0.5F, -0.5F, -0.5F).texture(n, p).color(1.0F, 1.0F, 1.0F, 0.9F);
            vertexConsumer.vertex(matrix4f, 0.5F, -0.5F, -0.5F).texture(m, p).color(1.0F, 1.0F, 1.0F, 0.9F);
            vertexConsumer.vertex(matrix4f, 0.5F, 0.5F, -0.5F).texture(m, o).color(1.0F, 1.0F, 1.0F, 0.9F);
            vertexConsumer.vertex(matrix4f, -0.5F, 0.5F, -0.5F).texture(n, o).color(1.0F, 1.0F, 1.0F, 0.9F);
            matrices.pop();
        }
    }
}
