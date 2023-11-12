package drzhark.mocreatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import drzhark.mocreatures.block.MoCBlockDirt;
import drzhark.mocreatures.block.MoCBlockGrass;
import drzhark.mocreatures.block.MoCBlockLeaf;
import drzhark.mocreatures.block.MoCBlockLog;
import drzhark.mocreatures.block.MoCBlockPlanks;
import drzhark.mocreatures.block.MoCBlockRock;
import drzhark.mocreatures.block.MoCBlockTallGrass;
import drzhark.mocreatures.client.MoCClientTickHandler;
import drzhark.mocreatures.client.MoCCreativeTabs;
import drzhark.mocreatures.client.handlers.MoCKeyHandler;
import drzhark.mocreatures.command.CommandMoCPets;
import drzhark.mocreatures.command.CommandMoCSpawn;
import drzhark.mocreatures.command.CommandMoCTP;
import drzhark.mocreatures.command.CommandMoCreatures;
import drzhark.mocreatures.dimension.BiomeGenWyvernLair;
import drzhark.mocreatures.dimension.WorldProviderWyvernEnd;
import drzhark.mocreatures.entity.ambient.MoCEntityAnt;
import drzhark.mocreatures.entity.ambient.MoCEntityBee;
import drzhark.mocreatures.entity.ambient.MoCEntityButterfly;
import drzhark.mocreatures.entity.ambient.MoCEntityCrab;
import drzhark.mocreatures.entity.ambient.MoCEntityCricket;
import drzhark.mocreatures.entity.ambient.MoCEntityDragonfly;
import drzhark.mocreatures.entity.ambient.MoCEntityFirefly;
import drzhark.mocreatures.entity.ambient.MoCEntityFly;
import drzhark.mocreatures.entity.ambient.MoCEntityMaggot;
import drzhark.mocreatures.entity.ambient.MoCEntityRoach;
import drzhark.mocreatures.entity.ambient.MoCEntitySnail;
import drzhark.mocreatures.entity.aquatic.MoCEntityDolphin;
import drzhark.mocreatures.entity.aquatic.MoCEntityFishy;
import drzhark.mocreatures.entity.aquatic.MoCEntityJellyFish;
import drzhark.mocreatures.entity.aquatic.MoCEntityMediumFish;
import drzhark.mocreatures.entity.aquatic.MoCEntityPiranha;
import drzhark.mocreatures.entity.aquatic.MoCEntityRay;
import drzhark.mocreatures.entity.aquatic.MoCEntityShark;
import drzhark.mocreatures.entity.aquatic.MoCEntitySmallFish;
import drzhark.mocreatures.entity.item.MoCEntityEgg;
import drzhark.mocreatures.entity.item.MoCEntityFishBowl;
import drzhark.mocreatures.entity.item.MoCEntityKittyBed;
import drzhark.mocreatures.entity.item.MoCEntityLitterBox;
import drzhark.mocreatures.entity.item.MoCEntityPlatform;
import drzhark.mocreatures.entity.item.MoCEntityThrowableRock;
import drzhark.mocreatures.entity.monster.MoCEntityFlameWraith;
import drzhark.mocreatures.entity.monster.MoCEntityGolem;
import drzhark.mocreatures.entity.monster.MoCEntityHellRat;
import drzhark.mocreatures.entity.monster.MoCEntityHorseMob;
import drzhark.mocreatures.entity.monster.MoCEntityMiniGolem;
import drzhark.mocreatures.entity.monster.MoCEntityOgre;
import drzhark.mocreatures.entity.monster.MoCEntityRat;
import drzhark.mocreatures.entity.monster.MoCEntityScorpion;
import drzhark.mocreatures.entity.monster.MoCEntitySilverSkeleton;
import drzhark.mocreatures.entity.monster.MoCEntityWWolf;
import drzhark.mocreatures.entity.monster.MoCEntityWerewolf;
import drzhark.mocreatures.entity.monster.MoCEntityWraith;
import drzhark.mocreatures.entity.passive.MoCEntityBear;
import drzhark.mocreatures.entity.passive.MoCEntityBigCat;
import drzhark.mocreatures.entity.passive.MoCEntityBird;
import drzhark.mocreatures.entity.passive.MoCEntityBoar;
import drzhark.mocreatures.entity.passive.MoCEntityBunny;
import drzhark.mocreatures.entity.passive.MoCEntityCrocodile;
import drzhark.mocreatures.entity.passive.MoCEntityDeer;
import drzhark.mocreatures.entity.passive.MoCEntityDuck;
import drzhark.mocreatures.entity.passive.MoCEntityElephant;
import drzhark.mocreatures.entity.passive.MoCEntityEnt;
import drzhark.mocreatures.entity.passive.MoCEntityFox;
import drzhark.mocreatures.entity.passive.MoCEntityGoat;
import drzhark.mocreatures.entity.passive.MoCEntityHorse;
import drzhark.mocreatures.entity.passive.MoCEntityKitty;
import drzhark.mocreatures.entity.passive.MoCEntityKomodo;
import drzhark.mocreatures.entity.passive.MoCEntityMole;
import drzhark.mocreatures.entity.passive.MoCEntityMouse;
import drzhark.mocreatures.entity.passive.MoCEntityOstrich;
import drzhark.mocreatures.entity.passive.MoCEntityPetScorpion;
import drzhark.mocreatures.entity.passive.MoCEntityRaccoon;
import drzhark.mocreatures.entity.passive.MoCEntitySnake;
import drzhark.mocreatures.entity.passive.MoCEntityTurkey;
import drzhark.mocreatures.entity.passive.MoCEntityTurtle;
import drzhark.mocreatures.entity.passive.MoCEntityWyvern;
import drzhark.mocreatures.item.ItemBuilderHammer;
import drzhark.mocreatures.item.ItemStaffPortal;
import drzhark.mocreatures.item.ItemStaffTeleport;
import drzhark.mocreatures.item.MoCItem;
import drzhark.mocreatures.item.MoCItemArmor;
import drzhark.mocreatures.item.MoCItemEgg;
import drzhark.mocreatures.item.MoCItemFishBowl;
import drzhark.mocreatures.item.MoCItemFood;
import drzhark.mocreatures.item.MoCItemHayStack;
import drzhark.mocreatures.item.MoCItemHorseAmulet;
import drzhark.mocreatures.item.MoCItemHorseSaddle;
import drzhark.mocreatures.item.MoCItemKittyBed;
import drzhark.mocreatures.item.MoCItemLitterBox;
import drzhark.mocreatures.item.MoCItemPetAmulet;
import drzhark.mocreatures.item.MoCItemRecord;
import drzhark.mocreatures.item.MoCItemSugarLump;
import drzhark.mocreatures.item.MoCItemTurtleSoup;
import drzhark.mocreatures.item.MoCItemWeapon;
import drzhark.mocreatures.item.MoCItemWhip;
import drzhark.mocreatures.network.MoCMessageHandler;
import drzhark.mocreatures.utils.MoCLog;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@Mod(modid = MoCreatures.MODID, name = MoCreatures.NAME, version = MoCreatures.VERSION)
public class MoCreatures {

	public static final String MODID = "MoCreatures";
	public static final String NAME = "DrZhark's Mo'Creatures";
	public static final String VERSION = "6.3.1";
	
    @Instance(MODID)
    public static MoCreatures instance;

    @SidedProxy(clientSide = "drzhark.mocreatures.client.MoCClientProxy", serverSide = "drzhark.mocreatures.MoCProxy")
    public static MoCProxy proxy;
    public static final CreativeTabs tabMoC = new MoCCreativeTabs(CreativeTabs.creativeTabArray.length, "MoCreaturesTab");
    public MoCPetMapData mapData;
    public static boolean isCustomSpawnerLoaded = false;
    public static GameProfile MOCFAKEPLAYER = new GameProfile(UUID.fromString("6E379B45-1111-2222-3333-2FE1A88BCD66"), "[MoCreatures]");

    static int MoCEntityID = 7256; // used internally, does not need to be configured by users
    public static int WyvernLairDimensionID; //17;
    public static BiomeGenBase WyvernLairBiome;
    
    public static MoCPlayerTracker tracker;
    public static Map<String, MoCEntityData> mocEntityMap = new TreeMap<String, MoCEntityData>(String.CASE_INSENSITIVE_ORDER);
    public static Map<Class<? extends EntityLiving>, MoCEntityData> entityMap = new HashMap<Class<? extends EntityLiving>, MoCEntityData>();
    public static Map<Integer, Class<? extends EntityLiving>> instaSpawnerMap = new HashMap<Integer, Class<? extends EntityLiving>>();
    public static List<String> defaultBiomeSupport = new ArrayList<String>();
    public static final String CATEGORY_ITEM_IDS = "item-ids";

