package net.enderkitty.mixin;

import net.enderkitty.EnchantTags;
import net.enderkitty.FireHud;
import net.enderkitty.SoulFireHolder;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Hud.class)
public abstract class HudMixin {
    @Unique private static final Identifier FIRE_VIGNETTE = FireHud.id("textures/fire/fire_vignette.png");
    @Unique private static final Identifier SOUL_FIRE_VIGNETTE = FireHud.id("textures/fire/soul_fire_vignette.png");

    // Indexed by hardcore << 2 | half << 1 | blinking, following vanilla's own heart sprite naming
    @Unique private static final Identifier[] FIRE_HEARTS = fireHud$heartSet("fire");
    @Unique private static final Identifier[] SOUL_FIRE_HEARTS = fireHud$heartSet("soul_fire");

    @Unique
    private static Identifier[] fireHud$heartSet(String kind) {
        Identifier[] sprites = new Identifier[8];
        for (int i = 0; i < sprites.length; i++) {
            sprites[i] = FireHud.id("hud/heart/" + kind
                    + ((i & 4) != 0 ? "_hardcore" : "")
                    + ((i & 2) != 0 ? "_half" : "_full")
                    + ((i & 1) != 0 ? "_blinking" : ""));
        }
        return sprites;
    }

    @Unique
    private static Identifier fireHud$heart(Identifier[] set, boolean hardcore, boolean half, boolean blinking) {
        return set[(hardcore ? 4 : 0) | (half ? 2 : 0) | (blinking ? 1 : 0)];
    }

    @Inject(method = "extractHeart", at = @At("HEAD"), cancellable = true)
    private void drawHeart(GuiGraphicsExtractor graphics, Hud.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half, CallbackInfo ci) {
        FireHudConfig config = FireHud.getConfig();
        if (!config.renderFireHearts || type != Hud.HeartType.NORMAL) return;
        if (!(Minecraft.getInstance().getCameraEntity() instanceof Player playerEntity)) return;
        if (FireHud.fireResSuppressed(playerEntity)) return;

        BlockState onBlock = playerEntity.getBlockStateOn();
        boolean standingOnFire = onBlock.getBlock() == Blocks.MAGMA_BLOCK && !playerEntity.isSteppingCarefully()
                || onBlock.getBlock() instanceof CampfireBlock && onBlock.getValue(BlockStateProperties.LIT);
        if (!playerEntity.isOnFire() && !(standingOnFire && !fireHud$blocksFireHearts(playerEntity))) return;

        boolean soul = config.renderSoulFire && (((SoulFireHolder) playerEntity).fireHud$isOnSoulFire()
                || onBlock.getBlock() == Blocks.SOUL_CAMPFIRE);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                fireHud$heart(soul ? SOUL_FIRE_HEARTS : FIRE_HEARTS, hardcore, half, blinking), x, y, 9, 9);
        ci.cancel();
    }

    @Unique
    private static boolean fireHud$blocksFireHearts(Player player) {
        for (Holder<Enchantment> enchantment : player.getItemBySlot(EquipmentSlot.FEET).getEnchantments().keySet()) {
            if (ClientTags.isInWithLocalFallback(EnchantTags.PREVENTS_FIRE_HEARTS, enchantment)) return true;
        }
        return false;
    }

    @Inject(method = "extractCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I"))
    private void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        FireHudConfig config = FireHud.getConfig();
        if (config.fireVignette == FireHudConfig.VignetteOptions.OFF) return;

        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || !player.isOnFire() || !client.options.getCameraType().isFirstPerson()) return;
        if (FireHud.suppressed(player, config.renderFireInLava)) return;

        // Scales 1-4 map to (4,8,7) (3,6,5) (2,4,3) (1,2,1). Anything else - GUI scale "auto", or a screen big
        // enough to allow 5+ - keeps the original fallback triple, which deliberately breaks that pattern
        int scale = config.vignetteScale != 0 ? config.vignetteScale : client.options.guiScale().get();
        boolean known = scale >= 1 && scale <= 4;
        int var1 = known ? 5 - scale : 1;
        int var2 = known ? var1 * 2 : 2;
        int var3 = known ? var2 - 1 : 3;

        Identifier texture = ((SoulFireHolder) player).fireHud$isOnSoulFire() ? SOUL_FIRE_VIGNETTE : FIRE_VIGNETTE;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int color = ARGB.white(config.vignetteOpacity);

        if (config.fireVignette != FireHudConfig.VignetteOptions.LOWER) {
            fireHud$renderCorner(graphics, texture, width, height, var1, var2, var3, false, false, color);
            fireHud$renderCorner(graphics, texture, width, height, var1, var2, var3, true, false, color);
        }
        if (config.fireVignette != FireHudConfig.VignetteOptions.UPPER) {
            fireHud$renderCorner(graphics, texture, width, height, var1, var2, var3, false, true, color);
            fireHud$renderCorner(graphics, texture, width, height, var1, var2, var3, true, true, color);
        }
    }

    @Unique
    private void fireHud$renderCorner(GuiGraphicsExtractor graphics, Identifier texture, int width, int height,
                                      int var1, int var2, int var3, boolean right, boolean bottom, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
                right ? (width / var2) * var3 : 0, bottom ? (height / var2) * var3 : 0,
                right ? width / var2 : 0, bottom ? height / var2 : 0,
                right ? width : width / var2, bottom ? height : height / var2,
                width / var1, height / var1, color);
    }
}
