package net.anemoia.rivals.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class TestMixin {

    private Player self() {
        return (Player) (Object) this;
    }

    @Inject(method = "touch", at = @At("HEAD"))
    private void touch(Entity entity, CallbackInfo ci) {
        self().displayClientMessage(Component.literal("Touched entity: " + entity.getName().getString()).withStyle(ChatFormatting.GOLD), true);
    }
}
