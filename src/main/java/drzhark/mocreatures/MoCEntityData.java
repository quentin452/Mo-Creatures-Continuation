package drzhark.mocreatures;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drzhark.guiapi.widget.WidgetSimplewindow;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraftforge.common.BiomeDictionary.Type;

public class MoCEntityData {

    private EnumCreatureType typeOfCreature;
    private SpawnListEntry spawnListEntry;
    private String entityName;
    private boolean canSpawn = true;
    private int entityId;
    private final List<Type> biomeTypes;
    @SideOnly(Side.CLIENT)
    private WidgetSimplewindow entityWindow;

    private int frequency;
    private int minGroup;
    private int maxGroup;
    private int maxSpawnInChunk;

    public MoCEntityData(String name,  int maxchunk, EnumCreatureType type, SpawnListEntry spawnListEntry, List<Type> biomeTypes)
    {
        this.entityName = name;
        this.typeOfCreature = type;
        this.biomeTypes = biomeTypes;
        this.frequency = spawnListEntry.itemWeight;
        this.minGroup = spawnListEntry.minGroupCount;
        this.maxGroup = spawnListEntry.maxGroupCount;
        this.maxSpawnInChunk = maxchunk;
        this.spawnListEntry = spawnListEntry;
        MoCreatures.entityMap.put(spawnListEntry.entityClass, this);
    }

    public MoCEntityData(String name, int id, int maxchunk, EnumCreatureType type, SpawnListEntry spawnListEntry, List<Type> biomeTypes)
    {
        this.entityId = id;
        this.entityName = name;
        this.typeOfCreature = type;
        this.biomeTypes = biomeTypes;
        this.frequency = spawnListEntry.itemWeight;
        this.minGroup = spawnListEntry.minGroupCount;
        this.maxGroup = spawnListEntry.maxGroupCount;
        this.maxSpawnInChunk = maxchunk;
        this.spawnListEntry = spawnListEntry;
        MoCreatures.entityMap.put(spawnListEntry.entityClass, this);
    }

    public Class<? extends EntityLiving> getEntityClass()
    {
        return this.spawnListEntry.entityClass;
    }

    public EnumCreatureType getType()
    {
        if (this.typeOfCreature != null)
            return this.typeOfCreature;
        return null;
    }

    public void setType(EnumCreatureType type)
    {
        this.typeOfCreature = type;
    }

    public List<Type> getBiomeTypes()
    {
        return this.biomeTypes;
    }

    public int getEntityID()
    {
        return this.entityId;
    }

    public void setEntityID(int id)
    {
        this.entityId = id;
    }

    public int getFrequency()
    {
        return this.frequency;
    }

    public void setFrequency(int freq)
    {
        this.frequency = Math.max(freq, 0);
    }

    public int getMinSpawn()
    {
        return this.minGroup;
    }

    public void setMinSpawn(int min)
    {
        this.minGroup = Math.max(min, 0);
    }

    public int getMaxSpawn()
    {
        return this.maxGroup;
    }

    public void setMaxSpawn(int max)
    {
        this.maxGroup = Math.max(max, 0);
    }

    public int getMaxInChunk()
    {
        return this.maxSpawnInChunk;
    }

    public void setMaxInChunk(int max)
    {
        this.maxSpawnInChunk = Math.max(max, 0);
    }

    public String getEntityName()
    {
        return this.entityName;
    }

    public void setEntityName(String name)
    {
        this.entityName = name;
    }

    public void setCanSpawn(boolean flag)
    {
        this.canSpawn = flag;
    }

    public boolean getCanSpawn()
    {
        return this.canSpawn;
    }

    public SpawnListEntry getSpawnListEntry()
    {
        return this.spawnListEntry;
    }

    @SideOnly(Side.CLIENT)
    public WidgetSimplewindow getEntityWindow()
    {
        return this.entityWindow;
    }

    @SideOnly(Side.CLIENT)
    public void setEntityWindow(WidgetSimplewindow window)
    {
        this.entityWindow = window;
    }
}
