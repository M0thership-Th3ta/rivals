package net.anemoia.rivals.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class RivalsKeybinds {
    public static final String CATEGORY = "key.categories.rivals";

    public static final KeyMapping PRIMARY_ABILITY = new KeyMapping(
            "key.rivals.primary",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            CATEGORY
    );

    public static final KeyMapping SECONDARY_ABILITY = new KeyMapping(
            "key.rivals.secondary",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_Z,
            CATEGORY
    );

    public static final KeyMapping TERTIARY_ABILITY = new KeyMapping(
            "key.rivals.tertiary",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            CATEGORY
    );
}
