package net.enderkitty;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class FireHud implements ClientModInitializer {
    public static final String MOD_ID = "firehud";
    /** Fire tick count the thermometer scales against. */
    private static final int MAX_FIRE_TICKS = 300;

    // Defaults stand in until (and unless) Cloth Config hands us the saved ones, so no consumer has to
    // care whether the optional dependency is present
    private static FireHudConfig config = new FireHudConfig();

    private static final Identifier FIRE_TINT = id("fire_tint");
    private static final Identifier FIRE_METER = id("fire_meter");
    private static final Identifier THERMOMETER = id("textures/gui/sprites/hud/thermometer.png");
    private static final Identifier THERMOMETER_TEMP = id("textures/gui/sprites/hud/thermometer_temp.png");
    private static final Identifier THERMOMETER_TEMP_SOUL = id("textures/gui/sprites/hud/thermometer_temp_soul.png");
    private static final Component FIRE_RES_LABEL = Component.translatable("text.firehud.hud.fireResTimer");

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        if (isClothConfigLoaded()) {
            config = AutoConfig.register(FireHudConfig.class, GsonConfigSerializer::new).getConfig();
        }

        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, FIRE_TINT, this::fireTint);
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, FIRE_METER, this::thermometer);

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            LocalPlayer player = client.player;
            if (player == null || client.level == null) return;

            if (config.displayFireResTimer) fireResTimer(client, player);
            if (config.thermometer) tickFireMeter(client, player);
        });
    }

    private static void fireResTimer(Minecraft client, LocalPlayer player) {
        MobEffectInstance fireRes = player.getEffect(MobEffects.FIRE_RESISTANCE);
        if (fireRes == null || fireRes.isInfiniteDuration()) return;

        int duration = fireRes.getDuration();
        int threshold = config.renderWithTimeLeft;
        if (threshold < 0 || (threshold > 0 && duration > threshold * 20)) return;

        ChatFormatting styling = duration <= 120 ? ChatFormatting.DARK_RED
                : duration <= 220 ? ChatFormatting.GOLD
                : ChatFormatting.WHITE;
        Component left = config.fireResTimerAsTicks
                ? Component.literal(String.valueOf(duration))
                : MobEffectUtil.formatDuration(fireRes, 1.0f, client.level.tickRateManager().tickrate());

        player.sendOverlayMessage(FIRE_RES_LABEL.copy().append(left.copy().withStyle(styling)));
    }

    private static void tickFireMeter(Minecraft client, LocalPlayer player) {
        ClientFireTick fire = (ClientFireTick) player;
        int ticks = fire.fireHud$clientFireTick();
        if (ticks <= 0) return;

        clearIfExtinguished(player);
        if (client.getSingleplayerServer() != null && client.getSingleplayerServer().isPaused()) return;
        fire.fireHud$setClientFireTick(ticks - (player.fireImmune() ? 4 : 1));
    }

    /** Run from the HUD element as well, because the tick loop stops while a singleplayer world is paused. */
    private static void clearIfExtinguished(LocalPlayer player) {
        ClientFireTick fire = (ClientFireTick) player;
        if (fire.fireHud$clientFireTick() > 0 && (player.isCreative() || !player.isOnFire())) {
            fire.fireHud$setClientFireTick(0);
        }
    }

    private void thermometer(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!config.thermometer) return;

        Minecraft client = Minecraft.getInstance();
        if (!(client.player instanceof ClientFireTick fire)) return;

        clearIfExtinguished(client.player);
        if (config.onlyShowWhenOnFire && !client.player.isOnFire()) return;

        int x = config.onLeftSide ? 6 : graphics.guiWidth() - 16;
        int top = graphics.guiHeight() / 2 - 22;

        if (config.showFireTicks) {
            String ticks = String.valueOf(fire.fireHud$clientFireTick());
            int textX = config.onLeftSide ? 9 : graphics.guiWidth() - 8 - client.font.width(ticks);
            graphics.text(client.font, Component.literal(ticks), textX, top + 44, CommonColors.WHITE, true);
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, THERMOMETER, x, top, 0.0f, 0.0f, 10, 44, 10, 44);

        if (client.player.isOnFire()) {
            float progress = Mth.clamp((float) fire.fireHud$clientFireTick() / MAX_FIRE_TICKS, 0.0f, 1.0f);
            int filled = Mth.ceil(progress * 43) + 1;
            Identifier temp = ((SoulFireHolder) client.player).fireHud$isOnSoulFire() ? THERMOMETER_TEMP_SOUL : THERMOMETER_TEMP;
            graphics.blit(RenderPipelines.GUI_TEXTURED, temp, x, top + 44 - filled, 0.0f, 44 - filled, 10, filled, 10, 44);
        }
    }

    private void fireTint(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!config.fireScreenTint) return;

        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || !player.isOnFire() || !client.options.getCameraType().isFirstPerson()) return;
        if (suppressed(player, config.renderFireInLava)) return;

        boolean soul = ((SoulFireHolder) player).fireHud$isOnSoulFire();
        if (soul && !config.renderSoulFire) return;

        graphics.fillGradient(0, 0, graphics.guiWidth(), graphics.guiHeight(),
                soul ? config.soulFireStartColor : config.fireStartColor,
                soul ? config.soulFireEndColor : config.fireEndColor);
    }

    /**
     * The lava / fire resistance pair that gates every fire visual. The lava option is the caller's own, because
     * the first person and third person overlays each have one.
     */
    public static boolean suppressed(Player player, boolean renderInLava) {
        return (!renderInLava && player.isInLava()) || fireResSuppressed(player);
    }

    public static boolean fireResSuppressed(Player player) {
        return !config.renderWithFireResistance && player.hasEffect(MobEffects.FIRE_RESISTANCE);
    }

    public static FireHudConfig getConfig() {
        return FireHud.config;
    }

    public static boolean isClothConfigLoaded() {
        return FabricLoader.getInstance().isModLoaded("cloth-config2");
    }
}
