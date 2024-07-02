package net.enderkitty.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.enderkitty.FireHud;
import net.enderkitty.SoulFireAccessor;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique private static final Identifier FIRE_VIGNETTE = new Identifier(FireHud.MOD_ID, "textures/fire/fire_vignette.png");
    @Unique private static final Identifier SOUL_FIRE_VIGNETTE = new Identifier(FireHud.MOD_ID, "textures/fire/soul_fire_vignette.png");
    
    @Unique private static final Identifier FIRE_HEART_FULL_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/fire_full");
    @Unique private static final Identifier FIRE_HEART_FULL_BLINKING_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/fire_full_blinking");
    @Unique private static final Identifier FIRE_HEART_HALF_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/fire_half");
    @Unique private static final Identifier FIRE_HEART_HALF_BLINKING_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/fire_half_blinking");
    @Unique private static final Identifier FIRE_HEART_HARDCORE_FULL_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/fire_hardcore_full");
    @Unique private static final Identifier FIRE_HEART_HARDCORE_FULL_BLINKING_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/fire_hardcore_full_blinking");
    @Unique private static final Identifier FIRE_HEART_HARDCORE_HALF_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/fire_hardcore_half");
    @Unique private static final Identifier FIRE_HEART_HARDCORE_HALF_BLINKING_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/fire_hardcore_half_blinking");
    
    @Unique private static final Identifier SOUL_FIRE_HEART_FULL_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/soul_fire_full");
    @Unique private static final Identifier SOUL_FIRE_HEART_FULL_BLINKING_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/soul_fire_full_blinking");
    @Unique private static final Identifier SOUL_FIRE_HEART_HALF_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/soul_fire_half");
    @Unique private static final Identifier SOUL_FIRE_HEART_HALF_BLINKING_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/soul_fire_half_blinking");
    @Unique private static final Identifier SOUL_FIRE_HEART_HARDCORE_FULL_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/soul_fire_hardcore_full");
    @Unique private static final Identifier SOUL_FIRE_HEART_HARDCORE_FULL_BLINKING_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/soul_fire_hardcore_full_blinking");
    @Unique private static final Identifier SOUL_FIRE_HEART_HARDCORE_HALF_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/soul_fire_hardcore_half");
    @Unique private static final Identifier SOUL_FIRE_HEART_HARDCORE_HALF_BLINKING_TEXTURE = new Identifier(FireHud.MOD_ID, "hud/heart/soul_fire_hardcore_half_blinking");
    
    @Unique FireHudConfig config = FireHud.getConfig();
    
    
    @Inject(method = "drawHeart", at = @At("HEAD"), cancellable = true)
    private void drawHeart(DrawContext context, InGameHud.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half, CallbackInfo ci) {
        if (MinecraftClient.getInstance().cameraEntity instanceof PlayerEntity playerEntity && !(!config.renderWithFireResistance && playerEntity.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))) {
            if (config.renderFireHearts && type == InGameHud.HeartType.NORMAL) {
                if (playerEntity.isOnFire() || (!EnchantmentHelper.hasFrostWalker(playerEntity) && 
                        ((playerEntity.getSteppingBlockState().getBlock() == Blocks.MAGMA_BLOCK && !playerEntity.bypassesSteppingEffects()) || 
                        playerEntity.getSteppingBlockState().getBlock() == Blocks.CAMPFIRE))) {
                    
                    context.drawGuiTexture(getFireHeartTexture(hardcore, half, blinking), x, y, 9, 9);
                    ci.cancel();
                }
                if (config.renderSoulFire) {
                    if ((playerEntity.isOnFire() && ((SoulFireAccessor) playerEntity).fireHud$isRenderSoulFire()) || 
                            (!EnchantmentHelper.hasFrostWalker(playerEntity) && playerEntity.getSteppingBlockState().getBlock() == Blocks.SOUL_CAMPFIRE)) {
                        
                        context.drawGuiTexture(getSoulFireHeartTexture(hardcore, half, blinking), x, y, 9, 9);
                        ci.cancel();
                    }
                }
            }
        }
    }
    
    
    @Inject(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I", shift = At.Shift.BEFORE))
    private void render(DrawContext context, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        
        Identifier texture = player != null && ((SoulFireAccessor) player).fireHud$isRenderSoulFire() ? SOUL_FIRE_VIGNETTE : FIRE_VIGNETTE;
        int hudScale = config.vignetteScale;
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int var1 = hudScale == 4 ? 1 : hudScale == 3 ? 2 : hudScale == 2 ? 3 : hudScale == 1 ? 4 : 1;
        int var2 = hudScale == 4 ? 2 : hudScale == 3 ? 4 : hudScale == 2 ? 6 : hudScale == 1 ? 8 : 2;
        int var3 = hudScale == 4 ? 1 : hudScale == 3 ? 3 : hudScale == 2 ? 5 : hudScale == 1 ? 7 : 3;
        
        if (player != null) {
            if (!(!config.renderFireInLava && player.isInLava())) {
                if (!(!config.renderWithFireResistance && player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))) {
                    if (player.isOnFire() && client.options.getPerspective().isFirstPerson()) {
                        if (config.fireVignette == FireHudConfig.VignetteOptions.FULL) {
                            renderTopLeftCorner(texture, context, width, height, var1, var2);
                            renderTopRightCorner(texture, context, width, height, var1, var2, var3);
                            renderBottomLeftCorner(texture, context, width, height, var1, var2, var3);
                            renderBottomRightCorner(texture, context, width, height, var1, var2, var3);
                        }
                        if (config.fireVignette == FireHudConfig.VignetteOptions.UPPER) {
                            renderTopLeftCorner(texture, context, width, height, var1, var2);
                            renderTopRightCorner(texture, context, width, height, var1, var2, var3);
                        }
                        if (config.fireVignette == FireHudConfig.VignetteOptions.LOWER) {
                            renderBottomLeftCorner(texture, context, width, height, var1, var2, var3);
                            renderBottomRightCorner(texture, context, width, height, var1, var2, var3);
                        }
                    }
                }
            }
        }
    }
    
    @Unique
    private void renderTopLeftCorner(Identifier texture, DrawContext context, int width, int height, int var1, int var2) {
        renderOverlay(context, texture, config.vignetteOpacity, 0, 0, 0, 0, width / var2, height / var2, width / var1, height / var1);
    }
    @Unique
    private void renderTopRightCorner(Identifier texture, DrawContext context, int width, int height, int var1, int var2, int var3) {
        renderOverlay(context, texture, config.vignetteOpacity, (width / var2) * var3, 0, width / var2, 0, width, height / var2, width / var1, height / var1);
    }
    @Unique
    private void renderBottomLeftCorner(Identifier texture, DrawContext context, int width, int height, int var1, int var2, int var3) {
        renderOverlay(context, texture, config.vignetteOpacity, 0, (height / var2) * var3, 0, height / var2, width / var2, height, width / var1, height / var1);
    }
    @Unique
    private void renderBottomRightCorner(Identifier texture, DrawContext context, int width, int height, int var1, int var2, int var3) {
        renderOverlay(context, texture, config.vignetteOpacity, (width / var2) * var3, (height / var2) * var3, width / var2, height / var2, width, height, width / var1, height / var1);
    }
    
    @Unique
    private void renderOverlay(DrawContext context, Identifier texture, float opacity, int xPos, int yPos, int uStart, int vStart, int uEnd, int vEnd, int textureWidth, int textureHeight) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        context.setShaderColor(1.0f, 1.0f, 1.0f, opacity);
        context.drawTexture(texture, xPos, yPos, -90, uStart, vStart, uEnd, vEnd, textureWidth, textureHeight);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    @Unique
    public Identifier getFireHeartTexture(boolean hardcore, boolean half, boolean blinking) {
        if (!hardcore) {
            if (half) return blinking ? FIRE_HEART_HALF_BLINKING_TEXTURE : FIRE_HEART_HALF_TEXTURE;
            return blinking ? FIRE_HEART_FULL_BLINKING_TEXTURE : FIRE_HEART_FULL_TEXTURE;
        }
        if (half) return blinking ? FIRE_HEART_HARDCORE_HALF_BLINKING_TEXTURE : FIRE_HEART_HARDCORE_HALF_TEXTURE;
        return blinking ? FIRE_HEART_HARDCORE_FULL_BLINKING_TEXTURE : FIRE_HEART_HARDCORE_FULL_TEXTURE;
    }
    @Unique
    public Identifier getSoulFireHeartTexture(boolean hardcore, boolean half, boolean blinking) {
        if (!hardcore) {
            if (half) return blinking ? SOUL_FIRE_HEART_HALF_BLINKING_TEXTURE : SOUL_FIRE_HEART_HALF_TEXTURE;
            return blinking ? SOUL_FIRE_HEART_FULL_BLINKING_TEXTURE : SOUL_FIRE_HEART_FULL_TEXTURE;
        }
        if (half) return blinking ? SOUL_FIRE_HEART_HARDCORE_HALF_BLINKING_TEXTURE : SOUL_FIRE_HEART_HARDCORE_HALF_TEXTURE;
        return blinking ? SOUL_FIRE_HEART_HARDCORE_FULL_BLINKING_TEXTURE : SOUL_FIRE_HEART_HARDCORE_FULL_TEXTURE;
    }
}
