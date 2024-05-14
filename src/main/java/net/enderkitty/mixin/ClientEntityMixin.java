package net.enderkitty.mixin;

import net.enderkitty.SoulFireAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public class ClientEntityMixin implements SoulFireAccessor {
    @Unique private boolean renderSoulFire;

    @Override
    public boolean isRenderSoulFire() {
        return renderSoulFire;
    }

    @Override
    public void setRenderSoulFire(boolean renderSoulFire) {
        this.renderSoulFire = renderSoulFire;
    }
}
