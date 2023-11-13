package drzhark.customspawner.utils;


import drzhark.customspawner.environment.EnvironmentSettings;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CMSLog {

    public final Logger logger;

    public CMSLog(String environment) {
        this.logger = LogManager.getLogger(environment);
        this.logger.info("Logger initialized for environment " + environment);
    }

    public void logSpawn(EnvironmentSettings environment, String entitySpawnType, String biomeName, String entityName, int x, int y, int z, int moblimit, SpawnListEntry spawnlistentry) {
        if (environment.debug)
            logger.info("[" + environment.name() + "]" + "[" + entitySpawnType.toUpperCase() + " TICKHANDLER]:[spawned " + entityName + " at " + x + ", " + y + ", " + z + " with " + entitySpawnType.toUpperCase() + ":" + spawnlistentry.itemWeight + ":" + spawnlistentry.minGroupCount + ":" + spawnlistentry.maxGroupCount + " in biome " + biomeName + "]:[spawns left in limit " + moblimit + "]");
    }
}
