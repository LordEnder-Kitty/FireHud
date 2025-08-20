package net.enderkitty.mixin;

import net.enderkitty.ClientFireTick;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin implements ClientFireTick {
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
        int ticks = MathHelper.floor(seconds * 20);
        if (fireHud$clientFireTick() < ticks) {
            fireHud$setClientFireTick(ticks);
        }
    }

}
