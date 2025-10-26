package net.anemoia.rivals.common.capabilities;

import net.anemoia.rivals.common.network.NetworkHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PlayerHeroDataCapability implements INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerHeroDataCapability.class);
    public static final Capability<PlayerHeroDataCapability> PLAYER_HERO_DATA = CapabilityManager.get(new CapabilityToken<>() {});

    private ResourceLocation currentHero;
    private Set<String> activeAbilities = new HashSet<>();

    private Player owner;

    public void setOwner(Player owner) {
        this.owner = Objects.requireNonNull(owner);
        LOGGER.info("PlayerHeroDataCapability owner set to: {} (serverSide={})", owner.getUUID(), owner.level() != null && !owner.level().isClientSide);
    }

    public Set<String> getActiveAbilities() {
        return activeAbilities;
    }

    public void addActiveAbility(String abilityName) {
        activeAbilities.add(abilityName);
        LOGGER.debug("Added active ability: {}", abilityName);
    }

    public void removeActiveAbility(String abilityName) {
        activeAbilities.remove(abilityName);
        LOGGER.debug("Removed active ability: {}", abilityName);
    }

    public void clearActiveAbilities() {
        LOGGER.debug("Clearing {} active abilities", activeAbilities.size());
        activeAbilities.clear();
    }

    public ResourceLocation getCurrentHero() {
        return currentHero;
    }

    public void setCurrentHero(ResourceLocation hero) {
        LOGGER.debug("setCurrentHero called: {} -> {} (owner present={}, ownerServerSide={})", this.currentHero, hero, owner != null, owner != null && owner.level() != null && !owner.level().isClientSide);
        this.currentHero = hero;

        if (owner == null) {
            LOGGER.warn("Not sending hero update because capability owner is null");
            return;
        }

        if (owner.level() == null) {
            LOGGER.warn("Not sending hero update because owner.level() is null for owner {}", owner.getUUID());
            return;
        }

        if (owner.level().isClientSide) {
            LOGGER.info("setCurrentHero running on client for owner {}, not sending S2C", owner.getUUID());
            return;
        }

        if (owner instanceof ServerPlayer serverPlayer) {
            LOGGER.info("Sending hero update for player {} -> {}", serverPlayer.getUUID(), hero);
            NetworkHandler.sendHeroUpdate(serverPlayer, hero);
        } else {
            LOGGER.warn("Owner is not a ServerPlayer ({}) — cannot send hero update", owner.getClass().getName());
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        if (currentHero != null) {
            tag.putString("current_hero", currentHero.toString());
            LOGGER.debug("Serializing hero data: {}", currentHero);
        } else {
            LOGGER.debug("Serializing with no current hero");
        }

        // Serialize active abilities
        if (!activeAbilities.isEmpty()) {
            CompoundTag abilitiesTag = new CompoundTag();
            int i = 0;
            for (String ability : activeAbilities) {
                abilitiesTag.putString("ability_" + i, ability);
                i++;
            }
            abilitiesTag.putInt("count", i);
            tag.put("active_abilities", abilitiesTag);
            LOGGER.debug("Serialized {} active abilities", i);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("current_hero")) {
            String heroString = nbt.getString("current_hero");
            this.currentHero = new ResourceLocation(heroString);
            LOGGER.debug("Deserialized hero data: {}", this.currentHero);
        } else {
            this.currentHero = null;
            LOGGER.debug("Deserialized with no current hero");
        }

        // Deserialize active abilities
        this.activeAbilities.clear();
        if (nbt.contains("active_abilities")) {
            CompoundTag abilitiesTag = nbt.getCompound("active_abilities");
            int count = abilitiesTag.getInt("count");
            for (int i = 0; i < count; i++) {
                if (abilitiesTag.contains("ability_" + i)) {
                    this.activeAbilities.add(abilitiesTag.getString("ability_" + i));
                }
            }
            LOGGER.debug("Deserialized {} active abilities", count);
        }
    }

    public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private static final Logger LOGGER = LoggerFactory.getLogger(Provider.class);
        private final PlayerHeroDataCapability data = new PlayerHeroDataCapability();
        private final LazyOptional<PlayerHeroDataCapability> optional = LazyOptional.of(() -> data);

        public Provider() {}

        public Provider(Player owner) {
            setOwner(owner);
        }

        public void setOwner(Player owner) {
            data.setOwner(owner);
            LOGGER.debug("Provider set owner for capability to: {}", owner.getUUID());
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == PLAYER_HERO_DATA ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            LOGGER.debug("Provider serializing NBT data");
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            LOGGER.debug("Provider deserializing NBT data");
            data.deserializeNBT(nbt);
        }
    }
}
