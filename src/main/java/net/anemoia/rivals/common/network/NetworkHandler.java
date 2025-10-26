package net.anemoia.rivals.common.network;

import net.anemoia.rivals.Rivals;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkHandler.class);
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Rivals.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        LOGGER.info("Initializing NetworkHandler channel for {}", Rivals.MOD_ID);
        int id = 0;
        INSTANCE.messageBuilder(AbilityTriggerPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(AbilityTriggerPacket::new)
                .encoder(AbilityTriggerPacket::toBytes)
                .consumerMainThread(AbilityTriggerPacket::handle)
                .add();
        INSTANCE.messageBuilder(HeroSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(HeroSyncPacket::new)
                .encoder(HeroSyncPacket::toBytes)
                .consumerMainThread(HeroSyncPacket::handle)
                .add();
        INSTANCE.messageBuilder(ChargeSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ChargeSyncPacket::new)
                .encoder(ChargeSyncPacket::toBytes)
                .consumerMainThread(ChargeSyncPacket::handle)
                .add();
        INSTANCE.messageBuilder(ViewAbilityPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ViewAbilityPacket::new)
                .encoder(ViewAbilityPacket::toBytes)
                .consumerMainThread(ViewAbilityPacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncPlayerHeroS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncPlayerHeroS2CPacket::decode)
                .encoder(SyncPlayerHeroS2CPacket::encode)
                .consumerMainThread(SyncPlayerHeroS2CPacket::handle)
                .add();
        LOGGER.info("Registered {} network message ids (final id={} )", id, id);
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendHeroUpdate(ServerPlayer player, ResourceLocation heroId) {
        LOGGER.info("NetworkHandler.sendHeroUpdate() called for player {} hero={}", player.getUUID(), heroId);
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncPlayerHeroS2CPacket(player.getUUID(), heroId));
    }
}
