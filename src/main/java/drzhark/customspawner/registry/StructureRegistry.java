package drzhark.customspawner.registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraftforge.event.terraingen.InitMapGenEvent.EventType;
import drzhark.customspawner.configuration.CMSConfigCategory;
import drzhark.customspawner.configuration.CMSProperty;
import drzhark.customspawner.configuration.CMSProperty.Type;
import drzhark.customspawner.entity.EntityData;
import drzhark.customspawner.environment.EnvironmentSettings;
import drzhark.customspawner.utils.CMSUtils;

public class StructureRegistry {


    public StructureRegistry()
    {
    }

    public void registerStructure(EnvironmentSettings environment, EventType type, MapGenBase base)
    {
        if (type != null && base != null)
        {
            String structKey = "";
            switch (type) {
            case NETHER_BRIDGE :
            {
                structKey = type.name();
                addStructToConfig(environment, base, structKey);
                break;
            }
            case SCATTERED_FEATURE : // handle witchhut
            {
                structKey = type.name();
                addStructToConfig(environment, base, structKey);
                break;
            }
            default:
                break;
            }
        }
    }

    public void addStructToConfig(EnvironmentSettings environment, MapGenBase base, String structKey) {
        List<SpawnListEntry> spawnList = null;
        String structCategoryName = "";

        if (structKey.equalsIgnoreCase("NETHER_BRIDGE") && base instanceof MapGenNetherBridge) {
            structCategoryName = "netherbridge";
            spawnList = ((MapGenNetherBridge) base).getSpawnList();
        } else if (structKey.equalsIgnoreCase("SCATTERED_FEATURE") && base instanceof MapGenScatteredFeature) {
            structCategoryName = "witchhut";
            spawnList = ((MapGenScatteredFeature) base).getScatteredFeatureSpawnList();
        }

        environment.CMSStructureConfig.load();

        if (spawnList != null) {
            if (!environment.CMSStructureConfig.hasCategory(structCategoryName)) {
                createDefaultSpawnList(environment, structCategoryName, spawnList);
            } else {
                updateSpawnListFromConfig(environment, structCategoryName, spawnList);
            }
        }
    }

    private void createDefaultSpawnList(EnvironmentSettings environment, String structCategoryName, List<SpawnListEntry> spawnList) {
        if (environment.debug) {
            environment.envLog.logger.info("Creating new category for STRUCTURE " + structCategoryName +
                " in environment " + environment.name());
        }

        CMSConfigCategory spawnListCat = environment.CMSStructureConfig.getCategory("spawnlist");
        spawnListCat.setComment("To add entities to a specific structure, add the entity in the format of TAG|ENTITYNAME to list."
            + "\n" + "Example: <MC|Witch:MOC|Horse:MC|Sheep");
        CMSConfigCategory structCat = environment.CMSStructureConfig.getCategory(structCategoryName);
        CMSConfigCategory spawnEntryCat = new CMSConfigCategory("spawnentries", structCat);
        spawnListCat.put(structCategoryName, new CMSProperty(structCategoryName, new ArrayList<>(), Type.STRING));
        CMSProperty spawnListProp = spawnListCat.get(structCategoryName);

        for (SpawnListEntry spawnListEntry : spawnList) {
            EntityData entityData = getOrCreateEntityData(environment, spawnListEntry.entityClass);

            if (isValidSpawnEntry(entityData)) {
                String entityName = entityData.getEntityMod().getModTag() + "|" + entityData.getEntityName();
                CMSConfigCategory entityCategory = new CMSConfigCategory(entityName, spawnEntryCat);
                entityCategory.put("frequency", new CMSProperty("frequency", Integer.toString(spawnListEntry.itemWeight), CMSProperty.Type.INTEGER));
                entityCategory.put("minSpawn", new CMSProperty("minSpawn", Integer.toString(spawnListEntry.minGroupCount), CMSProperty.Type.INTEGER));
                entityCategory.put("maxSpawn", new CMSProperty("maxSpawn", Integer.toString(spawnListEntry.maxGroupCount), CMSProperty.Type.INTEGER));

                if (environment.debug) {
                    environment.envLog.logger.info("Adding default spawnentry " + entityData.getEntityName() +
                        " to STRUCTURE " + structCategoryName + " with frequency " + spawnListEntry.itemWeight +
                        ", minSpawn " + spawnListEntry.minGroupCount + ", maxSpawn " + spawnListEntry.maxGroupCount);
                }

                spawnListProp.valueList.add(entityName);
            }
        }

        environment.CMSStructureConfig.save();
    }

    private EntityData getOrCreateEntityData(EnvironmentSettings environment, Class<?> entityClass) {
        EntityData entityData = environment.classToEntityMapping.get(entityClass);

        if (entityData == null) {
            entityData = environment.registerEntity((Class<? extends EntityLiving>) entityClass);
        }

        return entityData;
    }

    private boolean isValidSpawnEntry(EntityData entityData) {
        return entityData != null &&
            entityData.getCanSpawn() &&
            entityData.getFrequency() > 0 &&
            entityData.getMaxSpawn() > 0 &&
            entityData.getMaxInChunk() > 0;
    }

