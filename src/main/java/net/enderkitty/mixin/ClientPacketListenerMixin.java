package net.enderkitty.mixin;

import net.enderkitty.FireHud;
import net.enderkitty.SoulFireHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow private ClientLevel level;
    
    @Inject(method = "handleDamageEvent", at = @At("HEAD"))
    public void entitySetsOnSoulFire(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        if (!FireHud.getConfig().renderSoulFire || level == null) return;

        Entity targetEntity = level.getEntity(packet.entityId());
        if (targetEntity == null) return;

        Entity sourceEntity = level.getEntity(packet.sourceDirectId());
        if (sourceEntity != null && (sourceEntity instanceof Zombie || sourceEntity instanceof Arrow) && sourceEntity.displayFireAnimation()) {
            ((SoulFireHolder) targetEntity).fireHud$setOnSoulFire(((SoulFireHolder) sourceEntity).fireHud$isOnSoulFire());
        }
        if (packet.getSource(level).is(DamageTypes.LIGHTNING_BOLT)) {
            ((SoulFireHolder) targetEntity).fireHud$setOnSoulFire(false);
        }
    }
    
    /**
     * {@code BaseFireBlock#entityInside} never runs client side for entities the client does not own, so the
     * fire type has to be read off the blocks each entity is standing in. Last block in the volume wins, and
     * lava overrides everything.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    public void clientTickEvents(CallbackInfo ci) {
        if (level == null || !FireHud.getConfig().renderSoulFire) return;

        level.entitiesForRendering().forEach(entity -> {
            SoulFireHolder holder = (SoulFireHolder) entity;
            if (entity.isInLava()) {
                holder.fireHud$setOnSoulFire(false);
                return;
            }

            AABB box = entity.getBoundingBox().deflate(1.0E-3);
            BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
            BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
            Level entityLevel = entity.level();
            if (!entityLevel.hasChunksAt(min, max)) return;

            for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
                Block block = entityLevel.getBlockState(pos).getBlock();
                if (block instanceof SoulFireBlock) holder.fireHud$setOnSoulFire(true);
                else if (block instanceof FireBlock) holder.fireHud$setOnSoulFire(false);
            }
        });
    }
}
