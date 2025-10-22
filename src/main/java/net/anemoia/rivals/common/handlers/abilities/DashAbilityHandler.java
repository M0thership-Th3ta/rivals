package net.anemoia.rivals.common.handlers.abilities;

import net.anemoia.rivals.common.data.Hero;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DashAbilityHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashAbilityHandler.class);

    public static void handleDashAbility(Player player, Map<String, Object> abilityData) {
        if (abilityData == null) return;

        LOGGER.info("Executing dash ability for player {} with attributes: {}",
                player.getName().getString(), abilityData);

        // For abilities in chains, charges are already consumed at the top level
        // Just validate we have the required resource cost but don't consume it again
        Object resourceCostObj = abilityData.get("resource_cost");
        if (resourceCostObj != null) {
            int resourceCost = ((Number) resourceCostObj).intValue();
            Hero.Ability originalAbility = CustomAbilityHandler.getCurrentAbilityContext(player);

            if (originalAbility != null) {
                String originalAbilityName = originalAbility.getAbilityName();
                int availableCharges = ResourceAbilityHandler.getCharges(player, originalAbilityName);

                // This is just a safety check - charges should already be consumed
                if (availableCharges < 0) {
                    LOGGER.warn("Insufficient charges for dash ability {} (cost: {}, available: {})",
                            originalAbilityName, resourceCost, availableCharges);
                    return;
                }
            }
        }

        String dashType = (String) abilityData.get("dash_type");
        Number dashDistanceObj = (Number) abilityData.get("dash_distance");

        if (dashType == null || dashDistanceObj == null) {
            LOGGER.warn("Dash ability missing dash_type or dash_distance for player {}",
                    player.getName().getString());
            return;
        }

        double dashDistance = dashDistanceObj.doubleValue();

        switch (dashType) {
            case "blink":
                performBlinkDash(player, dashDistance);
                break;
            default:
                LOGGER.warn("Unknown dash type: {} for player {}", dashType, player.getName().getString());
        }
    }

    public static void handleDashAbility(Player player, Hero.Ability ability) {
        Map<String, Object> attributes = ability.getAbility().getAbilityAttributes();
        handleDashAbility(player, attributes);
    }

    private static void performBlinkDash(Player player, double distance) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            LOGGER.warn("Cannot perform blink dash on client side for player {}", player.getName().getString());
            return;
        }

        // Get player's look direction and current position
        Vec3 lookDirection = player.getLookAngle();
        Vec3 startPos = player.position();
        Vec3 targetPos = startPos.add(lookDirection.scale(distance));

        // Use eye position for raycast start to better handle looking angles
        Vec3 rayStart = player.getEyePosition();
        Vec3 rayEnd = rayStart.add(lookDirection.scale(distance));

        ClipContext clipContext = new ClipContext(
                rayStart,
                rayEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );

        BlockHitResult hitResult = player.level().clip(clipContext);
        Vec3 finalPos;

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            // Hit an obstacle, calculate distance to hit point
            Vec3 hitPos = hitResult.getLocation();
            Vec3 directionToHit = hitPos.subtract(rayStart);
            double distanceToHit = directionToHit.length();

            // Move back slightly from the collision point (0.5 blocks)
            Vec3 adjustedDirection = directionToHit.normalize().scale(Math.max(0, distanceToHit - 0.5));
            Vec3 teleportPos = rayStart.add(adjustedDirection);

            // Adjust Y to be at player's feet level
            finalPos = new Vec3(teleportPos.x, startPos.y, teleportPos.z);

            LOGGER.debug("Blink dash hit obstacle at {}, teleporting to {}", hitPos, finalPos);
        } else {
            // No obstacles, use target position
            finalPos = targetPos;
            LOGGER.debug("Blink dash clear path, teleporting to {}", finalPos);
        }

        // Check if destination has enough space for player (2 blocks high)
        Vec3 adjustedPos = findClearPosition(player.level(), finalPos, player);

        // Teleport the player
        serverPlayer.teleportTo(adjustedPos.x, adjustedPos.y, adjustedPos.z);

        LOGGER.debug("Successfully blinked player {} from {} to {}",
                player.getName().getString(), startPos, adjustedPos);
    }

    private static Vec3 findClearPosition(Level level, Vec3 targetPos, Player player) {
        BlockPos blockPos = BlockPos.containing(targetPos);

        // Check if there's enough space at the target position (2 blocks high)
        if (hasPlayerSpace(level, blockPos)) {
            return targetPos;
        }

        // Try positions above if the target is obstructed
        for (int yOffset = 1; yOffset <= 3; yOffset++) {
            BlockPos testPos = blockPos.above(yOffset);
            if (hasPlayerSpace(level, testPos)) {
                return new Vec3(targetPos.x, testPos.getY(), targetPos.z);
            }
        }

        // If no clear space above, return original position (better than getting stuck)
        return targetPos;
    }

    private static boolean hasPlayerSpace(Level level, BlockPos pos) {
        // Check if both feet and head positions are clear
        BlockState feetBlock = level.getBlockState(pos);
        BlockState headBlock = level.getBlockState(pos.above());

        return feetBlock.isAir() && headBlock.isAir();
    }
}




