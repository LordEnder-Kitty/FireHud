package net.enderkitty;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FireHud implements ClientModInitializer {
	public static final String MOD_ID = "firehud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static FireHudConfig config;
    private static final Identifier FIRE_METER = Identifier.of(MOD_ID, "fire_meter");
    private static final Identifier THERMOMETER = Identifier.of(MOD_ID, "textures/gui/sprites/hud/thermometer.png"); 
    private static final Identifier THERMOMETER_TEMP = Identifier.of(MOD_ID, "textures/gui/sprites/hud/thermometer_temp.png"); 
    private static final Identifier THERMOMETER_TEMP_SOUL = Identifier.of(MOD_ID, "textures/gui/sprites/hud/thermometer_temp_soul.png"); 
    
	@Override
	public void onInitializeClient() {
		if (isClothConfigLoaded()) {
			ConfigHolder<FireHudConfig> configHolder = AutoConfig.register(FireHudConfig.class, GsonConfigSerializer::new);
			FireHud.config = configHolder.getConfig();
		}
		
		HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> {
			layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS, IdentifiedLayer.of(Identifier.of(MOD_ID, "fire_tint"), this::fireTint));
		});
		
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
			if (player != null && client.world != null) {
				StatusEffectInstance fireRes = player.getStatusEffect(StatusEffects.FIRE_RESISTANCE);
				
                // Fire res timer
				if (config.displayFireResTimer && player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && fireRes != null && !fireRes.isInfinite()) {
					String styling = fireRes.getDuration() > 120 && fireRes.getDuration() <= 220 ? "§6" : fireRes.getDuration() <= 120 ? "§4" : "§f";
					
					Text durationSeconds = StatusEffectUtil.getDurationText(fireRes, 1.0f, client.world.getTickManager().getTickRate());
					Text durationTicks = Text.literal(String.valueOf(player.getStatusEffect(StatusEffects.FIRE_RESISTANCE).getDuration()));
					Text fireResText = Text.translatable("text.firehud.hud.fireResTimer");
					Text text = Text.literal(fireResText.getString() + styling + (config.fireResTimerAsTicks ? durationTicks : durationSeconds).getString());
					
					if (config.renderWithTimeLeft == 0) player.sendMessage(text, true);
					else if (config.renderWithTimeLeft > 0 && fireRes.getDuration() <= config.renderWithTimeLeft * 20) {
						player.sendMessage(text, true);
					}
				}
                
                // Thermometer
                if (config.thermometer) {
                    int clientFireTick = ((ClientFireTick) player).fireHud$clientFireTick();

                    if (clientFireTick > 0) {
                        if (player.isInCreativeMode() || !player.isOnFire()) { // Why the fuck doesn't this work?!!!!
                            ((ClientFireTick) player).fireHud$setClientFireTick(0);
                        }
                        if (client.getServer() == null || !client.getServer().isPaused()) {
                            if (player.isFireImmune()) {
                                ((ClientFireTick) player).fireHud$setClientFireTick(clientFireTick - 4);
                            } else {
                                ((ClientFireTick) player).fireHud$setClientFireTick(clientFireTick - 1);
                            }
                        }
                    }
                }
            }
            
		});
        
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer ->
                layeredDrawer.attachLayerAfter(IdentifiedLayer.HOTBAR_AND_BARS, FIRE_METER, this::render)
        );
	}
	
    private void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        // For some reason it's giving me shit so copy/paste code here 'cause I don't wanna work on this anymore
        if (client.player != null) {
            int clientFireTick = ((ClientFireTick) client.player).fireHud$clientFireTick();
            if (clientFireTick > 0) {
                if (client.player.isInCreativeMode() || !client.player.isOnFire()) {
                    ((ClientFireTick) client.player).fireHud$setClientFireTick(0);
                }
            }
        }
        
        if (config.thermometer) {
            if (!config.onlyShowWhenOnFire) {
                renderTherm(context, client);
            } else if (client.player != null && client.player.isOnFire()) {
                renderTherm(context, client);
            }
        }
    }
    private void renderTherm(DrawContext context, MinecraftClient client) {
        if (config.thermometer && client.player instanceof ClientFireTick player) {
            if (config.showFireTicks) {
                context.drawText(client.textRenderer, Text.literal(String.valueOf(player.fireHud$clientFireTick())),
                        thermNumPos(context, player), context.getScaledWindowHeight() / 2 - 22 + 44, Colors.WHITE, true);
            }
            
            context.drawTexture(RenderLayer::getGuiTextured, THERMOMETER,
                    config.onLeftSide ? 6 : context.getScaledWindowWidth() - 16, context.getScaledWindowHeight() / 2 - 22,
                    0, 0, 10, 44, 10, 44);
            if (client.player.isOnFire()) {
                int i = MathHelper.ceil(getThermProgress() * 43) + 1;
                context.drawTexture(RenderLayer::getGuiTextured, thermSprite(),
                        config.onLeftSide ? 6 : context.getScaledWindowWidth() - 16, context.getScaledWindowHeight() / 2 - 22 + 44 - i,
                        0, 44 - i, 10, i, 10, 44);
            }
        }
    }
    private Identifier thermSprite() {
        SoulFireEntityAccessor player = (SoulFireEntityAccessor) MinecraftClient.getInstance().player;
        return player != null && player.fireHud$isOnSoulFire() ? THERMOMETER_TEMP_SOUL : THERMOMETER_TEMP;
    }
    private int thermNumPos(DrawContext context, ClientFireTick player) {
        int length = String.valueOf(player.fireHud$clientFireTick()).length();
        int value = 14;
        for (int i = 1; i < length; i++) {
            value += 6 * i - 6;
        }
        // The for loop was retarded
        return config.onLeftSide ? 9 : context.getScaledWindowWidth() - switch (length) {
            case 1 -> 14;
            case 2 -> 20;
            case 3 -> 26;
            case 4 -> 32;
            case 5 -> 38;
            default -> value;
        };
    }
    public float getThermProgress() {
        ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
        if (playerEntity instanceof ClientFireTick player) {
            int max = 300;
            return MathHelper.clamp((float) player.fireHud$clientFireTick() / max, 0.0f, 1.0f);
        } else return 0.0f;
    }
    
	private void fireTint(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		PlayerEntity player = client.player;
		int width = context.getScaledWindowWidth();
		int height = context.getScaledWindowHeight();

		if (player != null && player.isOnFire() && client.options.getPerspective().isFirstPerson() &&
				!(!config.renderFireInLava && player.isInLava()) && !(!config.renderWithFireResistance && player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))) {

			if (config.fireScreenTint && !((SoulFireEntityAccessor) player).fireHud$isOnSoulFire()) {
				context.fillGradient(0, 0, width, height, config.fireStartColor, config.fireEndColor);
			}
			if (config.fireScreenTint && config.renderSoulFire && ((SoulFireEntityAccessor) player).fireHud$isOnSoulFire()) {
				context.fillGradient(0, 0, width, height, config.soulFireStartColor, config.soulFireEndColor);
			}
		}
	}
    
    
	public static FireHudConfig getConfig() {
		return FireHud.config;
	}
	
	public static boolean isClothConfigLoaded() {
		return FabricLoader.getInstance().isModLoaded("cloth-config2");
	}
}
