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

public class HeroDataManager extends SimplePreparableReloadListener<Map<ResourceLocation, Hero>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeroDataManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FOLDER = "heroes";

    private Map<ResourceLocation, Hero> heroes = new HashMap<>();

    private static final HeroDataManager INSTANCE = new HeroDataManager();

    @Override
    protected Map<ResourceLocation, Hero> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, Hero> heroMap = new HashMap<>();

        Map<ResourceLocation, Resource> resources = resourceManager.listResources(FOLDER,
                location -> location.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
           ResourceLocation location = entry.getKey();
           Resource resource = entry.getValue();

           try (InputStreamReader reader = new InputStreamReader(resource.open())) {
               Hero hero = GSON.fromJson(reader, Hero.class);
               if (hero != null) {
                   String path = location.getPath();
                   path = path.substring(FOLDER.length() + 1, path.length() - 5);
                   ResourceLocation heroId = new ResourceLocation(location.getNamespace(), path);
                   heroMap.put(heroId, hero);
                   LOGGER.info("Loaded hero: {}", heroId);
               }
           } catch (IOException | JsonSyntaxException e) {
               LOGGER.error("Failed to load hero from {}", location, e);
           }
        }

        return heroMap;
    }

    @Override
    protected void apply(Map<ResourceLocation, Hero> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.heroes.clear();
        this.heroes.putAll(prepared);
        LOGGER.info("Loaded {} heroes", heroes.size());
    }

    public Hero getHero(ResourceLocation id) {
        return heroes.get(id);
    }

    public Map<ResourceLocation, Hero> getAllHeroes() {
        return heroes;
    }

    public static HeroDataManager getInstance() {
        return INSTANCE;
    }
}
