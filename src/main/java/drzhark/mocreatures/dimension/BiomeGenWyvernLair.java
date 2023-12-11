package drzhark.mocreatures.dimension;

import java.util.List;
import java.util.Random;

import drzhark.mocreatures.MoCProxy;
import drzhark.mocreatures.entity.ambient.*;
import drzhark.mocreatures.entity.aquatic.*;
import drzhark.mocreatures.entity.item.MoCEntityEgg;
import drzhark.mocreatures.entity.item.MoCEntityFishBowl;
import drzhark.mocreatures.entity.item.MoCEntityKittyBed;
import drzhark.mocreatures.entity.item.MoCEntityLitterBox;
import drzhark.mocreatures.entity.monster.*;
import drzhark.mocreatures.entity.passive.*;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenShrub;
import net.minecraft.world.gen.feature.WorldGenVines;
import net.minecraft.world.gen.feature.WorldGenerator;
import drzhark.mocreatures.MoCreatures;

public class BiomeGenWyvernLair extends BiomeGenBase
{
    private final MoCWorldGenBigTree wyvernGenBigTree;
    private final WorldGenShrub worldGenShrub;
    public BiomeGenWyvernLair(int par1)
    {
        super(par1);
        spawnableCreatureList.clear();
        spawnableMonsterList.clear();
        spawnableWaterCreatureList.clear();
        // todo add modded entities in the config
        if(MoCProxy.SnakeSpawn){
        spawnableCreatureList.add(new SpawnListEntry(MoCEntitySnake.class, MoCProxy.SnakeSpawnWeight, MoCProxy.SnakeMinChunk, MoCProxy.SnakeMaxChunk));
        }
        if(MoCProxy.WyvernSpawn){
        spawnableCreatureList.add(new SpawnListEntry(MoCEntityWyvern.class, MoCProxy.WyvernSpawnWeight, MoCProxy.WyvernMinChunk, MoCProxy.WyvernMaxChunk));
        }
        if(MoCProxy.BunnySpawn){
        spawnableCreatureList.add(new SpawnListEntry(MoCEntityBunny.class, MoCProxy.BunnySpawnWeight, MoCProxy.BunnyMinChunk, MoCProxy.BunnyMaxChunk));
        }
        spawnableCaveCreatureList.add(new SpawnListEntry(EntityBat.class, 4, 1, 4));
        topBlock = MoCreatures.mocGrass;
        fillerBlock = MoCreatures.mocDirt;
        this.rootHeight = 0.3F;
        this.heightVariation = 1.5F;
        this.biomeName = "WyvernBiome";
        this.wyvernGenBigTree = new MoCWorldGenBigTree(false, MoCreatures.mocLog, 0, MoCreatures.mocLeaf, 0, 2, 30, 10);
        this.worldGenShrub = new WorldGenShrub(3, 0);
        this.theBiomeDecorator = new BiomeWyvernDecorator();
    }

    /**
     * Gets a WorldGen appropriate for this biome.
     */
    @Override
    public WorldGenAbstractTree func_150567_a(Random par1Random)
    {
        if (par1Random.nextInt(10) == 0)
        {
            return this.wyvernGenBigTree;
        }
        else
        {
            return this.worldGenShrub;
        }
    }

    /**
     * Gets a WorldGen appropriate for this biome.
     */
    @Override
    public WorldGenerator getRandomWorldGenForGrass(Random par1Random)
    {
        return new WorldGenWyvernGrass(MoCreatures.mocTallGrass, 0);
    }

    @Override
    public void decorate(World par1World, Random par2Random, int par3, int par4)
    {
        super.decorate(par1World, par2Random, par3, par4);

        WorldGenVines var5 = new WorldGenVines();

        for (int var6 = 0; var6 < 50; ++var6)
        {
            int var7 = par3 + par2Random.nextInt(16) + 8;
            byte var8 = 64;
            int var9 = par4 + par2Random.nextInt(16) + 8;
            var5.generate(par1World, par2Random, var7, var8, var9);
        }
    }
}
