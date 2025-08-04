package dev.ftb.mods.ftbunearthed.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Worker extends Villager {
    private static final EntityDataAccessor<Boolean> BUSY = SynchedEntityData.defineId(Worker.class, EntityDataSerializers.BOOLEAN);

    public Worker(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(BUSY, false);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // shouldn't normally be possible to reach a worker since they're enclosed
        // but players like to find a way...
        return InteractionResult.PASS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // see above
        return false;
    }

    public boolean isBusy() {
        return entityData.get(BUSY);
    }

    public void setBusy(boolean busy) {
        entityData.set(BUSY, busy);
    }
}