    /**
     * MATERIALS
     */
    static ArmorMaterial crocARMOR = EnumHelper.addArmorMaterial("crocARMOR", 15, new int[] { 2, 6, 5, 2 }, 12);
    static ArmorMaterial furARMOR = EnumHelper.addArmorMaterial("furARMOR", 15, new int[] { 2, 6, 5, 2 }, 12);
    static ArmorMaterial hideARMOR = EnumHelper.addArmorMaterial("hideARMOR", 15, new int[] { 2, 6, 5, 2 }, 12);
    static ArmorMaterial scorpfARMOR = EnumHelper.addArmorMaterial("scorpfARMOR", 18, new int[] { 2, 7, 6, 2 }, 12);
    static ArmorMaterial scorpnARMOR = EnumHelper.addArmorMaterial("scorpnARMOR", 20, new int[] { 3, 7, 6, 3 }, 15);
    static ArmorMaterial scorpcARMOR = EnumHelper.addArmorMaterial("scorpcARMOR", 15, new int[] { 2, 6, 5, 2 }, 12);
    static ArmorMaterial scorpdARMOR = EnumHelper.addArmorMaterial("scorpdARMOR", 15, new int[] { 2, 6, 5, 2 }, 12);
    //static ArmorMaterial silverARMOR = EnumHelper.addArmorMaterial("silverARMOR", 15, new int[] { 2, 6, 5, 2 }, 15);
    static ToolMaterial SILVER = EnumHelper.addToolMaterial("SILVER", 0, 250, 6.0F, 4, 15);

    /**
     * BLOCKS
     */
    public static ArrayList<String> multiBlockNames = new ArrayList<String>();
    public static Block mocStone;
    public static Block mocGrass;
    public static Block mocDirt;
    //public static Block mocSapling;
    public static Block mocPlank;
    public static Block mocLog;
    public static Block mocLeaf;
    public static Block mocTallGrass;

    /**
     * ITEMS
     */
    //Crab
    public static Item crabraw;
    public static Item crabcooked;
    //Ostrich
    public static Item ostrichraw;
    public static Item ostrichcooked;
    //Turkey
    public static Item rawTurkey;
    public static Item cookedTurkey;
    //Rat
    public static Item ratRaw;
    public static Item ratCooked;
    public static Item ratBurger;
    //Turtle
    public static Item turtleraw;
    public static Item turtlesoup;
    //Omelet
    public static Item omelet;

    //Mob drops
    public static Item hideCroc;
    public static Item fur;
    public static Item animalHide;
    public static Item chitinFrost;
    public static Item chitinNether;
    public static Item chitinCave;
    public static Item chitin;
    public static Item sharkteeth;
    public static Item bigcatclaw;
    public static Item heartdarkness;
    public static Item heartfire;
    public static Item heartundead;
    public static Item unicornhorn;

    //Crafting materials
    public static Item essencedarkness;
    public static Item essencefire;
    public static Item essenceundead;
    public static Item essencelight;
    
    //Croc set
    public static Item helmetCroc;
    public static Item plateCroc;
    public static Item legsCroc;
    public static Item bootsCroc;
    //Fur set
    public static Item helmetFur;
    public static Item chestFur;
    public static Item legsFur;
    public static Item bootsFur;
    //Hide set
    public static Item helmetHide;
    public static Item chestHide;
    public static Item legsHide;
    public static Item bootsHide;

    //Scorpion Frost set
    public static Item scorpHelmetFrost;
    public static Item scorpPlateFrost;
    public static Item scorpLegsFrost;
    public static Item scorpBootsFrost;
    //Scorpion Nether set
    public static Item scorpHelmetNether;
    public static Item scorpPlateNether;
    public static Item scorpLegsNether;
    public static Item scorpBootsNether;
    //Scorpion Cave set
    public static Item scorpHelmetCave;
    public static Item scorpPlateCave;
    public static Item scorpLegsCave;
    public static Item scorpBootsCave;
    //Scorpion Dirt set
    public static Item scorpHelmetDirt;
    public static Item scorpPlateDirt;
    public static Item scorpLegsDirt;
    public static Item scorpBootsDirt;

    //Scorpion stings
    public static Item scorpStingFrost;
    public static Item scorpStingNether;
    public static Item scorpStingCave;
    public static Item scorpStingDirt;
    //Scorpion swords
    public static Item scorpSwordFrost;
    public static Item scorpSwordNether;
    public static Item scorpSwordCave;
    public static Item scorpSwordDirt;

    //Other weapons
    public static Item silversword;
    public static Item sharksword;
    public static Item nunchaku;
    public static Item sai;
    public static Item bo;
    public static Item katana;
    public static Item builderHammer;

    //public static Item rope;
    //public static Item staff;
    //public static Item staffdiamond;
    //public static Item staffender;
    //public static Item staffunicorn;

    //Staves
    public static Item staffTeleport;
    public static Item staffPortal;
    
    //Amulets
    public static Item amuletbone;
    public static Item amuletbonefull;
    public static Item amuletghost;
    public static Item amuletghostfull;
    public static Item amuletfairy;
    public static Item amuletfairyfull;
    public static Item amuletpegasus;
    public static Item amuletpegasusfull;

    //Misc
    public static Item recordshuffle;
    public static Item key;

    //Elephant related
    public static Item tusksWood;
    public static Item tusksIron;
    public static Item tusksDiamond;
    public static Item elephantChest;
    public static Item elephantGarment;
    public static Item elephantHarness;
    public static Item elephantHowdah;
    public static Item mammothPlatform;

    //Horse related
    public static Item horsesaddle;
    //public static Item horsearmormetal;
    //public static Item horsearmorgold;
    //public static Item horsearmordiamond;
    public static Item horsearmorcrystal;
    public static Item haystack;
    public static Item sugarlump;

    //Pet related
    public static Item petfood;
    public static Item woolball;
    public static Item kittybed;
    public static Item litterbox;
    public static Item whip;
    public static Item fishnet;
    public static Item medallion;
    public static Item petamulet;
    public static Item scrollOfSale;
    public static Item scrollFreedom;
    public static Item scrollOfOwner;

    public static Item mocegg;

