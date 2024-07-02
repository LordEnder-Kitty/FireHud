package net.enderkitty.mixin;

import net.enderkitty.FireHud;
import net.enderkitty.SoulFireAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.FireBlock;
import net.minecraft.block.SoulFireBlock;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow private ClientWorld world;
    
    @Inject(method = "onEntityDamage", at = @At("HEAD"))
    public void entitySetsOnSoulFire(EntityDamageS2CPacket packet, CallbackInfo ci) {
        if (world != null && FireHud.getConfig().renderSoulFire) {
            Entity targetEntity = world.getEntityById(packet.entityId());
            Entity sourceEntity = world.getEntityById(packet.sourceDirectId());
            if (targetEntity != null && sourceEntity != null) {
                if ((sourceEntity instanceof ZombieEntity || sourceEntity instanceof ArrowEntity) && sourceEntity.doesRenderOnFire()) {
                    ((SoulFireAccessor) targetEntity).fireHud$setRenderSoulFire(((SoulFireAccessor) sourceEntity).fireHud$isRenderSoulFire());
                }
            }
            if (targetEntity != null) {
                if (packet.createDamageSource(world).isOf(DamageTypes.LIGHTNING_BOLT)) {
                    ((SoulFireAccessor) targetEntity).fireHud$setRenderSoulFire(false);
                }
            }
        }
    }
    
    @Inject(method = "tick", at = @At("HEAD"))
    public void clientTickEvents(CallbackInfo ci) {
        if (world != null && FireHud.getConfig().renderSoulFire) {
            world.getEntities().forEach(entity -> {
                Box box = entity.getBoundingBox();
                BlockPos blockPos = new BlockPos(MathHelper.floor(box.minX + 0.001), MathHelper.floor(box.minY + 0.001), MathHelper.floor(box.minZ + 0.001));
                BlockPos blockPos2 = new BlockPos(MathHelper.floor(box.maxX - 0.001), MathHelper.floor(box.maxY - 0.001), MathHelper.floor(box.maxZ - 0.001));
                if (entity.getWorld() != null && entity.getWorld().isRegionLoaded(blockPos, blockPos2)) {
                    BlockPos.Mutable mutable = new BlockPos.Mutable();
                    for (int i = blockPos.getX(); i <= blockPos2.getX(); ++i) {
                        for (int j = blockPos.getY(); j <= blockPos2.getY(); ++j) {
                            for (int k = blockPos.getZ(); k <= blockPos2.getZ(); ++k) {
                                mutable.set(i, j, k);
                                try {
                                    Block block = entity.getWorld().getBlockState(mutable).getBlock();
                                    if (block instanceof SoulFireBlock) ((SoulFireAccessor)entity).fireHud$setRenderSoulFire(true);
                                    if (block instanceof FireBlock) ((SoulFireAccessor)entity).fireHud$setRenderSoulFire(false);
                                    if (entity.isInLava()) ((SoulFireAccessor)entity).fireHud$setRenderSoulFire(false);
                                } catch (Throwable throwable) {
                                    CrashReport crashReport = CrashReport.create(throwable, "Colliding entity with block");
                                    throw new CrashException(crashReport);
                                }
                            }
                        }
                    }
                }
            });
        }
    }
    
}
