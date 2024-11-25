package net.enderkitty.mixin;

import net.enderkitty.SoulFireEntityAccessor;
import net.enderkitty.SoulFireRenderStateAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    
    @Inject(method = "updateRenderState", at = @At(value = "TAIL"))
    private void soulFireRenderState(T entity, S state, float tickDelta, CallbackInfo ci) {
        ((SoulFireRenderStateAccessor) state).fireHud$setOnSoulFire(((SoulFireEntityAccessor) entity).fireHud$isOnSoulFire() && !entity.isSpectator());
    }
}
