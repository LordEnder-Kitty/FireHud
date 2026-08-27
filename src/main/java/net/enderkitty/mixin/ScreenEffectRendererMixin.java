package net.enderkitty.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.enderkitty.FireHud;
import net.enderkitty.SoulFireHolder;
import net.enderkitty.SoulFireSprites;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
    @Shadow @Final private SpriteGetter sprites;
    // Yaw and roll of the "side fire" pair, in radians; 0.17453292f is vanilla's own 10 degree yaw
    @Unique private static final float SIDE_YAW = (float) Math.toRadians(70.0);
    @Unique private static final float SIDE_ROLL = (float) Math.toRadians(10.0);

    /**
     * 26.2 builds the whole first person fire overlay inside one {@code submitCustomGeometry} lambda, with the
     * colour baked into a packed constant and the offset applied to a bare {@link Matrix4f}, so there is nothing
     * left to {@code @ModifyArg}. We take the call over instead and emit the quads ourselves, which is also where
     * the extra "side fire" pair now lives.
     */
    @WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;submitFire(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void fireHud$submitFire(PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite sprite, Operation<Void> original) {
        FireHudConfig config = FireHud.getConfig();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || FireHud.suppressed(player, config.renderFireInLava)) return;

        TextureAtlasSprite fireSprite = config.renderSoulFire && ((SoulFireHolder) player).fireHud$isOnSoulFire()
                ? this.sprites.get(SoulFireSprites.FIRE_1)
                : sprite;

        if (config.renderVanillaHud) fireHud$submitCustomGeometry(poseStack, collector, fireSprite, false);
        if (config.sideFire) fireHud$submitCustomGeometry(poseStack, collector, fireSprite, true);
    }

    @Unique
    private static void fireHud$submitCustomGeometry(PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite fireSprite, boolean sideFire) {
        FireHudConfig config = FireHud.getConfig();

        float yPos = -1.0f + config.firePos;
        int fireColor = ARGB.white(config.fireOpacity);

        collector.submitCustomGeometry(poseStack, RenderTypes.fireScreenEffect(fireSprite.atlasLocation()), (basePose, builder) -> {
            Matrix4f pose = new Matrix4f();

            for (int r = 0; r < 2; r++) {
                float side = r * 2 - 1;
                pose.set(basePose.pose());
                pose.translate(-side * 0.24f, yPos, sideFire ? -0.2f : 0.0f);
                pose.rotateY(side * (sideFire ? SIDE_YAW : 0.17453292f));
                if (sideFire) pose.rotateZ(-side * SIDE_ROLL);
                fireHud$buildFireQuad(builder, pose, fireSprite, fireColor);
            }
        });
    }

    @Unique
    private static void fireHud$buildFireQuad(VertexConsumer builder, Matrix4f pose, TextureAtlasSprite sprite, int color) {
        float u0 = sprite.getU1();
        float v0 = sprite.getV1();
        float u1 = sprite.getU0();
        float v1 = sprite.getV0();
        builder.addVertex(pose, -0.5f, -0.5f, -0.5f).setUv(u0, v0).setColor(color);
        builder.addVertex(pose, 0.5f, -0.5f, -0.5f).setUv(u1, v0).setColor(color);
        builder.addVertex(pose, 0.5f, 0.5f, -0.5f).setUv(u1, v1).setColor(color);
        builder.addVertex(pose, -0.5f, 0.5f, -0.5f).setUv(u0, v1).setColor(color);
    }
}
