package net.enderkitty.mixin;

import net.enderkitty.SoulFireRenderStateAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(value = EnvType.CLIENT)
@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements SoulFireRenderStateAccessor {
    @Unique private boolean onSoulFire;
    
    @Override
    public boolean fireHud$onSoulFire() {
        return onSoulFire;
    }

    @Override
    public void fireHud$setOnSoulFire(boolean onSoulFire) {
        this.onSoulFire = onSoulFire;
    }
}
