package net.anemoia.rivals.client.render;

import net.anemoia.rivals.core.registry.ModEntities;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class PlayerGeoAdapter extends Entity implements GeoAnimatable {
    private final AbstractClientPlayer player;
    private final PlayerGeoAnimatable delegate;

    public float yHeadRot;
    public float yHeadRotO;
    public float yBodyRot;
    public float yBodyRotO;

    private static final Map<String, Field> ENTITY_FIELD_CACHE = new HashMap<>();
    private static final Map<String, Field> LIVING_FIELD_CACHE = new HashMap<>();

    static {
        for (String name : new String[]{"yRot", "yRotO", "xRot", "xRotO"}) {
            try {
                Field f = Entity.class.getDeclaredField(name);
                f.setAccessible(true);
                ENTITY_FIELD_CACHE.put(name, f);
            } catch (NoSuchFieldException e) {
                // ignore — mapping differences between environments
            }
        }

        for (String name : new String[]{"yHeadRot", "yHeadRotO", "yBodyRot", "yBodyRotO"}) {
            try {
                Field f = LivingEntity.class.getDeclaredField(name);
                f.setAccessible(true);
                LIVING_FIELD_CACHE.put(name, f);
            } catch (NoSuchFieldException e) {
                // ignore
            }
        }
    }

    public PlayerGeoAdapter(@NotNull AbstractClientPlayer player, @Nullable ResourceLocation animationResource) {
        super((EntityType<? extends Entity>) ModEntities.PLAYER_PROXY.get(), player.level());
        this.player = player;
        this.delegate = new PlayerGeoAnimatable(player, animationResource);

        // initialise adapter so it is non-physical and not culled
        this.setPos(player.getX(), player.getY(), player.getZ());
        this.setRot(player.getYRot(), player.getXRot());

        // copy rotation state so renderer interpolation uses same values as the real player
        copyEntityRotationFromPlayer();
        copyHeadBodyRotationFromPlayer();

        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    public void tick() {
        // Safety: never run logic on server for this adapter
        if (!this.level().isClientSide) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        if (player != null) {
            // Sync position
            this.setPos(player.getX(), player.getY(), player.getZ());
            // Sync immediate rotation values
            this.setRot(player.getYRot(), player.getXRot());

            // Also copy interpolation / body / head rotation states so renderers can interpolate correctly
            copyEntityRotationFromPlayer();
            copyHeadBodyRotationFromPlayer();
        }
    }

    private void copyEntityRotationFromPlayer() {
        setEntityFloatFieldFromSource("yRot", player, player.getYRot());
        setEntityFloatFieldFromSource("yRotO", player, getFloatFieldFromLiving(player, "yRotO", player.getYRot()));
        setEntityFloatFieldFromSource("xRot", player, player.getXRot());
        setEntityFloatFieldFromSource("xRotO", player, getFloatFieldFromLiving(player, "xRotO", player.getXRot()));
    }

    private void copyHeadBodyRotationFromPlayer() {
        // read from player (LivingEntity) via reflection and store in adapter fields we declared above
        this.yHeadRot = getFloatFieldFromLiving(player, "yHeadRot", player.getYRot());
        this.yHeadRotO = getFloatFieldFromLiving(player, "yHeadRotO", this.yHeadRot);
        this.yBodyRot = getFloatFieldFromLiving(player, "yBodyRot", player.getYRot());
        this.yBodyRotO = getFloatFieldFromLiving(player, "yBodyRotO", this.yBodyRot);
    }

    private float getFloatFieldFromLiving(Object instance, String name, float fallback) {
        Field f = LIVING_FIELD_CACHE.get(name);
        if (f == null) return fallback;
        try {
            return f.getFloat(instance);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            return fallback;
        }
    }

    private void setEntityFloatFieldFromSource(String name, Object source, float value) {
        Field f = ENTITY_FIELD_CACHE.get(name);
        if (f == null) return;
        try {
            // attempt to set the adapter's underlying Entity field
            f.setFloat(this, value);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // ignore failures — rendering will still use setRot for immediate orientation
        }
    }

    /* GeoAnimatable delegation */
    @Override
    public @NotNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return delegate.getAnimatableInstanceCache();
    }

    @Override
    public void registerControllers(@NotNull AnimatableManager.ControllerRegistrar controllers) {
        delegate.registerControllers(controllers);
    }

    @Override
    public double getTick(Object ignored) {
        return delegate.getTick(ignored);
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {}

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        // Not expected to be used (adapter is client-only), but satisfy signature.
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public AbstractClientPlayer getPlayer() {
        return player;
    }
}
