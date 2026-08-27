package net.enderkitty.mixin;

import net.enderkitty.ClientFireTick;
import net.enderkitty.FireHud;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BaseFireBlock.class)
public class FireBlockMixin {
    
    @Redirect(method = "animateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    private void fireSound(Level level, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, boolean distanceDelay) {
        FireHudConfig config = FireHud.getConfig();
        RandomSource random = level.getRandom();
        
        // The game clamps the final gain to 1.0, so adding a flat 0-1 on top made every Fire Volume above ~0.5
        // sound identical. Scaling instead keeps the setting a true master while matching vanilla at 1.0.
        float newVolume = config.fireVolume * (config.applyFireVolRand ? 1.0f + random.nextFloat() : 1.0f);
        // Pitch is clamped to 0.5-2.0, so the 0.5 span keeps the whole random range audible instead of piling
        // roughly a third of the rolls onto the 0.5 floor like vanilla does.
        float newPitch = config.firePitch + (config.applyFirePitchRand ? random.nextFloat() * 0.5f : 0.0f);
        
        level.playLocalSound(x, y, z, sound, source, newVolume, newPitch, distanceDelay);
    }
    
    @Inject(method = "fireIgnite", at = @At(value = "HEAD"))
    private static void fireIgnite(Entity entity, CallbackInfo ci) {
        if (FireHud.getConfig().thermometer && !entity.fireImmune() && entity instanceof LocalPlayer player) {
            ClientFireTick fire = (ClientFireTick) player;
            int clientFireTick = fire.fireHud$clientFireTick();

            if (clientFireTick < 0) {
                fire.fireHud$setClientFireTick(clientFireTick + 1);
            } else {
                fire.fireHud$setClientFireTick(clientFireTick + entity.level().getRandom().nextInt(2, 3));
                fire.fireHud$setClientFireFor(8.0f);
            }
        }
    }
}
