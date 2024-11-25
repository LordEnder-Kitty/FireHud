package net.enderkitty.mixin;

import net.enderkitty.EnchantTags;
import net.enderkitty.FireHud;
import net.enderkitty.SoulFireEntityAccessor;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique private static final Identifier FIRE_VIGNETTE = Identifier.of(FireHud.MOD_ID, "textures/fire/fire_vignette.png");
    @Unique private static final Identifier SOUL_FIRE_VIGNETTE = Identifier.of(FireHud.MOD_ID, "textures/fire/soul_fire_vignette.png");
    
    @Unique private static final Identifier FIRE_HEART_FULL_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/fire_full");
    @Unique private static final Identifier FIRE_HEART_FULL_BLINKING_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/fire_full_blinking");
    @Unique private static final Identifier FIRE_HEART_HALF_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/fire_half");
    @Unique private static final Identifier FIRE_HEART_HALF_BLINKING_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/fire_half_blinking");
    @Unique private static final Identifier FIRE_HEART_HARDCORE_FULL_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/fire_hardcore_full");
    @Unique private static final Identifier FIRE_HEART_HARDCORE_FULL_BLINKING_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/fire_hardcore_full_blinking");
    @Unique private static final Identifier FIRE_HEART_HARDCORE_HALF_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/fire_hardcore_half");
    @Unique private static final Identifier FIRE_HEART_HARDCORE_HALF_BLINKING_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/fire_hardcore_half_blinking");
    
    @Unique private static final Identifier SOUL_FIRE_HEART_FULL_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/soul_fire_full");
    @Unique private static final Identifier SOUL_FIRE_HEART_FULL_BLINKING_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/soul_fire_full_blinking");
    @Unique private static final Identifier SOUL_FIRE_HEART_HALF_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/soul_fire_half");
    @Unique private static final Identifier SOUL_FIRE_HEART_HALF_BLINKING_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/soul_fire_half_blinking");
    @Unique private static final Identifier SOUL_FIRE_HEART_HARDCORE_FULL_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/soul_fire_hardcore_full");
    @Unique private static final Identifier SOUL_FIRE_HEART_HARDCORE_FULL_BLINKING_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/soul_fire_hardcore_full_blinking");
    @Unique private static final Identifier SOUL_FIRE_HEART_HARDCORE_HALF_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/soul_fire_hardcore_half");
    @Unique private static final Identifier SOUL_FIRE_HEART_HARDCORE_HALF_BLINKING_TEXTURE = Identifier.of(FireHud.MOD_ID, "hud/heart/soul_fire_hardcore_half_blinking");
    
    @Unique FireHudConfig config = FireHud.getConfig();
    
    
    @Inject(method = "drawHeart", at = @At("HEAD"), cancellable = true)
    private void drawHeart(DrawContext context, InGameHud.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half, CallbackInfo ci) {
        if (MinecraftClient.getInstance().cameraEntity instanceof PlayerEntity playerEntity && !(!config.renderWithFireResistance && playerEntity.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))) {
            if (config.renderFireHearts && type == InGameHud.HeartType.NORMAL) {
                boolean hasFrostWalkerOnBoots = false;
                for (RegistryEntry<Enchantment> enchantment : playerEntity.getEquippedStack(EquipmentSlot.FEET).getEnchantments().getEnchantments()) {
                    if (ClientTags.isInWithLocalFallback(EnchantTags.FROST_WALKER, enchantment)) {
                        hasFrostWalkerOnBoots = true;
                    }
                }
                
                if (playerEntity.isOnFire() || (!hasFrostWalkerOnBoots && ((playerEntity.getSteppingBlockState().getBlock() == Blocks.MAGMA_BLOCK && !playerEntity.bypassesSteppingEffects()) || 
                        playerEntity.getSteppingBlockState().getBlock() == Blocks.CAMPFIRE))) {
                    context.drawGuiTexture(RenderLayer::getGuiTextured, getFireHeartTexture(hardcore, half, blinking), x, y, 9, 9);
                    ci.cancel();
                }
                if (config.renderSoulFire) {
                    if ((playerEntity.isOnFire() && ((SoulFireEntityAccessor) playerEntity).fireHud$isOnSoulFire()) || (!hasFrostWalkerOnBoots && playerEntity.getSteppingBlockState().getBlock() == Blocks.SOUL_CAMPFIRE)) {
                        context.drawGuiTexture(RenderLayer::getGuiTextured, getSoulFireHeartTexture(hardcore, half, blinking), x, y, 9, 9);
                        ci.cancel();
                    }
                }
            }
        }
    }
    
    @Unique
    private boolean scaleHelper(int scale) {
        int hudScale = config.vignetteScale;
        int guiScale = MinecraftClient.getInstance().options.getGuiScale().getValue();
        return hudScale == scale || hudScale == 0 && guiScale == scale;
    }
    
    @Inject(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I"))
    private void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        
        Identifier texture = player != null && ((SoulFireEntityAccessor) player).fireHud$isOnSoulFire() ? SOUL_FIRE_VIGNETTE : FIRE_VIGNETTE;
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int var1 = scaleHelper(4) ? 1 : scaleHelper(3) ? 2 : scaleHelper(2) ? 3 : scaleHelper(1) ? 4 : 1;
        int var2 = scaleHelper(4) ? 2 : scaleHelper(3) ? 4 : scaleHelper(2) ? 6 : scaleHelper(1) ? 8 : 2;
        int var3 = scaleHelper(4) ? 1 : scaleHelper(3) ? 3 : scaleHelper(2) ? 5 : scaleHelper(1) ? 7 : 3;
        
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
        int i = ColorHelper.getWhite(opacity);
        context.drawTexture(RenderLayer::getGuiTextured, texture, xPos, yPos, uStart, vStart, uEnd, vEnd, textureWidth, textureHeight, i);
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
