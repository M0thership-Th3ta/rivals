package net.anemoia.rivals.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ParticleDataManager extends SimplePreparableReloadListener<Map<ResourceLocation, ParticleDefinition>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticleDataManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FOLDER = "particles";

    private Map<ResourceLocation, ParticleDefinition> particles = new HashMap<>();

    private static final ParticleDataManager INSTANCE = new ParticleDataManager();

    @Override
    protected Map<ResourceLocation, ParticleDefinition> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, ParticleDefinition> particleMap = new HashMap<>();

        Map<ResourceLocation, Resource> resources = resourceManager.listResources(FOLDER,
                location -> location.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                ParticleDefinition particle = GSON.fromJson(reader, ParticleDefinition.class);
                if (particle != null) {
                    String path = location.getPath();
                    path = path.substring(FOLDER.length() + 1, path.length() - 5);
                    ResourceLocation particleId = new ResourceLocation(location.getNamespace(), path);
                    particleMap.put(particleId, particle);
                    LOGGER.info("Loaded particle: {}", particleId);
                }
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.error("Failed to load particle from {}", location, e);
            }
        }

        return particleMap;
    }

    @Override
    protected void apply(Map<ResourceLocation, ParticleDefinition> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.particles.clear();
        this.particles.putAll(prepared);
        LOGGER.info("Loaded {} particles", particles.size());
    }

    public ParticleDefinition getParticle(ResourceLocation id) {
        return particles.get(id);
    }

    public Map<ResourceLocation, ParticleDefinition> getAllParticles() {
        return particles;
    }

    public static ParticleDataManager getInstance() {
        return INSTANCE;
    }
}

