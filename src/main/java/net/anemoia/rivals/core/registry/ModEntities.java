package net.anemoia.rivals.core.registry;

import net.anemoia.rivals.Rivals;
import net.anemoia.rivals.common.entity.PlayerProxyEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Rivals.MOD_ID);

    public static final RegistryObject<EntityType<PlayerProxyEntity>> PLAYER_PROXY = ENTITIES.register("player_proxy",
            () -> EntityType.Builder.<PlayerProxyEntity>of(PlayerProxyEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f) // default size; adjust if you want a smaller hitbox
                    .clientTrackingRange(64)
                    .build(new ResourceLocation(Rivals.MOD_ID, "player_proxy").toString()));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }

    // Helper to spawn a proxy for a server player (server-side only)
    public static PlayerProxyEntity spawnPlayerProxy(ServerPlayer player) {
        if (player == null || player.level().isClientSide) return null;
        PlayerProxyEntity proxy = new PlayerProxyEntity(player.level(), player);
        player.level().addFreshEntity(proxy);
        return proxy;
    }
}
