package drzhark.mocreatures;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import drzhark.mocreatures.configuration.MoCConfigCategory;
import drzhark.mocreatures.configuration.MoCConfiguration;
import drzhark.mocreatures.configuration.MoCProperty;
import drzhark.mocreatures.entity.IMoCEntity;
import drzhark.mocreatures.entity.monster.MoCEntityGolem;
import drzhark.mocreatures.entity.passive.MoCEntityHorse;
import drzhark.mocreatures.utils.MoCLog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class MoCProxy implements IGuiHandler {

    public static String ARMOR_TEXTURE = "textures/armor/";
    public static String BLOCK_TEXTURE = "textures/blocks/";
    public static String ITEM_TEXTURE = "textures/items/";
    public static String MODEL_TEXTURE = "textures/models/";
    public static String GUI_TEXTURE = "textures/gui/";
    public static String MISC_TEXTURE = "textures/misc/";

    //CONFIG VARIABLES
    // Client Only
    public boolean displayPetHealth;
    public boolean displayPetName;
    public boolean displayPetIcons;
    public boolean animateTextures;

    public boolean attackDolphins;
    public boolean attackWolves;
    public boolean attackHorses;
    public boolean staticBed;
    public boolean staticLitter;

    public boolean easyBreeding;
    public boolean destroyDrops;
    public boolean enableOwnership;
    public boolean enableResetOwnership;
    public boolean elephantBulldozer;
    public boolean killallVillagers;

    // griefing options
    public boolean golemDestroyBlocks;

    public int itemID;
    //new blocks IDs
    public int blockDirtID;
    public int blockGrassID;
    public int blockStoneID;
    public int blockLeafID;
    public int blockLogID;
    public int blockTallGrassID;
    public int blockPlanksID;
    public int WyvernDimension;
    public int WyvernBiomeID;

    public int maxTamed;
    public int maxOPTamed;
    public int zebraChance;
    public int ostrichEggDropChance;
    public int rareItemDropChance;
    public int wyvernEggDropChance;
    public int motherWyvernEggDropChance;
    public int particleFX;
    // defaults
    public int frequency = 6;
    public int minGroup = 1;
    public int maxGroup = 2;
    public int maxSpawnInChunk = 3;
    public float strength = 1;
    public int minDespawnLightLevel = 2;
    public int maxDespawnLightLevel = 7;

    // ogre settings
    public float ogreStrength;
    public float caveOgreStrength;
    public float fireOgreStrength;
    public short ogreAttackRange;
    public short fireOgreChance;
    public short caveOgreChance;

    public boolean debug = false;
    public boolean allowInstaSpawn;
    public boolean needsUpdate = false;
    public boolean worldInitDone = false;
    public boolean forceDespawns = false;
    public boolean enableHunters = false;
    public int activeScreen = -1;

    public MoCConfiguration mocSettingsConfig;
    public MoCConfiguration mocEntityConfig;
    protected File configFile;

    protected static final String CATEGORY_MOC_GENERAL_SETTINGS = "global-settings";
    protected static final String CATEGORY_MOC_CREATURE_GENERAL_SETTINGS = "creature-general-settings";
    protected static final String CATEGORY_MOC_MONSTER_GENERAL_SETTINGS = "monster-general-settings";
    protected static final String CATEGORY_MOC_WATER_CREATURE_GENERAL_SETTINGS = "water-mob-general-settings";
    protected static final String CATEGORY_MOC_AMBIENT_GENERAL_SETTINGS = "ambient-general-settings";
    protected static final String CATEGORY_MOC_ID_SETTINGS = "custom-id-settings";
    private static final String CATEGORY_VANILLA_CREATURE_FREQUENCIES = "vanilla-creature-frequencies";
    private static final String CATEGORY_CREATURES = "Creatures";
    private static final String CATEGORY_OWNERSHIP_SETTINGS = "ownership-settings";
    protected static final String CATEGORY_Ant = "Entity-Ant";
    protected static final String CATEGORY_Bee = "Entity-Bee";
    protected static final String CATEGORY_Roach = "Entity-Roach";
    protected static final String CATEGORY_Butterfly = "Entity-Butterfly";
    protected static final String CATEGORY_Crab = "Entity-Crab";
    protected static final String CATEGORY_Cricket = "Entity-Cricket";
    protected static final String CATEGORY_Dragonfly = "Entity-Dragonfly";
    protected static final String CATEGORY_Firefly = "Entity-Firefly";
    protected static final String CATEGORY_Fly = "Entity-Fly";
    protected static final String CATEGORY_Maggot = "Entity-Maggot";
    protected static final String CATEGORY_Snail = "Entity-Snail";
    protected static final String CATEGORY_Bear = "Entity-Bear";
    protected static final String CATEGORY_BigCat = "Entity-BigCat";
    protected static final String CATEGORY_Bird = "Entity-Bird";
    protected static final String CATEGORY_Boar = "Entity-Boar";
    protected static final String CATEGORY_Bunny = "Entity-Bunny";
    protected static final String CATEGORY_Crocodile = "Entity-Crocodile";
    protected static final String CATEGORY_Deer = "Entity-Deer";
    protected static final String CATEGORY_Duck = "Entity-Duck";
    protected static final String CATEGORY_Elephant = "Entity-Elephant";
    protected static final String CATEGORY_Ent = "Entity-Ent";
    protected static final String CATEGORY_Fox = "Entity-Fox";
    protected static final String CATEGORY_Goat = "Entity-Goat";
    protected static final String CATEGORY_Kitty = "Entity-Kitty";
    protected static final String CATEGORY_Komodo = "Entity-Komodo";
    protected static final String CATEGORY_Mole = "Entity-Mole";
    protected static final String CATEGORY_Moose = "Entity-Moose";
    protected static final String CATEGORY_Ostrich = "Entity-Ostrich";
    protected static final String CATEGORY_Raccoon = "Entity-Raccoon";
    protected static final String CATEGORY_Snake = "Entity-Snake";
    protected static final String CATEGORY_Turkey = "Entity-Turkey";
    protected static final String CATEGORY_Turtle = "Entity-Turtle";
    protected static final String CATEGORY_Horse = "Entity-Horse";
    protected static final String CATEGORY_Wyvern = "Entity-Wyvern";

    protected static final String CATEGORY_Dolphin = "Entity-Dolphin";
    protected static final String CATEGORY_Fishy = "Entity-Fishy";
    protected static final String CATEGORY_JellyFish = "Entity-JellyFish";
    protected static final String CATEGORY_MediumFish = "Entity-MediumFish";
    protected static final String CATEGORY_Piranha = "Entity-Piranha";
    protected static final String CATEGORY_Ray = "Entity-Ray";
    protected static final String CATEGORY_Shark = "Entity-Shark";
    protected static final String CATEGORY_SmallFish = "Entity-SmallFish";
    protected static final String CATEGORY_Golem = "Entity-Golem";
    protected static final String CATEGORY_FlameWraith = "Entity-FlameWraith";
    protected static final String CATEGORY_HellRat = "Entity-HellRat";
    protected static final String CATEGORY_HorseMob = "Entity-HorseMob";
    protected static final String CATEGORY_MiniGolem = "Entity-MiniGolem";
    protected static final String CATEGORY_Ogre = "Entity-Ogre";
    protected static final String CATEGORY_Rat = "Entity-Rat";
    protected static final String CATEGORY_Scorpion = "Entity-Scorpion";
    protected static final String CATEGORY_SilverSkeleton = "Entity-SilverSkeleton";
    protected static final String CATEGORY_Werewolf = "Entity-Werewolf";
    protected static final String CATEGORY_Wraith = "Entity-Wraith";
    protected static final String CATEGORY_WWolf = "Entity-WWolf";

    public void resetAllData()
    {
        //registerEntities();
        this.readGlobalConfigValues();
    }

    //----------------CONFIG INITIALIZATION
    public void ConfigInit(FMLPreInitializationEvent event)
    {
        mocSettingsConfig = new MoCConfiguration(new File(event.getSuggestedConfigurationFile().getParent(), MoCreatures.MODID + File.separator + "MoCSettings.cfg"));
        mocEntityConfig = new MoCConfiguration(new File(event.getSuggestedConfigurationFile().getParent(), MoCreatures.MODID + File.separator + "MoCreatures.cfg"));
        configFile = event.getSuggestedConfigurationFile();
        mocSettingsConfig.load();
        mocEntityConfig.load();
        //registerEntities();
        this.readGlobalConfigValues();
        if (debug) MoCLog.logger.info("Initializing MoCreatures Config File at " + event.getSuggestedConfigurationFile().getParent() + "MoCSettings.cfg");
    }

    public int getFrequency(String entityName)//, EnumCreatureType type)
    {
        if (MoCreatures.mocEntityMap.get(entityName) != null)
            return MoCreatures.mocEntityMap.get(entityName).getFrequency();
        else return frequency;
    }

    //-----------------THE FOLLOWING ARE CLIENT SIDE ONLY, NOT TO BE USED IN SERVER AS THEY AFFECT ONLY DISPLAY / SOUNDS

    public void UndeadFX(Entity entity) {} //done client side

    public void StarFX(MoCEntityHorse moCEntityHorse) {}

    public void LavaFX(Entity entity) {}

    public void VanishFX(MoCEntityHorse entity) {}

    public void MaterializeFX(MoCEntityHorse entity) {}

    public void VacuumFX(MoCEntityGolem entity) {}

    public void hammerFX(EntityPlayer entityplayer) {}

    public void teleportFX(EntityPlayer entity) {}

    public boolean getAnimateTextures() {
        return false;
    }

    public boolean getDisplayPetName() {
        return displayPetName;
    }

    public boolean getDisplayPetIcons() {
        return displayPetIcons;
    }

    public boolean getDisplayPetHealth() {
        return displayPetHealth;
    }

    public int getParticleFX() {
        return 0;
    }

    public void initTextures() {}

    public ResourceLocation getTexture(String texture) {
        return null;
    }

    public EntityPlayer getPlayer() {
        return null;
    }

    public void printMessageToPlayer(String msg)
    {
    }

    public List<String> parseName(String biomeConfigEntry)
    {
        String tag = biomeConfigEntry.substring(0, biomeConfigEntry.indexOf('|'));
        String biomeName = biomeConfigEntry.substring(biomeConfigEntry.indexOf('|') + 1);
        List<String> biomeParts = new ArrayList<>();
        biomeParts.add(tag);
        biomeParts.add(biomeName);
        return biomeParts;
    }

    public static int AntSpawnWeight;
    public static int AntMinChunk;
    public static int AntMaxChunk;
    public static boolean AntSpawn;

    public static int BeeSpawnWeight;
    public static int BeeMinChunk;
    public static int BeeMaxChunk;
    public static boolean BeeSpawn;
    public static int RoachSpawnWeight;
    public static int RoachMinChunk;
    public static int RoachMaxChunk;
    public static boolean RoachSpawn;
    public static int ButterFlySpawnWeight;
    public static int ButterFlyMinChunk;
    public static int ButterFlyMaxChunk;
    public static boolean ButterFlySpawn;
    public static int CrabSpawnWeight;
    public static int CrabMinChunk;
    public static int CrabMaxChunk;
    public static boolean CrabSpawn;
    public static int CricketSpawnWeight;
    public static int CricketMinChunk;
    public static int CricketMaxChunk;
    public static boolean CricketSpawn;
    public static int DragonflySpawnWeight;
    public static int DragonflyMinChunk;
    public static int DragonflyMaxChunk;
    public static boolean DragonflySpawn;
    public static int FireflySpawnWeight;
    public static int FireflyMinChunk;
    public static int FireflyMaxChunk;
    public static boolean FireflySpawn;
    public static int MaggotSpawnWeight;
    public static int MaggotMinChunk;
    public static int MaggotMaxChunk;
    public static boolean MaggotSpawn;
    public static int SnailSpawnWeight;
    public static int SnailMinChunk;
    public static int SnailMaxChunk;
    public static boolean SnailSpawn;
    public static int BearSpawnWeight;
    public static int BearMinChunk;
    public static int BearMaxChunk;
    public static boolean BearSpawn;
    public static int BigCatSpawnWeight;
    public static int BigCatMinChunk;
    public static int BigCatMaxChunk;
    public static boolean BigCatSpawn;
    public static int BirdSpawnWeight;
    public static int BirdMinChunk;
    public static int BirdMaxChunk;
    public static boolean BirdSpawn;
    public static int BoarSpawnWeight;
    public static int BoarMinChunk;
    public static int BoarMaxChunk;
    public static boolean BoarSpawn;
    public static int BunnySpawnWeight;
    public static int BunnyMinChunk;
    public static int BunnyMaxChunk;
    public static boolean BunnySpawn;
    public static int CrocodileSpawnWeight;
    public static int CrocodileMinChunk;
    public static int CrocodileMaxChunk;
    public static boolean CrocodileSpawn;
    public static int DeerSpawnWeight;
    public static int DeerMinChunk;
    public static int DeerMaxChunk;
    public static boolean DeerSpawn;
    public static int DuckSpawnWeight;
    public static int DuckMinChunk;
    public static int DuckMaxChunk;
    public static boolean DuckSpawn;
    public static int ElephantSpawnWeight;
    public static int ElephantMinChunk;
    public static int ElephantMaxChunk;
    public static boolean ElephantSpawn;
    public static int EntSpawnWeight;
    public static int EntMinChunk;
    public static int EntMaxChunk;
    public static boolean EntSpawn;
    public static int FoxSpawnWeight;
    public static int FoxMinChunk;
    public static int FoxMaxChunk;
    public static boolean FoxSpawn;
    public static int GoatSpawnWeight;
    public static int GoatMinChunk;
    public static int GoatMaxChunk;
    public static boolean GoatSpawn;
    public static int KittySpawnWeight;
    public static int KittyMinChunk;
    public static int KittyMaxChunk;
    public static boolean KittySpawn;
    public static int KomodoSpawnWeight;
    public static int KomodoMinChunk;
    public static int KomodoMaxChunk;
    public static boolean KomodoSpawn;
    public static int MoleSpawnWeight;
    public static int MoleMinChunk;
    public static int MoleMaxChunk;
    public static boolean MoleSpawn;
    public static int MooseSpawnWeight;
    public static int MooseMinChunk;
    public static int MooseMaxChunk;
    public static boolean MooseSpawn;
    public static int OstrichSpawnWeight;
    public static int OstrichMinChunk;
    public static int OstrichMaxChunk;
    public static boolean OstrichSpawn;
    public static int RaccoonSpawnWeight;
    public static int RaccoonMinChunk;
    public static int RaccoonMaxChunk;
    public static boolean RaccoonSpawn;
    public static int SnakeSpawnWeight;
    public static int SnakeMinChunk;
    public static int SnakeMaxChunk;
    public static boolean SnakeSpawn;
    public static int TurkeySpawnWeight;
    public static int TurkeyMinChunk;
    public static int TurkeyMaxChunk;
    public static boolean TurkeySpawn;
    public static int TurtleSpawnWeight;
    public static int TurtleMinChunk;
    public static int TurtleMaxChunk;
    public static boolean TurtleSpawn;
    public static int HorseSpawnWeight;
    public static int HorseMinChunk;
    public static int HorseMaxChunk;
    public static boolean HorseSpawn;
    public static int WyvernSpawnWeight;
    public static int WyvernMinChunk;
    public static int WyvernMaxChunk;
    public static boolean WyvernSpawn;
    public static int DolphinSpawnWeight;
    public static int DolphinMinChunk;
    public static int DolphinMaxChunk;
    public static boolean DolphinSpawn;
    public static int FishySpawnWeight;
    public static int FishyMinChunk;
    public static int FishyMaxChunk;
    public static boolean FishySpawn;
    public static int JellyFishSpawnWeight;
    public static int JellyFishMinChunk;
    public static int JellyFishMaxChunk;
    public static boolean JellyFishSpawn;
    public static int MediumFishSpawnWeight;
    public static int MediumFishMinChunk;
    public static int MediumFishMaxChunk;
    public static boolean MediumFishSpawn;
    public static int PiranhaSpawnWeight;
    public static int PiranhaMinChunk;
    public static int PiranhaMaxChunk;
    public static boolean PiranhaSpawn;
    public static int RaySpawnWeight;
    public static int RayMinChunk;
    public static int RayMaxChunk;
    public static boolean RaySpawn;
    public static int SharkSpawnWeight;
    public static int SharkMinChunk;
    public static int SharkMaxChunk;
    public static boolean SharkSpawn;
    public static int SmallFishSpawnWeight;
    public static int SmallFishMinChunk;
    public static int SmallFishMaxChunk;
    public static boolean SmallFishSpawn;
    public static int GolemSpawnWeight;
    public static int GolemMinChunk;
    public static int GolemMaxChunk;
    public static boolean GolemSpawn;
    public static int FlameWraithSpawnWeight;
    public static int FlameWraithMinChunk;
    public static int FlameWraithMaxChunk;
    public static boolean FlameWraithSpawn;
    public static int HellRatSpawnWeight;
    public static int HellRatMinChunk;
    public static int HellRatMaxChunk;
    public static boolean HellRatSpawn;
    public static int HorseMobSpawnWeight;
    public static int HorseMobMinChunk;
    public static int HorseMobMaxChunk;
    public static boolean HorseMobSpawn;
    public static int MiniGolemSpawnWeight;
    public static int MiniGolemMinChunk;
    public static int MiniGolemMaxChunk;
    public static boolean MiniGolemSpawn;
    public static int OgreSpawnWeight;
    public static int OgreMinChunk;
    public static int OgreMaxChunk;
    public static boolean OgreSpawn;
    public static int RatSpawnWeight;
    public static int RatMinChunk;
    public static int RatMaxChunk;
    public static boolean RatSpawn;
    public static int ScorpionSpawnWeight;
    public static int ScorpionMinChunk;
    public static int ScorpionMaxChunk;
    public static boolean ScorpionSpawn;
    public static int SilverSkeletonSpawnWeight;
    public static int SilverSkeletonMinChunk;
    public static int SilverSkeletonMaxChunk;
    public static boolean SilverSkeletonSpawn;
    public static int WerewolfSpawnWeight;
    public static int WerewolfMinChunk;
    public static int WerewolfMaxChunk;
    public static boolean WerewolfSpawn;
    public static int WraithSpawnWeight;
    public static int WraithMinChunk;
    public static int WraithMaxChunk;
    public static boolean WraithSpawn;
    public static int WWolfSpawnWeight;
    public static int WWolfMinChunk;
    public static int WWolfMaxChunk;
    public static boolean WWolfSpawn;
    public static int FlySpawnWeight;
    public static int FlyMinChunk;
    public static int FlyMaxChunk;
    public static boolean FlySpawn;



    public void readMocConfigValues()
    {
        AntSpawn = mocEntityConfig.get(CATEGORY_Ant, "Ant Entity Should Spawn?", true).getBoolean(true);
        AntSpawnWeight = mocEntityConfig.get(CATEGORY_Ant, "Ant Entity SpawnWeight", 7).getInt();
        AntMinChunk = mocEntityConfig.get(CATEGORY_Ant, "Min Ant Entity by chunks", 1).getInt();
        AntMaxChunk = mocEntityConfig.get(CATEGORY_Ant, "Max Ant Entity by chunks", 5).getInt();

        BeeSpawn = mocEntityConfig.get(CATEGORY_Bee, "Bee Entity Should Spawn?", true).getBoolean(true);
        BeeSpawnWeight = mocEntityConfig.get(CATEGORY_Bee, "Bee Entity SpawnWeight", 6).getInt();
        BeeMinChunk = mocEntityConfig.get(CATEGORY_Bee, "Min Bee Entity by chunks", 1).getInt();
        BeeMaxChunk = mocEntityConfig.get(CATEGORY_Bee, "Max Bee Entity by chunks", 2).getInt();

        RoachSpawn = mocEntityConfig.get(CATEGORY_Roach, "Roach Entity Should Spawn?", true).getBoolean(true);
        RoachSpawnWeight = mocEntityConfig.get(CATEGORY_Roach, "Roach Entity SpawnWeight", 4).getInt();
        RoachMinChunk = mocEntityConfig.get(CATEGORY_Roach, "Min Roach Entity by chunks", 1).getInt();
        RoachMaxChunk = mocEntityConfig.get(CATEGORY_Roach, "Max Roach Entity by chunks", 4).getInt();

        ButterFlySpawn = mocEntityConfig.get(CATEGORY_Butterfly, "Butterfly Entity Should Spawn?", true).getBoolean(true);
        ButterFlySpawnWeight = mocEntityConfig.get(CATEGORY_Butterfly, "Butterfly Entity SpawnWeight", 8).getInt();
        ButterFlyMinChunk = mocEntityConfig.get(CATEGORY_Butterfly, "Min Butterfly Entity by chunks", 1).getInt();
        ButterFlyMaxChunk = mocEntityConfig.get(CATEGORY_Butterfly, "Max Butterfly Entity by chunks", 3).getInt();

        CrabSpawn = mocEntityConfig.get(CATEGORY_Crab, "Crab Entity Should Spawn?", true).getBoolean(true);
        CrabSpawnWeight = mocEntityConfig.get(CATEGORY_Crab, "Crab Entity SpawnWeight", 8).getInt();
        CrabMinChunk = mocEntityConfig.get(CATEGORY_Crab, "Min Crab Entity by chunks", 1).getInt();
        CrabMaxChunk = mocEntityConfig.get(CATEGORY_Crab, "Max Crab Entity by chunks", 2).getInt();

        CricketSpawn = mocEntityConfig.get(CATEGORY_Cricket, "Cricket Entity Should Spawn?", true).getBoolean(true);
        CricketSpawnWeight = mocEntityConfig.get(CATEGORY_Cricket, "Cricket Entity SpawnWeight", 8).getInt();
        CricketMinChunk = mocEntityConfig.get(CATEGORY_Cricket, "Min Cricket Entity by chunks", 1).getInt();
        CricketMaxChunk = mocEntityConfig.get(CATEGORY_Cricket, "Max Cricket Entity by chunks", 2).getInt();

        DragonflySpawn = mocEntityConfig.get(CATEGORY_Dragonfly, "Dragonfly Entity Should Spawn?", true).getBoolean(true);
        DragonflySpawnWeight = mocEntityConfig.get(CATEGORY_Dragonfly, "Dragonfly Entity SpawnWeight", 6).getInt();
        DragonflyMinChunk = mocEntityConfig.get(CATEGORY_Dragonfly, "Min Dragonfly Entity by chunks", 1).getInt();
        DragonflyMaxChunk = mocEntityConfig.get(CATEGORY_Dragonfly, "Max Dragonfly Entity by chunks", 2).getInt();

        FireflySpawn = mocEntityConfig.get(CATEGORY_Firefly, "Firefly Entity Should Spawn?", true).getBoolean(true);
        FireflySpawnWeight = mocEntityConfig.get(CATEGORY_Firefly, "Firefly Entity SpawnWeight", 8).getInt();
        FireflyMinChunk = mocEntityConfig.get(CATEGORY_Firefly, "Min Firefly Entity by chunks", 1).getInt();
        FireflyMaxChunk = mocEntityConfig.get(CATEGORY_Firefly, "Max Firefly Entity by chunks", 2).getInt();

        FlySpawn = mocEntityConfig.get(CATEGORY_Fly, "Fly Entity Should Spawn?", true).getBoolean(true);
        FlySpawnWeight = mocEntityConfig.get(CATEGORY_Fly, "Fly Entity SpawnWeight", 8).getInt();
        FlyMinChunk = mocEntityConfig.get(CATEGORY_Fly, "Min Fly Entity by chunks", 1).getInt();
        FlyMaxChunk = mocEntityConfig.get(CATEGORY_Fly, "Max Fly Entity by chunks", 2).getInt();

        MaggotSpawn = mocEntityConfig.get(CATEGORY_Maggot, "Maggot Entity Should Spawn?", true).getBoolean(true);
        MaggotSpawnWeight = mocEntityConfig.get(CATEGORY_Maggot, "Maggot Entity SpawnWeight", 8).getInt();
        MaggotMinChunk = mocEntityConfig.get(CATEGORY_Maggot, "Min Maggot Entity by chunks", 1).getInt();
        MaggotMaxChunk = mocEntityConfig.get(CATEGORY_Maggot, "Max Maggot Entity by chunks", 2).getInt();

        SnailSpawn = mocEntityConfig.get(CATEGORY_Snail, "Snail Entity Should Spawn?", true).getBoolean(true);
        SnailSpawnWeight = mocEntityConfig.get(CATEGORY_Snail, "Snail Entity SpawnWeight", 7).getInt();
        SnailMinChunk = mocEntityConfig.get(CATEGORY_Snail, "Min Snail Entity by chunks", 1).getInt();
        SnailMaxChunk = mocEntityConfig.get(CATEGORY_Snail, "Max Snail Entity by chunks", 2).getInt();

        BearSpawn = mocEntityConfig.get(CATEGORY_Bear, "Bear Entity Should Spawn?", true).getBoolean(true);
        BearSpawnWeight = mocEntityConfig.get(CATEGORY_Bear, "Bear Entity SpawnWeight", 6).getInt();
        BearMinChunk = mocEntityConfig.get(CATEGORY_Bear, "Min Bear Entity by chunks", 1).getInt();
        BearMaxChunk = mocEntityConfig.get(CATEGORY_Bear, "Max Bear Entity by chunks", 2).getInt();

        BigCatSpawn = mocEntityConfig.get(CATEGORY_BigCat, "BigCat Entity Should Spawn?", true).getBoolean(true);
        BigCatSpawnWeight = mocEntityConfig.get(CATEGORY_BigCat, "BigCat Entity SpawnWeight", 6).getInt();
        BigCatMinChunk = mocEntityConfig.get(CATEGORY_BigCat, "Min BigCat Entity by chunks", 1).getInt();
        BigCatMaxChunk = mocEntityConfig.get(CATEGORY_BigCat, "Max BigCat Entity by chunks", 2).getInt();

        BirdSpawn = mocEntityConfig.get(CATEGORY_Bird, "Bird Entity Should Spawn?", true).getBoolean(true);
        BirdSpawnWeight = mocEntityConfig.get(CATEGORY_Bird, "Bird Entity SpawnWeight", 15).getInt();
        BirdMinChunk = mocEntityConfig.get(CATEGORY_Bird, "Min Bird Entity by chunks", 2).getInt();
        BirdMaxChunk = mocEntityConfig.get(CATEGORY_Bird, "Max Bird Entity by chunks", 3).getInt();

        BoarSpawn = mocEntityConfig.get(CATEGORY_Boar, "Boar Entity Should Spawn?", true).getBoolean(true);
        BoarSpawnWeight = mocEntityConfig.get(CATEGORY_Boar, "Boar Entity SpawnWeight", 8).getInt();
        BoarMinChunk = mocEntityConfig.get(CATEGORY_Boar, "Min Boar Entity by chunks", 2).getInt();
        BoarMaxChunk = mocEntityConfig.get(CATEGORY_Boar, "Max Boar Entity by chunks", 2).getInt();

        BunnySpawn = mocEntityConfig.get(CATEGORY_Bunny, "Bunny Entity Should Spawn?", true).getBoolean(true);
        BunnySpawnWeight = mocEntityConfig.get(CATEGORY_Bunny, "Bunny Entity SpawnWeight", 8).getInt();
        BunnyMinChunk = mocEntityConfig.get(CATEGORY_Bunny, "Min Bunny Entity by chunks", 2).getInt();
        BunnyMaxChunk = mocEntityConfig.get(CATEGORY_Bunny, "Max Bunny Entity by chunks", 3).getInt();

        CrocodileSpawn = mocEntityConfig.get(CATEGORY_Crocodile, "Crocodile Entity Should Spawn?", true).getBoolean(true);
        CrocodileSpawnWeight = mocEntityConfig.get(CATEGORY_Crocodile, "Crocodile Entity SpawnWeight", 6).getInt();
        CrocodileMinChunk = mocEntityConfig.get(CATEGORY_Crocodile, "Min Crocodile Entity by chunks", 1).getInt();
        CrocodileMaxChunk = mocEntityConfig.get(CATEGORY_Crocodile, "Max Crocodile Entity by chunks", 2).getInt();

        DeerSpawn = mocEntityConfig.get(CATEGORY_Deer, "Deer Entity Should Spawn?", true).getBoolean(true);
        DeerSpawnWeight = mocEntityConfig.get(CATEGORY_Deer, "Deer Entity SpawnWeight", 8).getInt();
        DeerMinChunk = mocEntityConfig.get(CATEGORY_Deer, "Min Deer Entity by chunks", 1).getInt();
        DeerMaxChunk = mocEntityConfig.get(CATEGORY_Deer, "Max Deer Entity by chunks", 2).getInt();

        DuckSpawn = mocEntityConfig.get(CATEGORY_Duck, "Duck Entity Should Spawn?", true).getBoolean(true);
        DuckSpawnWeight = mocEntityConfig.get(CATEGORY_Duck, "Duck Entity SpawnWeight", 7).getInt();
        DuckMinChunk = mocEntityConfig.get(CATEGORY_Duck, "Min Duck Entity by chunks", 1).getInt();
        DuckMaxChunk = mocEntityConfig.get(CATEGORY_Duck, "Max Duck Entity by chunks", 2).getInt();

        ElephantSpawn = mocEntityConfig.get(CATEGORY_Elephant, "Elephant Entity Should Spawn?", true).getBoolean(true);
        ElephantSpawnWeight = mocEntityConfig.get(CATEGORY_Elephant, "Elephant Entity SpawnWeight", 4).getInt();
        ElephantMinChunk = mocEntityConfig.get(CATEGORY_Elephant, "Min Elephant Entity by chunks", 1).getInt();
        ElephantMaxChunk = mocEntityConfig.get(CATEGORY_Elephant, "Max Elephant Entity by chunks", 1).getInt();

        EntSpawn = mocEntityConfig.get(CATEGORY_Ent, "Ent Entity Should Spawn?", true).getBoolean(true);
        EntSpawnWeight = mocEntityConfig.get(CATEGORY_Ent, "Ent Entity SpawnWeight", 4).getInt();
        EntMinChunk = mocEntityConfig.get(CATEGORY_Ent, "Min Ent Entity by chunks", 1).getInt();
        EntMaxChunk = mocEntityConfig.get(CATEGORY_Ent, "Max Ent Entity by chunks", 2).getInt();

        FoxSpawn = mocEntityConfig.get(CATEGORY_Fox, "Fox Entity Should Spawn?", true).getBoolean(true);
        FoxSpawnWeight = mocEntityConfig.get(CATEGORY_Fox, "Fox Entity SpawnWeight", 8).getInt();
        FoxMinChunk = mocEntityConfig.get(CATEGORY_Fox, "Min Fox Entity by chunks", 1).getInt();
        FoxMaxChunk = mocEntityConfig.get(CATEGORY_Fox, "Max Fox Entity by chunks", 1).getInt();

        GoatSpawn = mocEntityConfig.get(CATEGORY_Goat, "Goat Entity Should Spawn?", true).getBoolean(true);
        GoatSpawnWeight = mocEntityConfig.get(CATEGORY_Goat, "Goat Entity SpawnWeight", 8).getInt();
        GoatMinChunk = mocEntityConfig.get(CATEGORY_Goat, "Min Goat Entity by chunks", 1).getInt();
        GoatMaxChunk = mocEntityConfig.get(CATEGORY_Goat, "Max Goat Entity by chunks", 3).getInt();

        KittySpawn = mocEntityConfig.get(CATEGORY_Kitty, "Kitty Entity Should Spawn?", true).getBoolean(true);
        KittySpawnWeight = mocEntityConfig.get(CATEGORY_Kitty, "Kitty Entity SpawnWeight", 8).getInt();
        KittyMinChunk = mocEntityConfig.get(CATEGORY_Kitty, "Min Kitty Entity by chunks", 1).getInt();
        KittyMaxChunk = mocEntityConfig.get(CATEGORY_Kitty, "Max Kitty Entity by chunks", 2).getInt();

        KomodoSpawn = mocEntityConfig.get(CATEGORY_Komodo, "Komodo Entity Should Spawn?", true).getBoolean(true);
        KomodoSpawnWeight = mocEntityConfig.get(CATEGORY_Komodo, "Komodo Entity SpawnWeight", 8).getInt();
        KomodoMinChunk = mocEntityConfig.get(CATEGORY_Komodo, "Min Komodo Entity by chunks", 1).getInt();
        KomodoMaxChunk = mocEntityConfig.get(CATEGORY_Komodo, "Max Komodo Entity by chunks", 2).getInt();

        MoleSpawn = mocEntityConfig.get(CATEGORY_Mole, "Mole Entity Should Spawn?", true).getBoolean(true);
        MoleSpawnWeight = mocEntityConfig.get(CATEGORY_Mole, "Mole Entity SpawnWeight", 7).getInt();
        MoleMinChunk = mocEntityConfig.get(CATEGORY_Mole, "Min Mole Entity by chunks", 1).getInt();
        MoleMaxChunk = mocEntityConfig.get(CATEGORY_Mole, "Max Mole Entity by chunks", 2).getInt();

        MooseSpawn = mocEntityConfig.get(CATEGORY_Moose, "Mouse Entity Should Spawn?", true).getBoolean(true);
        MooseSpawnWeight = mocEntityConfig.get(CATEGORY_Moose, "Mouse Entity SpawnWeight", 7).getInt();
        MooseMinChunk = mocEntityConfig.get(CATEGORY_Moose, "Min Mouse Entity by chunks", 1).getInt();
        MooseMaxChunk = mocEntityConfig.get(CATEGORY_Moose, "Max Mouse Entity by chunks", 2).getInt();

        OstrichSpawn = mocEntityConfig.get(CATEGORY_Ostrich, "Ostrich Entity Should Spawn?", true).getBoolean(true);
        OstrichSpawnWeight  = mocEntityConfig.get(CATEGORY_Ostrich, "Ostrich Entity SpawnWeight", 4).getInt();
        OstrichMinChunk = mocEntityConfig.get(CATEGORY_Ostrich, "Min Ostrich Entity by chunks", 1).getInt();
        OstrichMaxChunk = mocEntityConfig.get(CATEGORY_Ostrich, "Max Ostrich Entity by chunks", 1).getInt();

        RaccoonSpawn = mocEntityConfig.get(CATEGORY_Raccoon, "Raccoon Entity Should Spawn?", true).getBoolean(true);
        RaccoonSpawnWeight = mocEntityConfig.get(CATEGORY_Raccoon, "Raccoon Entity SpawnWeight", 8).getInt();
        RaccoonMinChunk = mocEntityConfig.get(CATEGORY_Raccoon, "Min Raccoon Entity by chunks", 1).getInt();
        RaccoonMaxChunk = mocEntityConfig.get(CATEGORY_Raccoon, "Max Raccoon Entity by chunks", 2).getInt();

        SnakeSpawn = mocEntityConfig.get(CATEGORY_Snake, "Snake Entity Should Spawn?", true).getBoolean(true);
        SnakeSpawnWeight = mocEntityConfig.get(CATEGORY_Snake, "Snake Entity SpawnWeight", 8).getInt();
        SnakeMinChunk = mocEntityConfig.get(CATEGORY_Snake, "Min Snake Entity by chunks", 1).getInt();
        SnakeMaxChunk = mocEntityConfig.get(CATEGORY_Snake, "Max Snake Entity by chunks", 2).getInt();

        TurkeySpawn = mocEntityConfig.get(CATEGORY_Turkey, "Turkey Entity Should Spawn?", true).getBoolean(true);
        TurkeySpawnWeight = mocEntityConfig.get(CATEGORY_Turkey, "Turkey Entity SpawnWeight", 8).getInt();
        TurkeyMinChunk = mocEntityConfig.get(CATEGORY_Turkey, "Min Turkey Entity by chunks", 1).getInt();
        TurkeyMaxChunk = mocEntityConfig.get(CATEGORY_Turkey, "Max Turkey Entity by chunks", 2).getInt();

        TurtleSpawn = mocEntityConfig.get(CATEGORY_Turtle, "Turtle Entity Should Spawn?", true).getBoolean(true);
        TurtleSpawnWeight = mocEntityConfig.get(CATEGORY_Turtle, "Turtle Entity SpawnWeight", 8).getInt();
        TurtleMinChunk = mocEntityConfig.get(CATEGORY_Turtle, "Min Turtle Entity by chunks", 1).getInt();
        TurtleMaxChunk = mocEntityConfig.get(CATEGORY_Turtle, "Max Turtle Entity by chunks", 2).getInt();

        HorseSpawn = mocEntityConfig.get(CATEGORY_Horse, "Horse Entity Should Spawn?", true).getBoolean(true);
        HorseSpawnWeight = mocEntityConfig.get(CATEGORY_Horse, "Horse Entity SpawnWeight", 8).getInt();
        HorseMinChunk = mocEntityConfig.get(CATEGORY_Horse, "Min Horse Entity by chunks", 1).getInt();
        HorseMaxChunk = mocEntityConfig.get(CATEGORY_Horse, "Max Horse Entity by chunks", 4).getInt();

        WyvernSpawn = mocEntityConfig.get(CATEGORY_Wyvern, "Wyvern Entity Should Spawn?", true).getBoolean(true);
        WyvernSpawnWeight = mocEntityConfig.get(CATEGORY_Wyvern, "Wyvern Entity SpawnWeight", 60).getInt();
        WyvernMinChunk = mocEntityConfig.get(CATEGORY_Wyvern, "Min Wyvern Entity by chunks", 5).getInt();
        WyvernMaxChunk = mocEntityConfig.get(CATEGORY_Wyvern, "Max Wyvern Entity by chunks", 15).getInt();

        DolphinSpawn = mocEntityConfig.get(CATEGORY_Dolphin, "Dolphin Entity Should Spawn?", true).getBoolean(true);
        DolphinSpawnWeight = mocEntityConfig.get(CATEGORY_Dolphin, "Dolphin Entity SpawnWeight", 6).getInt();
        DolphinMinChunk = mocEntityConfig.get(CATEGORY_Dolphin, "Min Dolphin Entity by chunks", 1).getInt();
        DolphinMaxChunk = mocEntityConfig.get(CATEGORY_Dolphin, "Max Dolphin Entity by chunks", 1).getInt();

        FishySpawn = mocEntityConfig.get(CATEGORY_Fishy, "Fishy Entity Should Spawn?", true).getBoolean(true);
        FishySpawnWeight = mocEntityConfig.get(CATEGORY_Fishy, "Fishy Entity SpawnWeight", 12).getInt();
        FishyMinChunk = mocEntityConfig.get(CATEGORY_Fishy, "Min Fishy Entity by chunks", 1).getInt();
        FishyMaxChunk = mocEntityConfig.get(CATEGORY_Fishy, "Max Fishy Entity by chunks", 6).getInt();

        JellyFishSpawn = mocEntityConfig.get(CATEGORY_JellyFish, "JellyFish Entity Should Spawn?", true).getBoolean(true);
        JellyFishSpawnWeight = mocEntityConfig.get(CATEGORY_JellyFish, "JellyFish Entity SpawnWeight", 8).getInt();
        JellyFishMinChunk = mocEntityConfig.get(CATEGORY_JellyFish, "Min JellyFish Entity by chunks", 1).getInt();
        JellyFishMaxChunk = mocEntityConfig.get(CATEGORY_JellyFish, "Max JellyFish Entity by chunks", 4).getInt();

        MediumFishSpawn = mocEntityConfig.get(CATEGORY_MediumFish, "MediumFish Entity Should Spawn?", true).getBoolean(true);
        MediumFishSpawnWeight = mocEntityConfig.get(CATEGORY_MediumFish, "MediumFish Entity SpawnWeight", 10).getInt();
        MediumFishMinChunk = mocEntityConfig.get(CATEGORY_MediumFish, "Min MediumFish Entity by chunks", 1).getInt();
        MediumFishMaxChunk = mocEntityConfig.get(CATEGORY_MediumFish, "Max MediumFish Entity by chunks", 4).getInt();

        PiranhaSpawn = mocEntityConfig.get(CATEGORY_Piranha, "Piranha Entity Should Spawn?", true).getBoolean(true);
        PiranhaSpawnWeight = mocEntityConfig.get(CATEGORY_Piranha, "Piranha Entity SpawnWeight", 4).getInt();
        PiranhaMinChunk = mocEntityConfig.get(CATEGORY_Piranha, "Min Piranha Entity by chunks", 1).getInt();
        PiranhaMaxChunk = mocEntityConfig.get(CATEGORY_Piranha, "Max Piranha Entity by chunks", 3).getInt();

        RaySpawn = mocEntityConfig.get(CATEGORY_Ray, "Ray Entity Should Spawn?", true).getBoolean(true);
        RaySpawnWeight = mocEntityConfig.get(CATEGORY_Ray, "Ray Entity SpawnWeight", 10).getInt();
        RayMinChunk = mocEntityConfig.get(CATEGORY_Ray, "Min Ray Entity by chunks", 1).getInt();
        RayMaxChunk = mocEntityConfig.get(CATEGORY_Ray, "Max Ray Entity by chunks", 2).getInt();

        SharkSpawn = mocEntityConfig.get(CATEGORY_Shark, "Shark Entity Should Spawn?", true).getBoolean(true);
        SharkSpawnWeight = mocEntityConfig.get(CATEGORY_Shark, "Shark Entity SpawnWeight", 6).getInt();
        SharkMinChunk = mocEntityConfig.get(CATEGORY_Shark, "Min Shark Entity by chunks", 1).getInt();
        SharkMaxChunk = mocEntityConfig.get(CATEGORY_Shark, "Max Shark Entity by chunks", 1).getInt();

        SmallFishSpawn = mocEntityConfig.get(CATEGORY_SmallFish, "SmallFish Entity Should Spawn?", true).getBoolean(true);
        SmallFishSpawnWeight = mocEntityConfig.get(CATEGORY_SmallFish, "SmallFish Entity SpawnWeight", 12).getInt();
        SmallFishMinChunk = mocEntityConfig.get(CATEGORY_SmallFish, "Min SmallFish Entity by chunks", 1).getInt();
        SmallFishMaxChunk = mocEntityConfig.get(CATEGORY_SmallFish, "Max SmallFish Entity by chunks", 6).getInt();

        GolemSpawn = mocEntityConfig.get(CATEGORY_Golem, "Golem Entity Should Spawn?", true).getBoolean(true);
        GolemSpawnWeight = mocEntityConfig.get(CATEGORY_Golem, "Golem Entity SpawnWeight", 3).getInt();
        GolemMinChunk = mocEntityConfig.get(CATEGORY_Golem, "Min Golem Entity by chunks", 1).getInt();
        GolemMaxChunk = mocEntityConfig.get(CATEGORY_Golem, "Max Golem Entity by chunks", 1).getInt();

        FlameWraithSpawn = mocEntityConfig.get(CATEGORY_FlameWraith, "FlameWraith Entity Should Spawn?", true).getBoolean(true);
        FlameWraithSpawnWeight = mocEntityConfig.get(CATEGORY_FlameWraith, "FlameWraith Entity SpawnWeight", 5).getInt();
        FlameWraithMinChunk = mocEntityConfig.get(CATEGORY_FlameWraith, "Min FlameWraith Entity by chunks", 1).getInt();
        FlameWraithMaxChunk = mocEntityConfig.get(CATEGORY_FlameWraith, "Max FlameWraith Entity by chunks", 1).getInt();

        HellRatSpawn = mocEntityConfig.get(CATEGORY_HellRat, "HellRat Entity Should Spawn?", true).getBoolean(true);
        HellRatSpawnWeight = mocEntityConfig.get(CATEGORY_HellRat, "HellRat Entity SpawnWeight", 6).getInt();
        HellRatMinChunk = mocEntityConfig.get(CATEGORY_HellRat, "Min HellRat Entity by chunks", 1).getInt();
        HellRatMaxChunk = mocEntityConfig.get(CATEGORY_HellRat, "Max HellRat Entity by chunks", 4).getInt();

        HorseMobSpawn = mocEntityConfig.get(CATEGORY_HorseMob, "HorseMob Entity Should Spawn?", true).getBoolean(true);
        HorseMobSpawnWeight = mocEntityConfig.get(CATEGORY_HorseMob, "HorseMob Entity SpawnWeight", 8).getInt();
        HorseMobMinChunk = mocEntityConfig.get(CATEGORY_HorseMob, "Min HorseMob Entity by chunks", 1).getInt();
        HorseMobMaxChunk = mocEntityConfig.get(CATEGORY_HorseMob, "Max HorseMob Entity by chunks", 3).getInt();

        MiniGolemSpawn = mocEntityConfig.get(CATEGORY_MiniGolem, "MiniGolem Entity Should Spawn?", true).getBoolean(true);
        MiniGolemSpawnWeight = mocEntityConfig.get(CATEGORY_MiniGolem, "MiniGolem Entity SpawnWeight", 6).getInt();
        MiniGolemMinChunk = mocEntityConfig.get(CATEGORY_MiniGolem, "Min MiniGolem Entity by chunks", 1).getInt();
        MiniGolemMaxChunk = mocEntityConfig.get(CATEGORY_MiniGolem, "Max MiniGolem Entity by chunks", 1).getInt();

        OgreSpawn = mocEntityConfig.get(CATEGORY_Ogre, "Ogre Entity Should Spawn?", true).getBoolean(true);
        OgreSpawnWeight = mocEntityConfig.get(CATEGORY_Ogre, "Ogre Entity SpawnWeight", 8).getInt();
        OgreMinChunk = mocEntityConfig.get(CATEGORY_Ogre, "Min Ogre Entity by chunks", 1).getInt();
        OgreMaxChunk = mocEntityConfig.get(CATEGORY_Ogre, "Max Ogre Entity by chunks", 1).getInt();

        RatSpawn = mocEntityConfig.get(CATEGORY_Rat, "Rat Entity Should Spawn?", true).getBoolean(true);
        RatSpawnWeight = mocEntityConfig.get(CATEGORY_Rat, "Rat Entity SpawnWeight", 7).getInt();
        RatMinChunk = mocEntityConfig.get(CATEGORY_Rat, "Min Rat Entity by chunks", 1).getInt();
        RatMaxChunk = mocEntityConfig.get(CATEGORY_Rat, "Max Rat Entity by chunks", 2).getInt();

        ScorpionSpawn = mocEntityConfig.get(CATEGORY_Scorpion, "Scorpion Entity Should Spawn?", true).getBoolean(true);
        ScorpionSpawnWeight = mocEntityConfig.get(CATEGORY_Scorpion, "Scorpion Entity SpawnWeight", 6).getInt();
        ScorpionMinChunk = mocEntityConfig.get(CATEGORY_Scorpion, "Min Scorpion Entity by chunks", 1).getInt();
        ScorpionMaxChunk = mocEntityConfig.get(CATEGORY_Scorpion, "Max Scorpion Entity by chunks", 1).getInt();

        SilverSkeletonSpawn = mocEntityConfig.get(CATEGORY_SilverSkeleton, "SilverSkeleton Entity Should Spawn?", true).getBoolean(true);
        SilverSkeletonSpawnWeight = mocEntityConfig.get(CATEGORY_SilverSkeleton, "SilverSkeleton SpawnWeight", 6).getInt();
        SilverSkeletonMinChunk = mocEntityConfig.get(CATEGORY_SilverSkeleton, "Min SilverSkeleton Entity by chunks", 1).getInt();
        SilverSkeletonMaxChunk = mocEntityConfig.get(CATEGORY_SilverSkeleton, "Max SilverSkeleton Entity by chunks", 4).getInt();

        WerewolfSpawn = mocEntityConfig.get(CATEGORY_Werewolf, "Werewolf Entity Should Spawn?", true).getBoolean(true);
        WerewolfSpawnWeight = mocEntityConfig.get(CATEGORY_Werewolf, "Werewolf Entity SpawnWeight", 8).getInt();
        WerewolfMinChunk = mocEntityConfig.get(CATEGORY_Werewolf, "Min Werewolf Entity by chunks", 1).getInt();
        WerewolfMaxChunk = mocEntityConfig.get(CATEGORY_Werewolf, "Max Werewolf Entity by chunks", 4).getInt();

        WraithSpawn = mocEntityConfig.get(CATEGORY_Wraith, "Wraith Entity Should Spawn?", true).getBoolean(true);
        WraithSpawnWeight = mocEntityConfig.get(CATEGORY_Wraith, "Wraith Entity SpawnWeight", 1).getInt();
        WraithMinChunk = mocEntityConfig.get(CATEGORY_Wraith, "Min Wraith Entity by chunks", 1).getInt();
        WraithMaxChunk = mocEntityConfig.get(CATEGORY_Wraith, "Max Wraith Entity by chunks", 1).getInt();

        WWolfSpawn = mocEntityConfig.get(CATEGORY_WWolf, "WWolf Entity Should Spawn?", true).getBoolean(true);
        WWolfSpawnWeight = mocEntityConfig.get(CATEGORY_WWolf, "WWolf Entity SpawnWeight", 8).getInt();
        WWolfMinChunk = mocEntityConfig.get(CATEGORY_WWolf, "Min WWolf Entity by chunks", 1).getInt();
        WWolfMaxChunk = mocEntityConfig.get(CATEGORY_WWolf, "Max WWolf Entity by chunks", 3).getInt();

        mocEntityConfig.save();
    }

    /**
     * Reads values from file
     */
    public void readGlobalConfigValues()
    {
        // client-side only
        displayPetHealth = mocSettingsConfig.get(CATEGORY_MOC_GENERAL_SETTINGS, "displayPetHealth", true, "Shows Pet Health").getBoolean(true);
        displayPetName = mocSettingsConfig.get(CATEGORY_MOC_GENERAL_SETTINGS, "displayPetName", true, "Shows Pet Name").getBoolean(true);
        displayPetIcons = mocSettingsConfig.get(CATEGORY_MOC_GENERAL_SETTINGS, "displayPetIcons", true, "Shows Pet Emotes").getBoolean(true);
        animateTextures = mocSettingsConfig.get(CATEGORY_MOC_GENERAL_SETTINGS, "animateTextures", true, "Animate Textures").getBoolean(true);
        // general
        itemID = mocSettingsConfig.get(CATEGORY_MOC_ID_SETTINGS, "ItemID", 8772, "The starting ID used for MoCreatures items. Each item will increment this number by 1 for its ID.").getInt();
        allowInstaSpawn = mocSettingsConfig.get(CATEGORY_MOC_GENERAL_SETTINGS, "allowInstaSpawn", false, "Allows you to instantly spawn MoCreatures from GUI.").getBoolean(false);
        debug = mocSettingsConfig.get(CATEGORY_MOC_GENERAL_SETTINGS, "debug", false, "Turns on verbose logging").getBoolean(false);
        minDespawnLightLevel = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "despawnLightLevel", 2, "The minimum light level threshold used to determine whether or not to despawn a farm animal. Note: Configure this value in CMS if it is installed.").getInt();
        maxDespawnLightLevel = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "despawnLightLevel", 7, "The maximum light level threshold used to determine whether or not to despawn a farm animal. Note: Configure this value in CMS if it is installed.").getInt();
        forceDespawns = mocSettingsConfig.get(CATEGORY_MOC_GENERAL_SETTINGS, "forceDespawns", false, "If true, it will force despawns on all creatures including vanilla for a more dynamic experience while exploring world. If false, all passive mocreatures will not despawn to prevent other creatures from taking over. Note: if you experience issues with farm animals despawning, adjust despawnLightLevel. If CMS is installed, this setting must remain true if you want MoCreatures to despawn.").getBoolean(false);
        maxTamed = mocSettingsConfig.get(CATEGORY_OWNERSHIP_SETTINGS, "maxTamedPerPlayer", 10, "Max tamed creatures a player can have. Requires enableOwnership to be set to true.").getInt();
        maxOPTamed = mocSettingsConfig.get(CATEGORY_OWNERSHIP_SETTINGS, "maxTamedPerOP", 20, "Max tamed creatures an op can have. Requires enableOwnership to be set to true.").getInt();
        enableOwnership = mocSettingsConfig.get(CATEGORY_OWNERSHIP_SETTINGS, "enableOwnership", false, "Assigns player as owner for each creature they tame. Only the owner can interact with the tamed creature.").getBoolean(false);
        enableResetOwnership = mocSettingsConfig.get(CATEGORY_OWNERSHIP_SETTINGS, "enableResetOwnerScroll", false, "Allows players to remove a tamed creatures owner essentially untaming it.").getBoolean(false);
        easyBreeding = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "EasyBreeding", false, "Makes horse breeding simpler.").getBoolean(true);
        elephantBulldozer = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "ElephantBulldozer", true).getBoolean(true);
        zebraChance = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "ZebraChance", 10, "The percent for spawning a zebra.").getInt();
        ostrichEggDropChance = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "OstrichEggDropChance", 3, "A value of 3 means ostriches have a 3% chance to drop an egg.").getInt();
        staticBed = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "StaticBed", true).getBoolean(true);
        staticLitter = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "StaticLitter", true).getBoolean(true);
        particleFX = mocSettingsConfig.get(CATEGORY_MOC_GENERAL_SETTINGS, "particleFX", 3).getInt();
        attackDolphins = mocSettingsConfig.get(CATEGORY_MOC_WATER_CREATURE_GENERAL_SETTINGS, "AttackDolphins", false, "Allows water creatures to attack dolphins.").getBoolean(false);
        attackHorses = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "AttackHorses", false, "Allows creatures to attack horses.").getBoolean(false);
        attackWolves = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "AttackWolves", false, "Allows creatures to attack wolves.").getBoolean(false);
        enableHunters = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "EnableHunters", false, "Allows creatures to attack other creatures. Not recommended if despawning is off.").getBoolean(false);
        destroyDrops = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "DestroyDrops", false).getBoolean(false);
        killallVillagers = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "KillAllVillagers", false).getBoolean(false);
        rareItemDropChance = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "RareItemDropChance", 25, "A value of 25 means Horses/Ostriches/Scorpions/etc. have a 25% chance to drop a rare item such as a heart of darkness, unicorn, bone when killed. Raise the value if you want higher drop rates.").getInt();
        wyvernEggDropChance = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "WyvernEggDropChance", 10, "A value of 10 means wyverns have a 10% chance to drop an egg.").getInt();
        motherWyvernEggDropChance = mocSettingsConfig.get(CATEGORY_MOC_CREATURE_GENERAL_SETTINGS, "MotherWyvernEggDropChance", 33, "A value of 33 means mother wyverns have a 33% chance to drop an egg.").getInt();

        ogreStrength = Float.parseFloat(mocSettingsConfig.get(CATEGORY_MOC_MONSTER_GENERAL_SETTINGS, "OgreStrength", 2.5F, "The block destruction radius of green Ogres").getString());
        caveOgreStrength = Float.parseFloat(mocSettingsConfig.get(CATEGORY_MOC_MONSTER_GENERAL_SETTINGS, "CaveOgreStrength", 3.0F, "The block destruction radius of Cave Ogres").getString());
        fireOgreStrength = Float.parseFloat(mocSettingsConfig.get(CATEGORY_MOC_MONSTER_GENERAL_SETTINGS, "FireOgreStrength", 2.0F, "The block destruction radius of Fire Ogres").getString());
        ogreAttackRange = (short) mocSettingsConfig.get(CATEGORY_MOC_MONSTER_GENERAL_SETTINGS, "OgreAttackRange", 12, "The block radius where ogres 'smell' players").getInt();
        fireOgreChance = (short) mocSettingsConfig.get(CATEGORY_MOC_MONSTER_GENERAL_SETTINGS, "FireOgreChance", 25, "The chance percentage of spawning Fire ogres in the Overworld").getInt();
        caveOgreChance = (short) mocSettingsConfig.get(CATEGORY_MOC_MONSTER_GENERAL_SETTINGS, "CaveOgreChance", 75, "The chance percentage of spawning Cave ogres at depth of 50 in the Overworld").getInt();
        golemDestroyBlocks = mocSettingsConfig.get(CATEGORY_MOC_MONSTER_GENERAL_SETTINGS, "golemDestroyBlocks", true, "Allows Big Golems to break blocks.").getBoolean(true);
        WyvernDimension = mocSettingsConfig.get(CATEGORY_MOC_ID_SETTINGS, "WyvernLairDimensionID", -17).getInt();
        WyvernBiomeID = mocSettingsConfig.get(CATEGORY_MOC_ID_SETTINGS, "WyvernLairBiomeID", 207).getInt();
        mocSettingsConfig.save();
    }

    // Client stuff
    public void registerRenderers() {
        // Nothing here as this is the server side proxy
    }

    public void registerRenderInformation() {
        //client
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        // TODO Auto-generated method stub
        return null;
    }

    /***
     * Dummy to know if is dedicated server or not
     *
     * @return
     */
    public int getProxyMode() {
        return 1;
    }

    /**
     * Sets the name client side. Name is synchronized with datawatchers
     *
     * @param player
     * @param mocanimal
     */
    public void setName(EntityPlayer player, IMoCEntity mocanimal) {
        //client side only
    }

    public void initGUI() {
        // client side only
    }
}
