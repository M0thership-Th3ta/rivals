package net.anemoia.rivals.common.network;

import net.anemoia.rivals.client.render.ClientHeroState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncPlayerHeroS2CPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncPlayerHeroS2CPacket.class);
    public final UUID playerUuid;
    public final ResourceLocation heroId; // nullable -> no hero

    public SyncPlayerHeroS2CPacket(UUID playerUuid, ResourceLocation heroId) {
        this.playerUuid = Objects.requireNonNull(playerUuid);
        this.heroId = heroId;
    }

    public static void encode(SyncPlayerHeroS2CPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.playerUuid);
        buf.writeBoolean(pkt.heroId != null);
        if (pkt.heroId != null) buf.writeUtf(pkt.heroId.toString());
    }

    public static SyncPlayerHeroS2CPacket decode(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        boolean has = buf.readBoolean();
        ResourceLocation rl = has ? new ResourceLocation(buf.readUtf(32767)) : null;
        return new SyncPlayerHeroS2CPacket(uuid, rl);
    }

    public static void handle(SyncPlayerHeroS2CPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LOGGER.debug("Received SyncPlayerHeroS2C for player {} hero={}", pkt.playerUuid, pkt.heroId);
            ClientHeroState.setHeroFor(pkt.playerUuid, pkt.heroId);
            // extra log to confirm the mapping was applied
            var snapshot = ClientHeroState.getHeroFor(pkt.playerUuid);
            LOGGER.debug("ClientHeroState set: {} -> {}", pkt.playerUuid, snapshot);
        });
        ctx.get().setPacketHandled(true);
    }
}
