package net.enderkitty.mixin;

import net.enderkitty.ClientFireTick;
import net.enderkitty.FireHud;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(AbstractFireBlock.class)
public class FireBlockMixin {
    
    @Redirect(method = "randomDisplayTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSoundClient(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"))
    private void fireSound(World world, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
        FireHudConfig config = FireHud.getConfig();
        float randomFl = Random.create().nextFloat();
        
        world.playSoundClient(x, y, z, sound, category, config.fireVolume + (config.applyFireVolRand ? randomFl : 0), (config.applyFirePitchRand ? randomFl * 0.7f : 0) + config.firePitch, useDistance);
    }
    
    @Inject(method = "igniteEntity", at = @At(value = "HEAD"))
    private static void igniteEntity(Entity entity, CallbackInfo ci) {
        if (FireHud.getConfig().thermometer && !entity.isFireImmune() && entity instanceof ClientPlayerEntity player) {
            int clientFireTick = ((ClientFireTick) player).fireHud$clientFireTick();
            if (clientFireTick < 0) {
                ((ClientFireTick) player).fireHud$setClientFireTick(clientFireTick + 1);
            } else {
                int i = entity.getEntityWorld().getRandom().nextBetweenExclusive(1, 3);
                ((ClientFireTick) player).fireHud$setClientFireTick(clientFireTick + i);
            }

            if (clientFireTick >= 0) {
                ((ClientFireTick) player).fireHud$setClientFireFor(8.0f);
            }
        }
    }
}
