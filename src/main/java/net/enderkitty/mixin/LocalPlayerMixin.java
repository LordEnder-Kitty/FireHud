package net.enderkitty.mixin;

import net.enderkitty.ClientFireTick;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin implements ClientFireTick {
    @Unique private int clientFireTick = 0;

    @Override
    public int fireHud$clientFireTick() {
        return clientFireTick;
    }

    @Override
    public void fireHud$setClientFireTick(int tick) {
        clientFireTick = tick;
    }

    @Override
    public void fireHud$setClientFireFor(float seconds) {
        int ticks = Mth.floor(seconds * 20);
        if (fireHud$clientFireTick() < ticks) {
            fireHud$setClientFireTick(ticks);
        }
    }

}