    private void updateSpawnListFromConfig(EnvironmentSettings environment, String structCategoryName, List<SpawnListEntry> spawnList) {
        CMSConfigCategory spawnListCat = environment.CMSStructureConfig.getCategory("spawnlist");
        CMSConfigCategory structCat = environment.CMSStructureConfig.getCategory(structCategoryName);
        environment.initializeEntities();
        CMSProperty spawnListProperty = spawnListCat.get(structCategoryName);

        if (spawnListProperty != null) {
            CMSConfigCategory spawnEntries = environment.CMSStructureConfig.getCategory(structCategoryName + ".spawnentries");

            if (spawnListProperty.valueList.isEmpty()) {
                removeAllSpawnEntries(spawnEntries);
            } else {
                removeObsoleteSpawnEntries(environment, structCategoryName, spawnListProperty, spawnEntries);
            }

            updateSpawnListFromConfigEntries(environment, structCategoryName, spawnList, spawnListProperty);
        }

        environment.CMSStructureConfig.save();
    }

    private void removeAllSpawnEntries(CMSConfigCategory spawnEntries) {
        for (CMSConfigCategory cat : spawnEntries.getChildren()) {
            spawnEntries.removeChild(cat);
        }
    }

    private void removeObsoleteSpawnEntries(EnvironmentSettings environment, String structCategoryName, CMSProperty spawnListProperty, CMSConfigCategory spawnEntries) {
        Iterator<String> iterator = spawnEntries.keySet().iterator();
        while (iterator.hasNext()) {
            String entryName = iterator.next();
            if (!spawnListProperty.valueList.contains(entryName)) {
                if (environment.debug) {
                    environment.envLog.logger.info("SpawnList " + structCategoryName + " does NOT contain spawn entry " +
                        entryName + ", REMOVING!!");
                }
                iterator.remove();
            }
        }
    }

    private void updateSpawnListFromConfigEntries(EnvironmentSettings environment, String structCategoryName, List<SpawnListEntry> spawnList, CMSProperty spawnListProperty) {
        for (String entryName : spawnListProperty.valueList) {
            EntityData entityData = environment.entityMap.get(entryName);

            if (entityData != null) {
                CMSConfigCategory spawnEntryCat = getOrCreateSpawnEntryCategory(environment, structCategoryName, entryName);
                updateSpawnEntryFromConfig(environment, structCategoryName, spawnEntryCat, entityData, spawnList);
            }
        }
    }

    private CMSConfigCategory getOrCreateSpawnEntryCategory(EnvironmentSettings environment, String structCategoryName, String entryName) {
        CMSConfigCategory spawnEntries = environment.CMSStructureConfig.getCategory(structCategoryName + ".spawnentries");
        CMSConfigCategory spawnEntryCat;

        if (!environment.CMSStructureConfig.hasCategory(structCategoryName + ".spawnentries." + entryName)) {
            spawnEntryCat = environment.CMSStructureConfig.getCategory(structCategoryName + ".spawnentries." + entryName);
            spawnEntryCat.put("frequency", new CMSProperty("frequency", "0", CMSProperty.Type.INTEGER));
            spawnEntryCat.put("minSpawn", new CMSProperty("minSpawn", "0", CMSProperty.Type.INTEGER));
            spawnEntryCat.put("maxSpawn", new CMSProperty("maxSpawn", "0", CMSProperty.Type.INTEGER));
        } else {
            spawnEntryCat = environment.CMSStructureConfig.getCategory(structCategoryName + ".spawnentries." + entryName);
        }

        return spawnEntryCat;
    }

    private void updateSpawnEntryFromConfig(EnvironmentSettings environment, String structCategoryName, CMSConfigCategory spawnEntryCat, EntityData entityData, List<SpawnListEntry> spawnList) {
        int frequency = entityData.getFrequency();
        int minSpawn = entityData.getMinSpawn();
        int maxSpawn = entityData.getMaxSpawn();

        for (Map.Entry<String, CMSProperty> entityEntry : spawnEntryCat.entrySet()) {
            switch (entityEntry.getKey()) {
                case "frequency":
                    frequency = Integer.parseInt(entityEntry.getValue().value);
                    break;
                case "minSpawn":
                    minSpawn = Integer.parseInt(entityEntry.getValue().value);
                    break;
                case "maxSpawn":
                    maxSpawn = Integer.parseInt(entityEntry.getValue().value);
                    break;
            }
        }

        SpawnListEntry spawnListEntry = new SpawnListEntry(entityData.getEntityClass(), frequency, minSpawn, maxSpawn);

        if (CMSUtils.getSpawnListEntry(entityData.getEntityClass(), spawnList) == null) {
            if (environment.debug) {
                environment.envLog.logger.info("Adding spawnentry " + entityData.getEntityName() + " to STRUCTURE " +
                    structCategoryName + " with frequency " + frequency + ", minSpawn " + minSpawn +
                    ", maxSpawn " + maxSpawn);
            }
            spawnList.add(new SpawnListEntry(entityData.getEntityClass(), frequency, minSpawn, maxSpawn));
        } else {
            if (environment.debug) {
                environment.envLog.logger.info("Updating existing entity in " + structCategoryName +
                    " with settings " + spawnListEntry.itemWeight + ":" + spawnListEntry.minGroupCount +
                    ":" + spawnListEntry.maxGroupCount);
            }
            SpawnListEntry existingSpawnEntry = CMSUtils.getSpawnListEntry(entityData.getEntityClass(), spawnList);
            assert existingSpawnEntry != null;
            existingSpawnEntry.itemWeight = spawnListEntry.itemWeight;
            existingSpawnEntry.minGroupCount = spawnListEntry.minGroupCount;
            existingSpawnEntry.maxGroupCount = spawnListEntry.maxGroupCount;
        }
    }
}
