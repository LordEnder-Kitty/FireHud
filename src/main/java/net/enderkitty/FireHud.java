package net.enderkitty;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FireHud implements ClientModInitializer {
	public static final String MOD_ID = "firehud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static FireHudConfig config;
	
	@Override
	public void onInitializeClient() {
		LOGGER.info("FireHud is loaded! Enjoy configuring your fire hud!");
		
		ConfigHolder<FireHudConfig> configHolder = AutoConfig.register(FireHudConfig.class, GsonConfigSerializer::new);
		FireHud.config = configHolder.getConfig();
		
		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			PlayerEntity player = client.player;
			int width = context.getScaledWindowWidth();
			int height = context.getScaledWindowHeight();
			
			if (player != null && player.isOnFire() && client.options.getPerspective().isFirstPerson() && 
					!(!config.renderFireInLava && player.isInLava()) && !(!config.renderWithFireResistance && player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))) {
				
				if (config.fireScreenTint && !((SoulFireAccessor) player).isRenderSoulFire()) {
					context.fillGradient(0, 0, width, height, config.fireStartColor, config.fireEndColor);
				}
				if (config.fireScreenTint && config.renderSoulFire && ((SoulFireAccessor) player).isRenderSoulFire()) {
					context.fillGradient(0, 0, width, height, config.soulFireStartColor, config.soulFireEndColor);
				}
			}
		});
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.player != null && client.world != null) {
				StatusEffectInstance fireRes = client.player.getStatusEffect(StatusEffects.FIRE_RESISTANCE);
				
				if (config.displayFireResTimer && client.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && fireRes != null && !fireRes.isInfinite()) {
					String styling = fireRes.getDuration() > 120 && fireRes.getDuration() <= 220 ? "§6" : fireRes.getDuration() <= 120 ? "§4" : "§f";
					
					Text durationSeconds = StatusEffectUtil.getDurationText(fireRes, 1.0f, client.world.getTickManager().getTickRate());
					Text durationTicks = Text.literal(String.valueOf(client.player.getStatusEffect(StatusEffects.FIRE_RESISTANCE).getDuration()));
					Text fireResText = Text.translatable("text.firehud.hud.fireResTimer");
					Text text = Text.literal(fireResText.getString() + styling + (config.fireResTimerAsTicks ? durationTicks : durationSeconds).getString());
					
					if (config.renderWithTimeLeft == 0) client.player.sendMessage(text, true);
					else if (config.renderWithTimeLeft > 0 && fireRes.getDuration() <= config.renderWithTimeLeft * 20) {
						client.player.sendMessage(text, true);
					}
				}
			}
		});
	}
	
	
	public static FireHudConfig getConfig() {
		return FireHud.config;
	}
}
