package net.anemoia.rivals.common.network;

import net.anemoia.rivals.Rivals;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Rivals.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
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
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
