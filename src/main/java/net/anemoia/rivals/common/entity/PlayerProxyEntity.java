package net.anemoia.rivals.common.entity;

import net.anemoia.rivals.core.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;
import java.util.UUID;

public class PlayerProxyEntity extends Entity implements GeoAnimatable {
    private UUID ownerUuid;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PlayerProxyEntity(EntityType<? extends PlayerProxyEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.noCulling = true;
        this.setInvisible(true);
    }

    public PlayerProxyEntity(Level level, ServerPlayer owner) {
        this(ModEntities.PLAYER_PROXY.get(), level);
        Objects.requireNonNull(owner);
        this.ownerUuid = owner.getUUID();
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
        this.setRot(owner.getYRot(), owner.getXRot());
        this.setBoundingBox(new AABB(-0.3, 0, -0.3, 0.3, 1.8, 0.3).move(this.position()));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (ownerUuid != null) {
            var server = this.level().getServer();
            if (server == null) {
                this.discard();
                return;
            }
            var owner = server.getPlayerList().getPlayer(ownerUuid);
            if (owner == null || owner.isRemoved()) {
                this.discard();
                return;
            }
            this.setPos(owner.getX(), owner.getY(), owner.getZ());
            this.setRot(owner.getYRot(), owner.getXRot());
            this.setBoundingBox(getBoundingBox().move(this.position()).inflate(0));
        } else {
            this.discard();
        }
    }

    public void checkCollisionsAndApply(java.util.function.Consumer<Entity> onHit) {
        if (this.level().isClientSide) return;
        AABB box = this.getBoundingBox().inflate(0.5);
        for (Entity ent : this.level().getEntities(this, box)) {
            if (ent == this) continue;
            onHit.accept(ent);
        }
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.putUUID("owner", this.ownerUuid);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    // --- GeoAnimatable implementation ---
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object ignored) {
        return this.tickCount;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Register your animation controllers here, e.g.:
        // controllers.add(new AnimationController<>(this, "walk_idle", 0, state -> ...));
    }
}
