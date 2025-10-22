package net.anemoia.rivals.client;

import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CameraLockManager {
    private static boolean cameraLocked = false;

    public static void setCameraLocked(boolean locked) {
        cameraLocked = locked;
    }

    public static boolean isCameraLocked() {
        return cameraLocked;
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (cameraLocked) {
            // Block F5 (key code 292 for F5 in Minecraft)
            if (event.getKey() == 292) {
                event.setCanceled(true);
            }
        }
    }
}