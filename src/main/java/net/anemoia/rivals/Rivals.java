package net.anemoia.rivals;

import com.mojang.logging.LogUtils;
import net.anemoia.rivals.client.RivalsKeybinds;
import net.anemoia.rivals.common.commands.HeroCommand;
import net.anemoia.rivals.common.data.HeroDataManager;
import net.anemoia.rivals.common.network.NetworkHandler;
import net.anemoia.rivals.core.registry.ModCreativeModeTabs;
import net.anemoia.rivals.core.registry.ModEntities;
import net.anemoia.rivals.core.registry.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(value = Rivals.MOD_ID)
@Mod.EventBusSubscriber(modid = Rivals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Rivals {
    public static final String MOD_ID = "rivals";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Rivals(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModEntities.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.init();
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(HeroDataManager.getInstance());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        HeroCommand.register(event.getDispatcher());
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(RivalsKeybinds.PRIMARY_ABILITY);
            event.register(RivalsKeybinds.SECONDARY_ABILITY);
            event.register(RivalsKeybinds.TERTIARY_ABILITY);
        }
    }
}