    public static Item fishbowl_e;
    public static Item fishbowl_w;
    public static Item fishbowl_1;
    public static Item fishbowl_2;
    public static Item fishbowl_3;
    public static Item fishbowl_4;
    public static Item fishbowl_5;
    public static Item fishbowl_6;
    public static Item fishbowl_7;
    public static Item fishbowl_8;
    public static Item fishbowl_9;
    public static Item fishbowl_10;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MoCMessageHandler.init();
        MinecraftForge.EVENT_BUS.register(new MoCEventHooks());
        proxy.ConfigInit(event);
        proxy.initTextures();
        WyvernLairDimensionID = proxy.WyvernDimension;//17
        this.InitItems();
        this.RegisterWithOreDictionary();
        this.AddRecipes();
        proxy.mocSettingsConfig.save();
        proxy.registerRenderers();
        proxy.registerRenderInformation();
        if (!isServer()) {
            FMLCommonHandler.instance().bus().register(new MoCClientTickHandler());
            FMLCommonHandler.instance().bus().register(new MoCKeyHandler());
        }
        FMLCommonHandler.instance().bus().register(new MoCPlayerTracker());
    }

    //how to check for client: if(FMLCommonHandler.instance().getSide().isRemote())

    @EventHandler
    public void load(FMLInitializationEvent event) {
        DimensionManager.registerProviderType(WyvernLairDimensionID, WorldProviderWyvernEnd.class, true);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        isCustomSpawnerLoaded = Loader.isModLoaded("CustomSpawner");
        //ForgeChunkManager.setForcedChunkLoadingCallback(instance, new MoCloadCallback());
        DimensionManager.registerDimension(WyvernLairDimensionID, WyvernLairDimensionID);
        // ***MUST REGISTER BIOMES AT THIS POINT TO MAKE SURE OUR ENTITIES GET ALL BIOMES FROM DICTIONARY****
        this.WyvernLairBiome = new BiomeGenWyvernLair(MoCreatures.proxy.WyvernBiomeID);
        this.defaultBiomeSupport.add("biomesop");
        this.defaultBiomeSupport.add("extrabiomes");
        this.defaultBiomeSupport.add("highlands");
        this.defaultBiomeSupport.add("ted80");
        this.defaultBiomeSupport.add("extrabiomes");
        this.defaultBiomeSupport.add("minecraft");
        registerEntities();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.initGUI();
        event.registerServerCommand(new CommandMoCreatures());
        event.registerServerCommand(new CommandMoCTP());
        event.registerServerCommand(new CommandMoCPets());
        if (isServer()) {
            if (MinecraftServer.getServer().isDedicatedServer()) {
                event.registerServerCommand(new CommandMoCSpawn());
            }
        }
    }

    /*
    public class MoCloadCallback implements ForgeChunkManager.OrderedLoadingCallback {

        @Override
        public void ticketsLoaded(List<Ticket> tickets, World world) {
        }

        @Override
        public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount) {
            return tickets;
        }
    }*/

    public void registerEntities() {
        registerEntity(MoCEntityBunny.class, "Bunny", 12623485, 9141102);//, 0x05600, 0x006500);
        registerEntity(MoCEntitySnake.class, "Snake", 14020607, 13749760);//, 0x05800, 0x006800);
        registerEntity(MoCEntityTurtle.class, "Turtle", 14772545, 9320590);//, 0x04800, 0x004500);
        registerEntity(MoCEntityBird.class, "Bird", 14020607, 14020607);// 0x03600, 0x003500);
        registerEntity(MoCEntityMouse.class, "Mouse", 14772545, 0);//, 0x02600, 0x002500);
        registerEntity(MoCEntityTurkey.class, "Turkey", 14020607, 16711680);//, 0x2600, 0x052500);
        registerEntity(MoCEntityHorse.class, "WildHorse", 12623485, 15656192);//, 0x2600, 0x052500);
        registerEntity(MoCEntityHorseMob.class, "HorseMob", 16711680, 9320590);//, 0x2600, 0x052500);
        registerEntity(MoCEntityOgre.class, "Ogre", 16711680, 65407);//, 0x2600, 0x052500);
        registerEntity(MoCEntityBoar.class, "Boar", 14772545, 9141102);//, 0x2600, 0x052500);
        registerEntity(MoCEntityBear.class, "Bear", 14772545, 1);//, 0x2600, 0x052500);
        registerEntity(MoCEntityDuck.class, "Duck", 14772545, 15656192);//, 0x2600, 0x052500);
        registerEntity(MoCEntityBigCat.class, "BigCat", 12623485, 16622);//, 0x2600, 0x052500);
        registerEntity(MoCEntityDeer.class, "Deer", 14772545, 33023);//, 0x2600, 0x052500);
        registerEntity(MoCEntityWWolf.class, "WWolf", 16711680, 13749760);//, 0x2600, 0x052500);
        registerEntity(MoCEntityWraith.class, "Wraith", 16711680, 0);//, 0x2600, 0x052500);
        registerEntity(MoCEntityFlameWraith.class, "FlameWraith", 16711680, 12623485);//, 0x2600, 0x052500);
        registerEntity(MoCEntityFox.class, "Fox", 14772545, 5253242);//, 0x2600, 0x052500);
        registerEntity(MoCEntityWerewolf.class, "Werewolf", 16711680, 7434694);//, 0x2600, 0x052500);
        registerEntity(MoCEntityShark.class, "Shark", 33023, 9013643);//, 0x2600, 0x052500);
        registerEntity(MoCEntityDolphin.class, "Dolphin", 33023, 15631086);//, 0x2600, 0x052500);
        registerEntity(MoCEntityFishy.class, "Fishy", 33023, 65407);//, 0x2600, 0x052500);
        registerEntity(MoCEntityKitty.class, "Kitty", 12623485, 5253242);//, 0x2600, 0x052500);
        registerEntity(MoCEntityKittyBed.class, "KittyBed");
        registerEntity(MoCEntityLitterBox.class, "LitterBox");
        registerEntity(MoCEntityRat.class, "Rat", 12623485, 9141102);//, 0x2600, 0x052500);
        registerEntity(MoCEntityHellRat.class, "HellRat", 16711680, 14772545);//, 0x2600, 0x052500);
        registerEntity(MoCEntityScorpion.class, "Scorpion", 16711680, 6053069);//, 0x2600, 0x052500);
        registerEntity(MoCEntityCrocodile.class, "Crocodile", 16711680, 65407);//, 0x2600, 0x052500);
        registerEntity(MoCEntityRay.class, "Ray", 33023, 9141102);//14772545, 9141102);
        registerEntity(MoCEntityJellyFish.class, "JellyFish", 33023, 14772545);//, 0x2600, 0x052500);
        registerEntity(MoCEntityGoat.class, "Goat", 7434694, 6053069);//, 0x2600, 0x052500);
        registerEntity(MoCEntityEgg.class, "Egg");//, 0x2600, 0x052500);
        registerEntity(MoCEntityFishBowl.class, "FishBowl");//, 0x2600, 0x052500);
        registerEntity(MoCEntityOstrich.class, "Ostrich", 14020607, 9639167);//, 0x2600, 0x052500);
        registerEntity(MoCEntityBee.class, "Bee", 65407, 15656192);//, 0x2600, 0x052500);
        registerEntity(MoCEntityFly.class, "Fly", 65407, 1);//, 0x2600, 0x052500);
        registerEntity(MoCEntityDragonfly.class, "DragonFly", 65407, 14020607);//, 0x2600, 0x052500);
        registerEntity(MoCEntityFirefly.class, "Firefly", 65407, 9320590);//, 0x2600, 0x052500);
        registerEntity(MoCEntityCricket.class, "Cricket", 65407, 16622);//, 0x2600, 0x052500);
        registerEntity(MoCEntitySnail.class, "Snail", 65407, 14772545);//, 0x2600, 0x052500);
        registerEntity(MoCEntityButterfly.class, "ButterFly", 65407, 7434694);//, 0x22600, 0x012500);
        registerEntity(MoCEntityThrowableRock.class, "TRock");
        registerEntity(MoCEntityGolem.class, "BigGolem", 16711680, 16622);
        registerEntity(MoCEntityPetScorpion.class, "PetScorpion");
        registerEntity(MoCEntityPlatform.class, "MoCPlatform");
        registerEntity(MoCEntityElephant.class, "Elephant", 14772545, 23423);
        registerEntity(MoCEntityKomodo.class, "KomodoDragon", 16711680, 23423);
        registerEntity(MoCEntityWyvern.class, "Wyvern", 14772545, 65407);
        registerEntity(MoCEntityRoach.class, "Roach", 65407, 13749760);
        registerEntity(MoCEntityMaggot.class, "Maggot", 65407, 9141102);
        registerEntity(MoCEntityCrab.class, "Crab", 65407, 13749760);
        registerEntity(MoCEntityRaccoon.class, "Raccoon", 14772545, 13749760);
        registerEntity(MoCEntityMiniGolem.class, "MiniGolem", 16711680, 13749760);
        registerEntity(MoCEntitySilverSkeleton.class, "SilverSkeleton", 16711680, 33023);
        registerEntity(MoCEntityAnt.class, "Ant", 65407, 12623485);
        registerEntity(MoCEntityMediumFish.class, "MediumFish", 33023, 16622);
        registerEntity(MoCEntitySmallFish.class, "SmallFish", 33023, 65407);
        registerEntity(MoCEntityPiranha.class, "Piranha", 33023, 16711680);
        registerEntity(MoCEntityEnt.class, "Ent", 12623485, 16711680);
        registerEntity(MoCEntityMole.class, "Mole", 14020607, 16711680);
        
        /**
         * fucsia 16711680 orange curuba 14772545 gris claro 9141102 gris medio
         * 9013643 rosado 15631086 rosado claro 12623485 azul oscuro 2037680
         * azul mas oscuro 205 amarillo 15656192 marron claro 13749760
         * 
         * verde claro esmeralda 65407 azul oscuro 30091 azul oscuro 2 2372490
         * blanco azulado 14020607 azul oscuro 16622 marron claro rosado
         * 12623485 azul bse huevos acuaticos 5665535 azul brillane 33023 morado
         * fucsia 9320590 lila 7434694 morado lila 6053069
         */

        // ambients
        mocEntityMap.put("Ant", new MoCEntityData("Ant", 4, EnumCreatureType.ambient, new SpawnListEntry(MoCEntityAnt.class, 7, 1, 4), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("Bee", new MoCEntityData("Bee", 3, EnumCreatureType.ambient, new SpawnListEntry(MoCEntityBee.class, 6, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS))));
        mocEntityMap.put("ButterFly", new MoCEntityData("ButterFly", 3, EnumCreatureType.ambient, new SpawnListEntry(MoCEntityButterfly.class, 8, 1, 3), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS))));
        mocEntityMap.put("Crab", new MoCEntityData("Crab", 2, EnumCreatureType.ambient, new SpawnListEntry(MoCEntityCrab.class, 8, 1, 2), new ArrayList(Arrays.asList(Type.BEACH, Type.WATER))));
        mocEntityMap.put("Cricket", new MoCEntityData("Cricket", 2, EnumCreatureType.ambient, new SpawnListEntry(MoCEntityCricket.class, 8, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP))));
        mocEntityMap.put("DragonFly", new MoCEntityData("DragonFly", 2, EnumCreatureType.ambient, new SpawnListEntry(MoCEntityDragonfly.class, 6, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.BEACH))));
        mocEntityMap.put("Firefly", new MoCEntityData("Firefly", 3, EnumCreatureType.ambient, new SpawnListEntry(MoCEntityFirefly.class, 8, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP))));
        mocEntityMap.put("Fly", new MoCEntityData("Fly", 2, EnumCreatureType.ambient, new SpawnListEntry(MoCEntityFly.class, 8, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("Maggot", new MoCEntityData("Maggot", 2, EnumCreatureType.ambient, new SpawnListEntry(MoCEntityMaggot.class, 8, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.JUNGLE, Type.PLAINS, Type.SWAMP))));
        mocEntityMap.put("Snail", new MoCEntityData("Snail", 2, EnumCreatureType.ambient, new SpawnListEntry(MoCEntitySnail.class, 7, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.JUNGLE, Type.PLAINS))));
        // creatures
        mocEntityMap.put("Bear", new MoCEntityData("Bear", 4, EnumCreatureType.creature, new SpawnListEntry(MoCEntityBear.class, 6, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.MOUNTAIN, Type.PLAINS, Type.FROZEN))));
        mocEntityMap.put("BigCat", new MoCEntityData("BigCat", 4, EnumCreatureType.creature, new SpawnListEntry(MoCEntityBigCat.class, 6, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS, Type.FROZEN))));
        mocEntityMap.put("Bird", new MoCEntityData("Bird", 4, EnumCreatureType.creature, new SpawnListEntry(MoCEntityBird.class, 15, 2, 3), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS))));
        mocEntityMap.put("Boar", new MoCEntityData("Boar", 3, EnumCreatureType.creature, new SpawnListEntry(MoCEntityBoar.class, 8, 2, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.JUNGLE, Type.PLAINS))));
        mocEntityMap.put("Bunny", new MoCEntityData("Bunny", 4, EnumCreatureType.creature, new SpawnListEntry(MoCEntityBunny.class, 8, 2, 3), new ArrayList(Arrays.asList(Type.FOREST, Type.JUNGLE, Type.PLAINS, Type.FROZEN))));
        mocEntityMap.put("Crocodile", new MoCEntityData("Crocodile", 2, EnumCreatureType.creature, new SpawnListEntry(MoCEntityCrocodile.class, 6, 1, 2), new ArrayList(Arrays.asList(Type.SWAMP))));
        mocEntityMap.put("Deer", new MoCEntityData("Deer", 2, EnumCreatureType.creature, new SpawnListEntry(MoCEntityDeer.class, 8, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.PLAINS))));
        mocEntityMap.put("Duck", new MoCEntityData("Duck", 3, EnumCreatureType.creature, new SpawnListEntry(MoCEntityDuck.class, 8, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.JUNGLE, Type.PLAINS))));
        mocEntityMap.put("Elephant", new MoCEntityData("Elephant", 3, EnumCreatureType.creature, new SpawnListEntry(MoCEntityElephant.class, 4, 1, 1), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.PLAINS, Type.FROZEN))));
        mocEntityMap.put("Ent", new MoCEntityData("Ent", 3, EnumCreatureType.creature, new SpawnListEntry(MoCEntityEnt.class, 4, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.JUNGLE, Type.PLAINS))));
        mocEntityMap.put("Fox", new MoCEntityData("Fox", 2, EnumCreatureType.creature, new SpawnListEntry(MoCEntityFox.class, 8, 1, 1), new ArrayList(Arrays.asList(Type.FOREST, Type.JUNGLE, Type.PLAINS, Type.FROZEN))));
        mocEntityMap.put("Goat", new MoCEntityData("Goat", 2, EnumCreatureType.creature, new SpawnListEntry(MoCEntityGoat.class, 8, 1, 3), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS))));
        mocEntityMap.put("Kitty", new MoCEntityData("Kitty", 3, EnumCreatureType.creature, new SpawnListEntry(MoCEntityKitty.class, 8, 1, 2), new ArrayList(Arrays.asList(Type.PLAINS))));
        mocEntityMap.put("KomodoDragon", new MoCEntityData("KomodoDragon", 2, EnumCreatureType.creature, new SpawnListEntry(MoCEntityKomodo.class, 8, 1, 2), new ArrayList(Arrays.asList(Type.SWAMP))));
        mocEntityMap.put("Mole", new MoCEntityData("Mole", 3, EnumCreatureType.creature, new SpawnListEntry(MoCEntityMole.class, 7, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.PLAINS))));
        mocEntityMap.put("Mouse", new MoCEntityData("Mouse", 2, EnumCreatureType.creature, new SpawnListEntry(MoCEntityMouse.class, 7, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS))));
        mocEntityMap.put("Ostrich", new MoCEntityData("Ostrich", 3, EnumCreatureType.creature, new SpawnListEntry(MoCEntityOstrich.class, 4, 1, 1), new ArrayList(Arrays.asList(Type.DESERT, Type.PLAINS))));
        mocEntityMap.put("Raccoon", new MoCEntityData("Raccoon", 2, EnumCreatureType.creature, new SpawnListEntry(MoCEntityRaccoon.class, 8, 1, 2), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.MOUNTAIN, Type.PLAINS))));
        mocEntityMap.put("Snake", new MoCEntityData("Snake", 3, EnumCreatureType.creature, new SpawnListEntry(MoCEntitySnake.class, 8, 1, 2), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("Turkey", new MoCEntityData("Turkey", 2, EnumCreatureType.creature, new SpawnListEntry(MoCEntityTurkey.class, 8, 1, 2), new ArrayList(Arrays.asList(Type.PLAINS))));
        mocEntityMap.put("Turtle", new MoCEntityData("Turtle", 3, EnumCreatureType.creature, new SpawnListEntry(MoCEntityTurtle.class, 6, 1, 2), new ArrayList(Arrays.asList(Type.JUNGLE, Type.SWAMP))));
        mocEntityMap.put("WildHorse", new MoCEntityData("WildHorse", 4, EnumCreatureType.creature, new SpawnListEntry(MoCEntityHorse.class, 8, 1, 4), new ArrayList(Arrays.asList(Type.FOREST, Type.HILLS, Type.MOUNTAIN, Type.PLAINS))));
        mocEntityMap.put("Wyvern", new MoCEntityData("Wyvern", 3, EnumCreatureType.creature, new SpawnListEntry(MoCEntityWyvern.class, 8, 1, 3), new ArrayList()));
        // water creatures
        mocEntityMap.put("Dolphin", new MoCEntityData("Dolphin", 3, EnumCreatureType.waterCreature, new SpawnListEntry(MoCEntityDolphin.class, 6, 1, 1), new ArrayList(Arrays.asList(Type.BEACH, Type.WATER))));
        mocEntityMap.put("Fishy", new MoCEntityData("Fishy", 6, EnumCreatureType.waterCreature, new SpawnListEntry(MoCEntityFishy.class, 12, 1, 6), new ArrayList(Arrays.asList(Type.BEACH, Type.SWAMP, Type.WATER))));
        mocEntityMap.put("JellyFish", new MoCEntityData("JellyFish", 4, EnumCreatureType.waterCreature, new SpawnListEntry(MoCEntityJellyFish.class, 8, 1, 4), new ArrayList(Arrays.asList(Type.WATER))));
        mocEntityMap.put("MediumFish", new MoCEntityData("MediumFish", 4, EnumCreatureType.waterCreature, new SpawnListEntry(MoCEntityMediumFish.class, 10, 1, 4), new ArrayList(Arrays.asList(Type.BEACH, Type.SWAMP, Type.WATER))));
        mocEntityMap.put("Piranha", new MoCEntityData("Piranha", 4, EnumCreatureType.waterCreature, new SpawnListEntry(MoCEntityPiranha.class, 4, 1, 3), new ArrayList(Arrays.asList(Type.BEACH, Type.SWAMP, Type.WATER))));
        mocEntityMap.put("Ray", new MoCEntityData("Ray", 3, EnumCreatureType.waterCreature, new SpawnListEntry(MoCEntityRay.class, 10, 1, 2), new ArrayList(Arrays.asList(Type.SWAMP, Type.WATER))));
        mocEntityMap.put("Shark", new MoCEntityData("Shark", 3, EnumCreatureType.waterCreature, new SpawnListEntry(MoCEntityShark.class, 6, 1, 1), new ArrayList(Arrays.asList(Type.WATER))));
        mocEntityMap.put("SmallFish", new MoCEntityData("SmallFish", 6, EnumCreatureType.waterCreature, new SpawnListEntry(MoCEntitySmallFish.class, 12, 1, 6), new ArrayList(Arrays.asList(Type.BEACH, Type.SWAMP, Type.WATER))));
        // monsters
        mocEntityMap.put("BigGolem", new MoCEntityData("BigGolem", 1, EnumCreatureType.monster, new SpawnListEntry(MoCEntityGolem.class, 3, 1, 1), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.FROZEN, Type.JUNGLE, Type.HILLS, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("FlameWraith", new MoCEntityData("FlameWraith", 3, EnumCreatureType.monster, new SpawnListEntry(MoCEntityFlameWraith.class, 5, 1, 1), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.FROZEN, Type.JUNGLE, Type.HILLS, Type.MOUNTAIN, Type.NETHER, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("HellRat", new MoCEntityData("HellRat", 4, EnumCreatureType.monster, new SpawnListEntry(MoCEntityHellRat.class, 6, 1, 4), new ArrayList(Arrays.asList(Type.NETHER))));
        mocEntityMap.put("HorseMob", new MoCEntityData("HorseMob", 3, EnumCreatureType.monster, new SpawnListEntry(MoCEntityHorseMob.class, 8, 1, 3), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.FROZEN, Type.JUNGLE, Type.HILLS, Type.MOUNTAIN, Type.NETHER, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("MiniGolem", new MoCEntityData("MiniGolem", 2, EnumCreatureType.monster, new SpawnListEntry(MoCEntityMiniGolem.class, 6, 1, 1), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.FROZEN, Type.JUNGLE, Type.HILLS, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("Ogre", new MoCEntityData("Ogre", 3, EnumCreatureType.monster, new SpawnListEntry(MoCEntityOgre.class, 8, 1, 1), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.FROZEN, Type.JUNGLE, Type.HILLS, Type.MOUNTAIN, Type.NETHER, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("Rat", new MoCEntityData("Rat", 2, EnumCreatureType.monster, new SpawnListEntry(MoCEntityRat.class, 7, 1, 2), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.FROZEN, Type.JUNGLE, Type.HILLS, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("Scorpion", new MoCEntityData("Scorpion", 3, EnumCreatureType.monster, new SpawnListEntry(MoCEntityScorpion.class, 6, 1, 1), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.FROZEN, Type.JUNGLE, Type.HILLS, Type.MOUNTAIN, Type.NETHER, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("SilverSkeleton", new MoCEntityData("SilverSkeleton", 4, EnumCreatureType.monster, new SpawnListEntry(MoCEntitySilverSkeleton.class, 6, 1, 4), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.FROZEN, Type.JUNGLE, Type.HILLS, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("Werewolf", new MoCEntityData("Werewolf", 3, EnumCreatureType.monster, new SpawnListEntry(MoCEntityWerewolf.class, 8, 1, 4), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.FROZEN, Type.JUNGLE, Type.HILLS, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("Wraith", new MoCEntityData("Wraith", 3, EnumCreatureType.monster, new SpawnListEntry(MoCEntityWraith.class, 1, 1, 1), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.FROZEN, Type.JUNGLE, Type.HILLS, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));
        mocEntityMap.put("WWolf", new MoCEntityData("WWolf", 3, EnumCreatureType.monster, new SpawnListEntry(MoCEntityWWolf.class, 8, 1, 3), new ArrayList(Arrays.asList(Type.DESERT, Type.FOREST, Type.FROZEN, Type.JUNGLE, Type.HILLS, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND))));

        for (MoCEntityData entityData : mocEntityMap.values()) {
            if (entityData.getEntityName().equals("Wyvern")) continue;
            SpawnListEntry spawnEntry = entityData.getSpawnListEntry();
            for (BiomeDictionary.Type type : entityData.getBiomeTypes()) {
                for (BiomeGenBase biome : BiomeDictionary.getBiomesForType(type)) {
                    boolean match = false;
                    for (String allowedBiomeMod : defaultBiomeSupport) {
                        if (biome.getClass().getName().contains(allowedBiomeMod)) {
                            match = true;
                            break;
                        }
                    }
                    if (!biome.getSpawnableList(entityData.getType()).contains(spawnEntry) && match) {
                        biome.getSpawnableList(entityData.getType()).add(spawnEntry);
                    }
                }
            }
        }
        proxy.readMocConfigValues();
    }

    /**
     * For Litterbox and kittybeds
     * 
     * @param entityClass
     * @param entityName
     */
    protected void registerEntity(Class<? extends Entity> entityClass, String entityName) {
        if (proxy.debug)  {
            MoCLog.logger.info("registerEntity " + entityClass + " with Mod ID " + MoCEntityID);
        }
        EntityRegistry.registerModEntity(entityClass, entityName, MoCEntityID, instance, 128, 1, true);
        MoCEntityID += 1;
    }

    private void registerEntity(Class<? extends Entity> entityClass, String entityName, int eggColor, int eggDotsColor) {
        if (proxy.debug)  {
          MoCLog.logger.info("registerEntity " + entityClass + " with Mod ID " + MoCEntityID);
        }
        EntityRegistry.registerModEntity(entityClass, entityName, MoCEntityID, instance, 128, 1, true);
        EntityList.IDtoClassMapping.put(Integer.valueOf(MoCEntityID), entityClass);
        EntityList.entityEggs.put(Integer.valueOf(MoCEntityID), new EntityEggInfo(MoCEntityID, eggColor, eggDotsColor));
        MoCEntityID += 1;
    }

    private int getItemId(String name, int defaultId) {
        return proxy.mocSettingsConfig.get(CATEGORY_ITEM_IDS, "item_" + name, defaultId).getInt();
    }

    protected void InitItems() {
        multiBlockNames.add ("WyvernLair");
        multiBlockNames.add("OgreLair");

        //Terrain blocks
        mocStone = new MoCBlockRock("MoCStone").setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundTypeStone);
        mocGrass = new MoCBlockGrass("MoCGrass").setHardness(0.5F).setStepSound(Block.soundTypeGrass);
        mocDirt = new MoCBlockDirt("MoCDirt").setHardness(0.6F).setStepSound(Block.soundTypeGravel);
        //Plant blocks
        mocPlank = new MoCBlockPlanks("MoCWoodPlank").setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood);
        mocLog = new MoCBlockLog("MoCLog").setHardness(2.0F).setStepSound(Block.soundTypeWood);
        mocLeaf = new MoCBlockLeaf("MoCLeaves").setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundTypeGrass);
        mocTallGrass = new MoCBlockTallGrass("MoCTallGrass", true).setHardness(0.0F).setStepSound(Block.soundTypeGrass);     
        //Harvest settings
        mocStone.setHarvestLevel("pickaxe", 1, 0);
        mocDirt.setHarvestLevel("shovel", 0, 0);
        mocGrass.setHarvestLevel("shovel", 0, 0);

        //Crab
        crabraw = new MoCItemFood("crabraw", 2, 0.3F, false).setPotionEffect(Potion.hunger.id, 30, 0, 0.8F);
        crabcooked = new MoCItemFood("crabcooked", 6, 0.6F, false);
        //Ostrich
        ostrichraw = new MoCItemFood("ostrichraw", 2, 0.3F, false).setPotionEffect(Potion.hunger.id, 30, 0, 0.8F);
        ostrichcooked = new MoCItemFood("ostrichcooked", 6, 0.6F, false);
        //Turkey
        rawTurkey = new MoCItemFood("turkeyraw", 3, 0.3F, false).setPotionEffect(Potion.hunger.id, 30, 0, 0.8F);
        cookedTurkey = new MoCItemFood("turkeycooked", 8, 0.6F, false);
        //Rat
        ratRaw = new MoCItemFood("ratraw", 2, 0.3F, false).setPotionEffect(Potion.hunger.id, 30, 0, 0.8F);
        ratCooked = new MoCItemFood("ratcooked", 4, 0.6F, false);
        ratBurger = new MoCItemFood("ratburger", 8, 0.6F, false);
        //Turtle
        turtleraw = new MoCItemFood("turtleraw", 2, 0.3F, false);
        turtlesoup = new MoCItemTurtleSoup("turtlesoup", 6, 0.6F, false);
        //Omelet
        omelet = new MoCItemFood("omelet", 4, 0.6F, false);

        //Materials
        hideCroc = new MoCItem("reptilehide");
        fur = new MoCItem("fur");
        animalHide = new MoCItem("hide");
        chitinFrost = new MoCItem("chitinfrost");
        chitinNether = new MoCItem("chitinnether");
        chitinCave = new MoCItem("chitinblack");
        chitin = new MoCItem("chitin");
        sharkteeth = new MoCItem("sharkteeth");
        bigcatclaw = new MoCItem("bigcatclaw");
        heartdarkness = new MoCItem("heartdarkness");
        heartfire = new MoCItem("heartfire");
        heartundead = new MoCItem("heartundead");
        unicornhorn = new MoCItem("unicornhorn");

        //Crafting materials
        essencedarkness = new MoCItem("essencedarkness");
        essencefire = new MoCItem("essencefire");
        essenceundead = new MoCItem("essenceundead");
        essencelight = new MoCItem("essencelight");
        
        //Croc set
        helmetCroc = new MoCItemArmor("reptilehelmet", crocARMOR, 4, 0);
        plateCroc = new MoCItemArmor("reptileplate", crocARMOR, 4, 1);
        legsCroc = new MoCItemArmor("reptilelegs", crocARMOR, 4, 2);
        bootsCroc = new MoCItemArmor("reptileboots", crocARMOR, 4, 3);
        //Fur set
        helmetFur = new MoCItemArmor("furhelmet", furARMOR, 4, 0);
        chestFur = new MoCItemArmor("furchest", furARMOR, 4, 1);
        legsFur = new MoCItemArmor("furlegs", furARMOR, 4, 2);
        bootsFur = new MoCItemArmor("furboots", furARMOR, 4, 3);
        //Hide set
        helmetHide = new MoCItemArmor("hidehelmet", hideARMOR, 4, 0);
        chestHide = new MoCItemArmor("hidechest", hideARMOR, 4, 1);
        legsHide = new MoCItemArmor("hidelegs", hideARMOR, 4, 2);
        bootsHide = new MoCItemArmor("hideboots", hideARMOR, 4, 3);

        //Scorpion Frost set
        scorpHelmetFrost = new MoCItemArmor("scorphelmetfrost", scorpfARMOR, 4, 0);
        scorpPlateFrost = new MoCItemArmor("scorpplatefrost", scorpfARMOR, 4, 1);
        scorpLegsFrost = new MoCItemArmor("scorplegsfrost", scorpfARMOR, 4, 2);
        scorpBootsFrost = new MoCItemArmor("scorpbootsfrost", scorpfARMOR, 4, 3);
        //Scorpion Nether set
        scorpHelmetNether = new MoCItemArmor("scorphelmetnether", scorpnARMOR, 4, 0);
        scorpPlateNether = new MoCItemArmor("scorpplatenether", scorpnARMOR, 4, 1);
        scorpLegsNether = new MoCItemArmor("scorplegsnether", scorpnARMOR, 4, 2);
        scorpBootsNether = new MoCItemArmor("scorpbootsnether", scorpnARMOR, 4, 3);
        //Scorpion Cave set
        scorpHelmetCave = new MoCItemArmor("scorphelmetcave", scorpcARMOR, 4, 0);
        scorpPlateCave = new MoCItemArmor("scorpplatecave", scorpcARMOR, 4, 1);
        scorpLegsCave = new MoCItemArmor("scorplegscave", scorpcARMOR, 4, 2);
        scorpBootsCave = new MoCItemArmor("scorpbootscave", scorpcARMOR, 4, 3);
        //Scorpion Dirt set
        scorpHelmetDirt = new MoCItemArmor("scorphelmetdirt", scorpdARMOR, 4, 0);
        scorpPlateDirt = new MoCItemArmor("scorpplatedirt", scorpdARMOR, 4, 1);
        scorpLegsDirt = new MoCItemArmor("scorplegsdirt", scorpdARMOR, 4, 2);
        scorpBootsDirt = new MoCItemArmor("scorpbootsdirt", scorpdARMOR, 4, 3);

        //Scorpion stings
        scorpStingFrost = new MoCItemWeapon("scorpstingfrost", ToolMaterial.GOLD, 2, true);
        scorpStingNether = new MoCItemWeapon("scorpstingnether", ToolMaterial.GOLD, 3, true);
        scorpStingCave = new MoCItemWeapon("scorpstingcave", ToolMaterial.GOLD, 4, true);
        scorpStingDirt = new MoCItemWeapon("scorpstingdirt", ToolMaterial.GOLD, 1, true);
        //Scorpion swords
        scorpSwordFrost = new MoCItemWeapon("scorpswordfrost", ToolMaterial.IRON, 2, false);
        scorpSwordNether = new MoCItemWeapon("scorpswordnether", ToolMaterial.IRON, 3, false);
        scorpSwordCave = new MoCItemWeapon("scorpswordcave", ToolMaterial.IRON, 4, false);
        scorpSwordDirt = new MoCItemWeapon("scorpsworddirt", ToolMaterial.IRON, 1, false);

        //Other weapons
        silversword = new MoCItemWeapon("silversword", this.SILVER);
        sharksword = new MoCItemWeapon("sharksword", ToolMaterial.IRON);
        nunchaku = new MoCItemWeapon("nunchaku", ToolMaterial.IRON);
        sai = new MoCItemWeapon("sai", ToolMaterial.IRON);
        bo = new MoCItemWeapon("bo", ToolMaterial.IRON);
        katana = new MoCItemWeapon("katana", ToolMaterial.IRON);
        builderHammer = new ItemBuilderHammer("builderhammer");
        
        //Staves
        staffTeleport = new ItemStaffTeleport("staffteleport");
        staffPortal = new ItemStaffPortal("staffportal");

        //Amulets
        amuletbone = new MoCItemHorseAmulet("amuletbone");
        amuletbonefull = new MoCItemHorseAmulet("amuletbonefull");
        amuletghost = new MoCItemHorseAmulet("amuletghost");
        amuletghostfull = new MoCItemHorseAmulet("amuletghostfull");
        amuletfairy = new MoCItemHorseAmulet("amuletfairy");
        amuletfairyfull = new MoCItemHorseAmulet("amuletfairyfull");
        amuletpegasus = new MoCItemHorseAmulet("amuletpegasus");
        amuletpegasusfull = new MoCItemHorseAmulet("amuletpegasusfull");

        //Misc
        recordshuffle = new MoCItemRecord("recordshuffle");
        key = new MoCItem("key");

        //Elephant related
        tusksWood = new MoCItemWeapon("tuskswood", ToolMaterial.WOOD);
        tusksIron = new MoCItemWeapon("tusksiron", ToolMaterial.IRON);
        tusksDiamond = new MoCItemWeapon("tusksdiamond", ToolMaterial.EMERALD);
        elephantHarness = new MoCItem("elephantharness");
        elephantChest = new MoCItem("elephantchest");
        elephantGarment = new MoCItem("elephantgarment");
        elephantHowdah = new MoCItem("elephanthowdah");
        mammothPlatform = new MoCItem("mammothplatform");

        //Horse related
        horsesaddle = new MoCItemHorseSaddle("horsesaddle");
        horsearmorcrystal = new MoCItem("horsearmorcrystal");
        haystack = new MoCItemHayStack("haystack");
        sugarlump = new MoCItemSugarLump("sugarlump");

        //Pet related
        petfood = new MoCItem("petfood");
        woolball = new MoCItem("woolball");
        kittybed = new MoCItemKittyBed("kittybed");
        litterbox = new MoCItemLitterBox("kittylitter");
        whip = new MoCItemWhip("whip");
        fishnet = new MoCItemPetAmulet("fishnet");
        medallion = new MoCItem("medallion");
        petamulet = new MoCItemPetAmulet("petamulet", 1);
        scrollOfSale = new MoCItem("scrollofsale");
        scrollFreedom = new MoCItem("scrolloffreedom");
        scrollOfOwner = new MoCItem("scrollofowner");

        mocegg = new MoCItemEgg("mocegg");

        fishbowl_e = new MoCItemFishBowl("bowlempty", 0);
        fishbowl_w = new MoCItemFishBowl("bowlwater", 11);
        fishbowl_1 = new MoCItemFishBowl("bowlfish1", 1);
        fishbowl_2 = new MoCItemFishBowl("bowlfish2", 2);
        fishbowl_3 = new MoCItemFishBowl("bowlfish3", 3);
        fishbowl_4 = new MoCItemFishBowl("bowlfish4", 4);
        fishbowl_5 = new MoCItemFishBowl("bowlfish5", 5);
        fishbowl_6 = new MoCItemFishBowl("bowlfish6", 6);
        fishbowl_7 = new MoCItemFishBowl("bowlfish7", 7);
        fishbowl_8 = new MoCItemFishBowl("bowlfish8", 8);
        fishbowl_9 = new MoCItemFishBowl("bowlfish9", 9);
        fishbowl_10 = new MoCItemFishBowl("bowlfish10", 10);
    }

    private void RegisterWithOreDictionary() {
    	//Vanilla
    	OreDictionary.registerOre("chestWood", new ItemStack(Blocks.chest));
    	OreDictionary.registerOre("bowlWood", new ItemStack(Items.bowl));
    	OreDictionary.registerOre("listAllmeatraw", new ItemStack(Items.porkchop));
    	OreDictionary.registerOre("listAllfishraw", new ItemStack(Items.fish, 1, OreDictionary.WILDCARD_VALUE));
    	OreDictionary.registerOre("listAllmeatraw", new ItemStack(Items.beef));
    	OreDictionary.registerOre("listAllmeatraw", new ItemStack(Items.chicken));
    	
    	//Blocks
    	OreDictionary.registerOre("plankWood", new ItemStack(mocPlank, 1, OreDictionary.WILDCARD_VALUE));
    	OreDictionary.registerOre("logWood", new ItemStack(mocLog, 1, OreDictionary.WILDCARD_VALUE));
    	OreDictionary.registerOre("treeLeaves", new ItemStack(mocLeaf, 1, OreDictionary.WILDCARD_VALUE));
    	
    	//Crab
    	OreDictionary.registerOre("foodCrabraw", new ItemStack(crabraw));
    	OreDictionary.registerOre("foodCrabcooked", new ItemStack(crabcooked));
    	//Ostrich
    	OreDictionary.registerOre("foodOstrichraw", new ItemStack(ostrichraw));
    	OreDictionary.registerOre("listAllostrichraw", new ItemStack(ostrichraw));
    	OreDictionary.registerOre("listAllmeatraw", new ItemStack(ostrichraw));
    	OreDictionary.registerOre("foodOstrichcooked", new ItemStack(ostrichcooked));
    	OreDictionary.registerOre("listAllostrichcooked", new ItemStack(ostrichcooked));
    	OreDictionary.registerOre("listAllmeatcooked", new ItemStack(ostrichcooked));
    	//Turkey
    	OreDictionary.registerOre("foodTurkeyraw", new ItemStack(rawTurkey));
    	OreDictionary.registerOre("listAllturkeyraw", new ItemStack(rawTurkey));
    	OreDictionary.registerOre("listAllmeatraw", new ItemStack(rawTurkey));
    	OreDictionary.registerOre("foodTurkeycooked", new ItemStack(cookedTurkey));
    	OreDictionary.registerOre("listAllturkeycooked", new ItemStack(cookedTurkey));
    	OreDictionary.registerOre("listAllmeatcooked", new ItemStack(cookedTurkey));
    	//Rat
    	OreDictionary.registerOre("foodRatraw", new ItemStack(ratRaw));
    	OreDictionary.registerOre("foodRatcooked", new ItemStack(ratCooked));
    	OreDictionary.registerOre("foodRatburger", new ItemStack(ratBurger));
    	//Turtle
    	OreDictionary.registerOre("foodTurtleraw", new ItemStack(turtleraw));
    	OreDictionary.registerOre("foodTurtlesoup", new ItemStack(turtlesoup));
    	//Omelet
    	OreDictionary.registerOre("foodOmelet", new ItemStack(omelet));

    	//Mob drops
    	OreDictionary.registerOre("chitinScorpion", new ItemStack(chitinFrost));
    	OreDictionary.registerOre("chitinScorpionFrost", new ItemStack(chitinFrost));
    	OreDictionary.registerOre("chitinScorpion", new ItemStack(chitinNether));
    	OreDictionary.registerOre("chitinScorpionNether", new ItemStack(chitinNether));
    	OreDictionary.registerOre("chitinScorpion", new ItemStack(chitinCave));
    	OreDictionary.registerOre("chitinScorpionCave", new ItemStack(chitinCave));
    	OreDictionary.registerOre("chitinScorpion", new ItemStack(chitin));
    	OreDictionary.registerOre("chitinScorpionDirt", new ItemStack(chitin));
    	OreDictionary.registerOre("clawFelineLarge", new ItemStack(bigcatclaw));
    	OreDictionary.registerOre("toothShark", new ItemStack(sharkteeth));
    	OreDictionary.registerOre("heartDarkness", new ItemStack(heartdarkness));
    	OreDictionary.registerOre("heartFire", new ItemStack(heartfire));
    	OreDictionary.registerOre("heartUndead", new ItemStack(heartundead));
    	OreDictionary.registerOre("hornUnicorn", new ItemStack(unicornhorn));

    	//Crafting materials
    	OreDictionary.registerOre("essenceDarkness", new ItemStack(essencedarkness));
    	OreDictionary.registerOre("essenceFire", new ItemStack(essencefire));
    	OreDictionary.registerOre("essenceUndead", new ItemStack(essenceundead));
    	OreDictionary.registerOre("essenceLight", new ItemStack(essencelight));

    	//Scorpion stings
    	OreDictionary.registerOre("stingScorpion", new ItemStack(scorpStingFrost));
    	OreDictionary.registerOre("stingScorpionFrost", new ItemStack(scorpStingFrost));
    	OreDictionary.registerOre("stingScorpion", new ItemStack(scorpStingNether));
    	OreDictionary.registerOre("stingScorpionNether", new ItemStack(scorpStingNether));
    	OreDictionary.registerOre("stingScorpion", new ItemStack(scorpStingCave));
    	OreDictionary.registerOre("stingScorpionCave", new ItemStack(scorpStingCave));
    	OreDictionary.registerOre("stingScorpion", new ItemStack(scorpStingDirt));
    	OreDictionary.registerOre("stingScorpionDirt", new ItemStack(scorpStingDirt));

    	//Misc
    	OreDictionary.registerOre("record", new ItemStack(recordshuffle));
    }

    private void AddRecipes() {
    	final String[] dyeNames = {"dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite"};
    	
    	//Foods
        GameRegistry.addSmelting(MoCreatures.crabraw, new ItemStack(MoCreatures.crabcooked, 1), 0F);
        GameRegistry.addSmelting(MoCreatures.ostrichraw, new ItemStack(MoCreatures.ostrichcooked, 1), 0F);
        GameRegistry.addSmelting(MoCreatures.rawTurkey, new ItemStack(MoCreatures.cookedTurkey, 1), 0F);
        GameRegistry.addSmelting(MoCreatures.ratRaw, new ItemStack(MoCreatures.ratCooked, 1), 0F);
        GameRegistry.addSmelting(Items.egg, new ItemStack(MoCreatures.omelet, 1), 0F);
        GameRegistry.addSmelting(MoCreatures.mocegg, new ItemStack(MoCreatures.omelet, 1), 0F);



        GameRegistry.addShapelessRecipe(new ItemStack(Blocks.wool, 1), new Object[] { fur });
        GameRegistry.addShapelessRecipe(new ItemStack(Items.leather, 1), new Object[] { animalHide });
        GameRegistry.addShapelessRecipe(new ItemStack(Items.wheat, 6), new Object[] { haystack });

        //Foods
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(turtlesoup, 1), "foodTurtleraw", "bowlWood"));

        //Crafting materials
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(essencelight, 1), "essenceUndead", "essenceFire", "essenceDarkness"));

        //Scorpion weapons
        GameRegistry.addShapelessRecipe(new ItemStack(scorpSwordFrost, 1), new Object[] { Items.diamond_sword, scorpStingFrost, scorpStingFrost, scorpStingFrost });
        GameRegistry.addShapelessRecipe(new ItemStack(scorpSwordNether, 1), new Object[] { Items.diamond_sword, scorpStingNether, scorpStingNether, scorpStingNether });
        GameRegistry.addShapelessRecipe(new ItemStack(scorpSwordCave, 1), new Object[] { Items.diamond_sword, scorpStingCave, scorpStingCave, scorpStingCave });
        GameRegistry.addShapelessRecipe(new ItemStack(scorpSwordDirt, 1), new Object[] { Items.diamond_sword, scorpStingDirt, scorpStingDirt, scorpStingDirt });

        //Horse related
        GameRegistry.addShapelessRecipe(new ItemStack(sugarlump, 1), new Object[] { Items.sugar, Items.sugar, Items.sugar, Items.sugar });
        
        //Pet related
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(petfood, 4), "listAllfishraw", "listAllmeatraw"));
        GameRegistry.addShapelessRecipe(new ItemStack(scrollOfSale, 1), new Object[] { Items.paper, Items.feather });
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(scrollFreedom, 1), Items.paper, Items.feather, "dustRedstone"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(scrollFreedom, 1), scrollOfSale, "dustRedstone"));



        //Foods
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ratBurger, 1), "SB ", "GRG", " B ", Character.valueOf('R'), "foodRatcooked", Character.valueOf('B'), Items.bread, Character.valueOf('S'), Items.pumpkin_seeds, Character.valueOf('G'), Items.wheat_seeds));

        //Crafting materials
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(essencedarkness, 1), "X", "Y", "Z", Character.valueOf('X'), Items.ender_pearl, Character.valueOf('Y'), "heartDarkness", Character.valueOf('Z'), Items.glass_bottle));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(essencefire, 1), "X", "Y", "Z", Character.valueOf('X'), Items.blaze_powder, Character.valueOf('Y'), "heartFire", Character.valueOf('Z'), Items.glass_bottle));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(essencefire, 1), "X", "Y", "Z", Character.valueOf('X'), Blocks.fire, Character.valueOf('Y'), "heartFire", Character.valueOf('Z'), Items.glass_bottle));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(essenceundead, 1), "X", "Y", "Z", Character.valueOf('X'), Items.rotten_flesh, Character.valueOf('Y'), "heartUndead", Character.valueOf('Z'), Items.glass_bottle));

        //Chainmail set
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.chainmail_helmet, 1), "XXX", "X X", Character.valueOf('X'), "toothShark"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.chainmail_chestplate, 1), "X X", "XXX", "XXX", Character.valueOf('X'), "toothShark"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.chainmail_leggings, 1), "XXX", "X X", "X X", Character.valueOf('X'), "toothShark"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.chainmail_boots, 1), "X X", "X X", Character.valueOf('X'), "toothShark"));
        //Croc set
        GameRegistry.addRecipe(new ItemStack(helmetCroc, 1), new Object[] { "XXX", "X X", Character.valueOf('X'), hideCroc });
        GameRegistry.addRecipe(new ItemStack(plateCroc, 1), new Object[] { "X X", "XXX", "XXX", Character.valueOf('X'), hideCroc });
        GameRegistry.addRecipe(new ItemStack(legsCroc, 1), new Object[] { "XXX", "X X", "X X", Character.valueOf('X'), hideCroc });
        GameRegistry.addRecipe(new ItemStack(bootsCroc, 1), new Object[] { "X X", "X X", Character.valueOf('X'), hideCroc });
        //Fur set
        GameRegistry.addRecipe(new ItemStack(helmetFur, 1), new Object[] { "XXX", "X X", Character.valueOf('X'), fur });
        GameRegistry.addRecipe(new ItemStack(chestFur, 1), new Object[] { "X X", "XXX", "XXX", Character.valueOf('X'), fur });
        GameRegistry.addRecipe(new ItemStack(legsFur, 1), new Object[] { "XXX", "X X", "X X", Character.valueOf('X'), fur });
        GameRegistry.addRecipe(new ItemStack(bootsFur, 1), new Object[] { "X X", "X X", Character.valueOf('X'), fur });
        //Hide set
        GameRegistry.addRecipe(new ItemStack(helmetHide, 1), new Object[] { "XXX", "X X", Character.valueOf('X'), animalHide });
        GameRegistry.addRecipe(new ItemStack(chestHide, 1), new Object[] { "X X", "XXX", "XXX", Character.valueOf('X'), animalHide });
        GameRegistry.addRecipe(new ItemStack(legsHide, 1), new Object[] { "XXX", "X X", "X X", Character.valueOf('X'), animalHide });
        GameRegistry.addRecipe(new ItemStack(bootsHide, 1), new Object[] { "X X", "X X", Character.valueOf('X'), animalHide });
        
        //Scorpion Frost set
        GameRegistry.addRecipe(new ItemStack(scorpHelmetFrost, 1), new Object[] { "XXX", "X X", Character.valueOf('X'), chitinFrost });
        GameRegistry.addRecipe(new ItemStack(scorpPlateFrost, 1), new Object[] { "X X", "XXX", "XXX", Character.valueOf('X'), chitinFrost });
        GameRegistry.addRecipe(new ItemStack(scorpLegsFrost, 1), new Object[] { "XXX", "X X", "X X", Character.valueOf('X'), chitinFrost });
        GameRegistry.addRecipe(new ItemStack(scorpBootsFrost, 1), new Object[] { "X X", "X X", Character.valueOf('X'), chitinFrost });
        //Scorpion Nether set
        GameRegistry.addRecipe(new ItemStack(scorpHelmetNether, 1), new Object[] { "XXX", "X X", Character.valueOf('X'), chitinNether });
        GameRegistry.addRecipe(new ItemStack(scorpPlateNether, 1), new Object[] { "X X", "XXX", "XXX", Character.valueOf('X'), chitinNether });
        GameRegistry.addRecipe(new ItemStack(scorpLegsNether, 1), new Object[] { "XXX", "X X", "X X", Character.valueOf('X'), chitinNether });
        GameRegistry.addRecipe(new ItemStack(scorpBootsNether, 1), new Object[] { "X X", "X X", Character.valueOf('X'), chitinNether });
        //Scorpion Cave set
        GameRegistry.addRecipe(new ItemStack(scorpHelmetCave, 1), new Object[] { "XXX", "X X", Character.valueOf('X'), chitinCave });
        GameRegistry.addRecipe(new ItemStack(scorpPlateCave, 1), new Object[] { "X X", "XXX", "XXX", Character.valueOf('X'), chitinCave });
        GameRegistry.addRecipe(new ItemStack(scorpLegsCave, 1), new Object[] { "XXX", "X X", "X X", Character.valueOf('X'), chitinCave });
        GameRegistry.addRecipe(new ItemStack(scorpBootsCave, 1), new Object[] { "X X", "X X", Character.valueOf('X'), chitinCave });
        //Scorpion Dirt set
        GameRegistry.addRecipe(new ItemStack(scorpHelmetDirt, 1), new Object[] { "XXX", "X X", Character.valueOf('X'), chitin });
        GameRegistry.addRecipe(new ItemStack(scorpPlateDirt, 1), new Object[] { "X X", "XXX", "XXX", Character.valueOf('X'), chitin });
        GameRegistry.addRecipe(new ItemStack(scorpLegsDirt, 1), new Object[] { "XXX", "X X", "X X", Character.valueOf('X'), chitin });
        GameRegistry.addRecipe(new ItemStack(scorpBootsDirt, 1), new Object[] { "X X", "X X", Character.valueOf('X'), chitin });

        //Other weapons
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sharksword, 1), "#X#", "#X#", " X ", Character.valueOf('#'), "toothShark", Character.valueOf('X'), "stickWood"));

        //GameRegistry.addRecipe(new ItemStack(rope, 1), new Object[] { "# #", " # ", "# #", Character.valueOf('#'), Item.silk, });

        //Staves
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(staffPortal, 1), "  E", " U ", "R  ", Character.valueOf('E'), Items.ender_eye, Character.valueOf('U'), "hornUnicorn", Character.valueOf('R'), Items.blaze_rod));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(staffPortal, 1), "  E", " U ", "R  ", Character.valueOf('E'), Items.ender_eye, Character.valueOf('U'), "essenceLight", Character.valueOf('R'), Items.blaze_rod));

        //Amulets
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(amuletbone, 1), "#X#", "XZX", "#X#", Character.valueOf('#'), Items.bone, Character.valueOf('X'), "nuggetGold", Character.valueOf('Z'), Items.ender_pearl));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(amuletghost, 1), "#X#", "XZX", "#X#", Character.valueOf('#'), Items.bone, Character.valueOf('X'), "nuggetGold", Character.valueOf('Z'), Items.ghast_tear));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(amuletfairy, 1), "#X#", "XZX", "#X#", Character.valueOf('#'), Blocks.fire, Character.valueOf('X'), "nuggetGold", Character.valueOf('Z'), "hornUnicorn"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(amuletfairy, 1), "#X#", "XZX", "#X#", Character.valueOf('#'), Blocks.fire, Character.valueOf('X'), "nuggetGold", Character.valueOf('Z'), "essenceLight"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(amuletpegasus, 1), "#X#", "XZX", "#X#", Character.valueOf('#'), Blocks.fire, Character.valueOf('X'), "nuggetGold", Character.valueOf('Z'), "gemDiamond"));

        //Misc
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(key, 1), "  #", " # ", "X  ", Character.valueOf('#'), "stickWood", Character.valueOf('X'), "ingotIron"));

        //Elephant related
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(tusksWood, 1), "X  ", "XR ", "XXX", Character.valueOf('X'), "plankWood", Character.valueOf('R'), Items.lead));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(tusksIron, 1), "X  ", "XR ", "XXX", Character.valueOf('X'), "ingotIron", Character.valueOf('R'), Items.lead));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(tusksDiamond, 1), "X  ", "XR ", "XXX", Character.valueOf('X'), "gemDiamond", Character.valueOf('R'), Items.lead));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(elephantHarness, 1), "HWH", "IWI", "HWH", Character.valueOf('H'), animalHide, Character.valueOf('W'), new ItemStack(Blocks.wool, 1, 0), Character.valueOf('I'), "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(elephantChest, 1), " W ", "CHC", " W ", Character.valueOf('H'), animalHide, Character.valueOf('W'), new ItemStack(Blocks.wool, 1, 0), Character.valueOf('C'), "chestWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(elephantGarment, 1), "pyg", "RMR", "BYB", Character.valueOf('R'), new ItemStack(Blocks.wool, 1, 14), Character.valueOf('Y'), new ItemStack(Blocks.wool, 1, 4), 
            Character.valueOf('B'), new ItemStack(Blocks.wool, 1, 11), Character.valueOf('M'), medallion, Character.valueOf('p'), "dyePink", Character.valueOf('y'), "dyeYellow", Character.valueOf('g'), "dyeLime")
        );
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(elephantHowdah, 1), "SRS", "RYR", "SRS", Character.valueOf('S'), "stickWood", Character.valueOf('R'), new ItemStack(Blocks.wool, 1, 14), Character.valueOf('Y'), new ItemStack(Blocks.wool, 1, 4)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(mammothPlatform, 1), "WRW", "PPP", "WRW",  Character.valueOf('W'), "logWood", Character.valueOf('R'), Items.lead ,  Character.valueOf('P'), "plankWood"));

        //Horse related
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(horsesaddle, 1), "X", "#", Character.valueOf('X'), Items.saddle, Character.valueOf('#'), "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(horsesaddle, 1), "XXX", "X#X", "# #", Character.valueOf('#'), "ingotIron", Character.valueOf('X'), Items.leather));
        //GameRegistry.addRecipe(new ItemStack(horsearmormetal, 1), new Object[] { "  X", "XYX", "XXX", Character.valueOf('X'), Item.ingotIron, Character.valueOf('Y'), new ItemStack(Blocks.wool, 1, 15) });
        //GameRegistry.addRecipe(new ItemStack(horsearmorgold, 1), new Object[] { "  X", "XYX", "XXX", Character.valueOf('X'), Item.ingotGold, Character.valueOf('Y'), new ItemStack(Blocks.wool, 1, 14) });
        //GameRegistry.addRecipe(new ItemStack(horsearmordiamond, 1), new Object[] { "  X", "XYX", "XXX", Character.valueOf('X'), Item.diamond, Character.valueOf('Y'), new ItemStack(Blocks.wool, 1, 11) });
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(horsearmorcrystal, 1), "  D", "CDC", "DCD", Character.valueOf('D'), "gemDiamond", Character.valueOf('C'), "blockGlassColorless"));
        GameRegistry.addRecipe(new ItemStack(haystack, 1), new Object[] { "XXX", "XXX", Character.valueOf('X'), Items.wheat });

        
        //Pet related
        GameRegistry.addRecipe(new ItemStack(woolball, 1), new Object[] { " # ", "# #", " # ", Character.valueOf('#'), Items.string });
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(litterbox, 1), "###", "#X#", "###", Character.valueOf('#'), "plankWood", Character.valueOf('X'), "sand"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(whip, 1), "#X#", "X X", "# Z", Character.valueOf('#'), "clawFelineLarge", Character.valueOf('X'), Items.leather, Character.valueOf('Z'), "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(fishnet, 1), " # ", "S#S", "#S#", Character.valueOf('#'), Items.string, Character.valueOf('S'), "toothShark"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(medallion, 1), "# #", "XZX", " X ", Character.valueOf('#'), Items.leather, Character.valueOf('Z'), "gemDiamond", Character.valueOf('X'), "ingotGold"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(petamulet, 1), "X X", " Z ", "X X", Character.valueOf('X'), "nuggetGold", Character.valueOf('Z'), "gemDiamond"));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(fishbowl_e, 1), "# #", "# #", "###", Character.valueOf('#'), "blockGlassColorless"));

        for (int i = 0; i < 16; i++) {
        	GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(kittybed, 1, i), dyeNames[i], new ItemStack(kittybed, OreDictionary.WILDCARD_VALUE)));

            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(kittybed, 1, i), "###", "#X#", "Z  ", Character.valueOf('#'), "plankWood", Character.valueOf('X'), new ItemStack(Blocks.wool, 1, MoCTools.colorize(i)), Character.valueOf('Z'), "ingotIron"));
            //String s = ItemDye.field_150923_a[i];
            //s = s.substring(0, 1).toUpperCase() + s.substring(1);
            //LanguageRegistry.addName(new ItemStack(kittybed, 1, i), (s + " Kitty Bed"));
        }

        for (int i = 0; i < multiBlockNames.size(); i++) {
            GameRegistry.addShapelessRecipe(new ItemStack(mocPlank, 4, i), new Object[] { new ItemStack(mocLog, 1, i)});
        }
    }

    public static void showCreaturePedia(EntityPlayer player, String s) {
        //TODO 4FIX        mc.displayGuiScreen(new MoCGUIPedia(s, 256, 256));
    }

    public static void showCreaturePedia(String s) {
        //TODO 4FIX        EntityPlayer entityplayer = mc.thePlayer;
        //showCreaturePedia(entityplayer, s);
    }

    public static void updateSettings() {
        proxy.readGlobalConfigValues();
    }

    public static boolean isServer() {
        return (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER);
    }

    public static boolean isHuntingEnabled() {
        return proxy.forceDespawns;
    }

}
