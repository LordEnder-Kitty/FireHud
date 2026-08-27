package net.enderkitty.mixin;

import net.enderkitty.SoulFireHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    
    @Inject(method = "extractRenderState", at = @At(value = "TAIL"))
    private void soulFireRenderState(T entity, S state, float partialTicks, CallbackInfo ci) {
        ((SoulFireHolder) state).fireHud$setOnSoulFire(
                ((SoulFireHolder) entity).fireHud$isOnSoulFire() && !entity.isSpectator());
    }
}
