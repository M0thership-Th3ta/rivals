package net.anemoia.rivals.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.anemoia.rivals.common.data.Hero;
import net.anemoia.rivals.common.data.HeroDataManager;
import net.anemoia.rivals.common.handlers.HeroHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class HeroCommand {

    private static final SuggestionProvider<CommandSourceStack> HERO_SUGGESTIONS = (context, builder) -> {
        return SharedSuggestionProvider.suggest(
                HeroDataManager.getInstance().getAllHeroes().keySet().stream()
                        .map(ResourceLocation::toString),
                builder
        );
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hero")
                .then(Commands.literal("apply")
                        .then(Commands.argument("hero_id", StringArgumentType.greedyString())
                                .suggests(HERO_SUGGESTIONS)
                                .executes(context -> applyHero(context, context.getSource().getPlayerOrException()))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> applyHero(context, EntityArgument.getPlayer(context, "target")))
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .executes(context -> removeHero(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> removeHero(context, EntityArgument.getPlayer(context, "target")))
                        )
                )
        );
    }

    private static int applyHero(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
        String heroIdString = StringArgumentType.getString(context, "hero_id");

        try {
            ResourceLocation heroId;

            // Handle both formats: "modid:heroname" and just "heroname"
            if (heroIdString.contains(":")) {
                heroId = new ResourceLocation(heroIdString);
            } else {
                heroId = new ResourceLocation("rivals", heroIdString);
            }

            Hero hero = HeroDataManager.getInstance().getHero(heroId);

            if (hero == null) {
                context.getSource().sendFailure(Component.literal("Unknown hero: " + heroIdString));
                return 0;
            }

            // Remove any existing hero first to ensure clean state
            HeroHandler.removeHero(player);

            // Apply the new hero
            HeroHandler.applyHero(player, heroId);

            context.getSource().sendSuccess(() ->
                            Component.literal("Applied hero " + hero.getName() + " to " + player.getName().getString()),
                    true
            );

            return 1;
        } catch (IllegalArgumentException e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Invalid resource location format";
            context.getSource().sendFailure(Component.literal("Invalid hero ID format: " + errorMsg));
            return 0;
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            context.getSource().sendFailure(Component.literal("Failed to apply hero: " + errorMsg));
            // Log the full stack trace for debugging
            e.printStackTrace();
            return 0;
        }
    }

    private static int removeHero(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        try {
            HeroHandler.removeHero(player);

            context.getSource().sendSuccess(() ->
                            Component.literal("Removed hero from " + player.getName().getString()),
                    true
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to remove hero: " + e.getMessage()));
            return 0;
        }
    }
}
