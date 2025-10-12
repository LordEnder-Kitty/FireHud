package net.enderkitty.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.fog.LavaFogModifier;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(LavaFogModifier.class)
public class LavaFogModifierMixin {
    
}
