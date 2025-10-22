package net.anemoia.rivals.common.network;

import net.anemoia.rivals.common.capabilities.PlayerHeroDataCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class HeroSyncPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeroSyncPacket.class);
    private final ResourceLocation heroId;

    public HeroSyncPacket(ResourceLocation heroId) {
        this.heroId = heroId;
    }

    public HeroSyncPacket(FriendlyByteBuf buf) {
        boolean hasHero = buf.readBoolean();
        if (hasHero) {
            this.heroId = buf.readResourceLocation();
        } else {
            this.heroId = null;
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(heroId != null);
        if (heroId != null) {
            buf.writeResourceLocation(heroId);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                LOGGER.debug("Received hero sync packet for hero: {}", heroId);

                player.getCapability(PlayerHeroDataCapability.PLAYER_HERO_DATA).ifPresent(data -> {
                    data.setCurrentHero(heroId);
                    LOGGER.debug("Updated client-side hero data to: {}", heroId);
                });
            }
        });
        return true;
    }
}
