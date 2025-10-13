package net.anemoia.rivals.common.network;

import net.anemoia.rivals.common.handlers.AbilityTriggerHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class AbilityTriggerPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbilityTriggerPacket.class);
    private final String keyPressed;

    public AbilityTriggerPacket(String keyPressed) {
        this.keyPressed = keyPressed;
    }

    public AbilityTriggerPacket(FriendlyByteBuf buf) {
        this.keyPressed = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.keyPressed);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                LOGGER.debug("Received ability trigger for key {} from player {}", keyPressed, player.getName().getString());

                AbilityTriggerHandler.triggerAbility(player, keyPressed);
            }
        });
        return true;
    }
}
