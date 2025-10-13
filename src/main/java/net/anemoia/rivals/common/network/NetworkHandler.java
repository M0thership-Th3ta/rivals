package net.anemoia.rivals.common.network;

import net.anemoia.rivals.Rivals;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
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
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}
