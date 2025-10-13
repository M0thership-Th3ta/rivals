package net.anemoia.rivals.common.capabilities;

import net.anemoia.rivals.Rivals;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = Rivals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RivalsCapabilityEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(RivalsCapabilityEvents.class);

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        LOGGER.info("Registering PlayerHeroDataCapability");
        event.register(PlayerHeroDataCapability.class);
    }
}
