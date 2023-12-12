package drzhark.mocreatures.init;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import drzhark.mocreatures.entity.ambient.*;
import drzhark.mocreatures.entity.aquatic.*;
import drzhark.mocreatures.entity.monster.*;
import drzhark.mocreatures.entity.passive.*;
import drzhark.mocreatures.handlers.MoCSpawnEggsHandler;

public class EggInit {
    public static MoCSpawnEggsHandler EntityAntEgg;
    public static MoCSpawnEggsHandler EntityBeeEgg;
    public static MoCSpawnEggsHandler EntityButterflyEgg;
    public static MoCSpawnEggsHandler EntityCrabEgg;
    public static MoCSpawnEggsHandler EntityCricketEgg;
    public static MoCSpawnEggsHandler EntityDragonflyEgg;
    public static MoCSpawnEggsHandler EntityFireflyEgg;
    public static MoCSpawnEggsHandler EntityFlyEgg;
    public static MoCSpawnEggsHandler EntityMaggotEgg;
    public static MoCSpawnEggsHandler EntityRoachEgg;
    public static MoCSpawnEggsHandler EntitySnailEgg;
    public static MoCSpawnEggsHandler EntityDolphinEgg;
    public static MoCSpawnEggsHandler EntityFishyEgg;
    public static MoCSpawnEggsHandler EntityJellyFishEgg;
    public static MoCSpawnEggsHandler EntityMediumFishEgg;
    public static MoCSpawnEggsHandler EntityPiranhaEgg;
    public static MoCSpawnEggsHandler EntityRayEgg;
    public static MoCSpawnEggsHandler EntitySharkEgg;
    public static MoCSpawnEggsHandler EntitySmallFishEgg;
    public static MoCSpawnEggsHandler EntityFlameWraithEgg;
    public static MoCSpawnEggsHandler EntityGolemEgg;
    public static MoCSpawnEggsHandler EntityHellRatEgg;
    public static MoCSpawnEggsHandler EntityHorseMobEgg;
    public static MoCSpawnEggsHandler EntityMiniGolemEgg;
    public static MoCSpawnEggsHandler EntityOgreEgg;
    public static MoCSpawnEggsHandler EntityRatEgg;
    public static MoCSpawnEggsHandler EntityScorpionEgg;
    public static MoCSpawnEggsHandler EntitySilverSkeletonEgg;
    public static MoCSpawnEggsHandler EntityWraithEgg;
    public static MoCSpawnEggsHandler EntityWWolfEgg;
    public static MoCSpawnEggsHandler EntityBearEgg;
    public static MoCSpawnEggsHandler EntityBigCatEgg;
    public static MoCSpawnEggsHandler EntityBirdEgg;
    public static MoCSpawnEggsHandler EntityBoarEgg;
    public static MoCSpawnEggsHandler EntityBunnyEgg;
    public static MoCSpawnEggsHandler EntityCrocodileEgg;
    public static MoCSpawnEggsHandler EntityDeerEgg;
    public static MoCSpawnEggsHandler EntityDuckEgg;
    public static MoCSpawnEggsHandler EntityElephantEgg;
    public static MoCSpawnEggsHandler EntityEntEgg;
    public static MoCSpawnEggsHandler EntityFoxEgg;
    public static MoCSpawnEggsHandler EntityGoatEgg;
    public static MoCSpawnEggsHandler EntityHorseEgg;
    public static MoCSpawnEggsHandler EntityKittyEgg;
    public static MoCSpawnEggsHandler EntityKomodoEgg;
    public static MoCSpawnEggsHandler EntityMoleEgg;
    public static MoCSpawnEggsHandler EntityMouseEgg;
    public static MoCSpawnEggsHandler EntityOstrichEgg;
    public static MoCSpawnEggsHandler EntityRaccoonEgg;
    public static MoCSpawnEggsHandler EntitySnakeEgg;
    public static MoCSpawnEggsHandler EntityTurkeyEgg;
    public static MoCSpawnEggsHandler EntityTurtleEgg;
    public static MoCSpawnEggsHandler EntityWyvernEgg;
    public static MoCSpawnEggsHandler EntityWereWolfEgg;
    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
       /* MoCSpawnEggsHandler EntityAntEggInit = new MoCSpawnEggsHandler(MoCEntityAnt.class);
        EntityAntEggInit.setUnlocalizedName("entity_ant_egg");
        EntityAntEgg = EntityAntEggInit;
        GameRegistry.registerItem(EntityAntEgg, "Ant Egg");

        MoCSpawnEggsHandler EntityBeeEggInit = new MoCSpawnEggsHandler(MoCEntityBee.class);
        EntityBeeEggInit.setUnlocalizedName("entity_bee_egg");
        EntityBeeEgg = EntityBeeEggInit;
        GameRegistry.registerItem(EntityBeeEgg, "Bee Egg");

        MoCSpawnEggsHandler EntityButterflyEggInit = new MoCSpawnEggsHandler(MoCEntityButterfly.class);
        EntityButterflyEggInit.setUnlocalizedName("entity_butterfly_egg");
        EntityButterflyEgg = EntityButterflyEggInit;
        GameRegistry.registerItem(EntityButterflyEgg, "Butterfly Egg");

        MoCSpawnEggsHandler EntityCrabEggInit = new MoCSpawnEggsHandler(MoCEntityCrab.class);
        EntityCrabEggInit.setUnlocalizedName("entity_crab_egg");
        EntityCrabEgg = EntityCrabEggInit;
        GameRegistry.registerItem(EntityCrabEgg, "Crab Egg");

        MoCSpawnEggsHandler EntityCricketEggInit = new MoCSpawnEggsHandler(MoCEntityCricket.class);
        EntityCricketEggInit.setUnlocalizedName("entity_cricket_egg");
        EntityCricketEgg = EntityCricketEggInit;
        GameRegistry.registerItem(EntityCricketEgg, "Cricket Egg");

        MoCSpawnEggsHandler EntityDragonflyEggInit = new MoCSpawnEggsHandler(MoCEntityDragonfly.class);
        EntityDragonflyEggInit.setUnlocalizedName("entity_dragonfly_egg");
        EntityDragonflyEgg = EntityDragonflyEggInit;
        GameRegistry.registerItem(EntityDragonflyEgg, "Dragonfly Egg");

        MoCSpawnEggsHandler EntityFireflyEggInit = new MoCSpawnEggsHandler(MoCEntityFirefly.class);
        EntityFireflyEggInit.setUnlocalizedName("entity_firefly_egg");
        EntityFireflyEgg = EntityFireflyEggInit;
        GameRegistry.registerItem(EntityFireflyEgg, "Firefly Egg");

        MoCSpawnEggsHandler EntityFlyEggInit = new MoCSpawnEggsHandler(MoCEntityFly.class);
        EntityFlyEggInit.setUnlocalizedName("entity_fly_egg");
        EntityFlyEgg = EntityFlyEggInit;
        GameRegistry.registerItem(EntityFlyEgg, "Fly Egg");

        MoCSpawnEggsHandler EntityMaggotEggInit = new MoCSpawnEggsHandler(MoCEntityMaggot.class);
        EntityMaggotEggInit.setUnlocalizedName("entity_maggot_egg");
        EntityMaggotEgg = EntityMaggotEggInit;
        GameRegistry.registerItem(EntityMaggotEgg, "Maggot Egg");

        MoCSpawnEggsHandler EntityRoachEggInit = new MoCSpawnEggsHandler(MoCEntityRoach.class);
        EntityRoachEggInit.setUnlocalizedName("entity_roach_egg");
        EntityRoachEgg = EntityRoachEggInit;
        GameRegistry.registerItem(EntityRoachEgg, "Roach Egg");

        MoCSpawnEggsHandler EntitySnailEggInit = new MoCSpawnEggsHandler(MoCEntitySnail.class);
        EntitySnailEggInit.setUnlocalizedName("entity_snail_egg");
        EntitySnailEgg = EntitySnailEggInit;
        GameRegistry.registerItem(EntitySnailEgg, "Snail Egg");

        MoCSpawnEggsHandler EntityDolphinEggInit = new MoCSpawnEggsHandler(MoCEntityDolphin.class);
        EntityDolphinEggInit.setUnlocalizedName("entity_dolphin_egg");
        EntityDolphinEgg = EntityDolphinEggInit;
        GameRegistry.registerItem(EntityDolphinEgg, "Dolphin Egg");

        MoCSpawnEggsHandler EntityFishyEggInit = new MoCSpawnEggsHandler(MoCEntityFishy.class);
        EntityFishyEggInit.setUnlocalizedName("entity_fishy_egg");
        EntityFishyEgg = EntityFishyEggInit;
        GameRegistry.registerItem(EntityFishyEgg, "Fishy Egg");

        MoCSpawnEggsHandler EntityJellyFishEggInit = new MoCSpawnEggsHandler(MoCEntityJellyFish.class);
        EntityJellyFishEggInit.setUnlocalizedName("entity_jellyfish_egg");
        EntityJellyFishEgg = EntityJellyFishEggInit;
        GameRegistry.registerItem(EntityJellyFishEgg, "JellyFish Egg");

        MoCSpawnEggsHandler EntityMediumFishEggInit = new MoCSpawnEggsHandler(MoCEntityMediumFish.class);
        EntityMediumFishEggInit.setUnlocalizedName("entity_mediumfish_egg");
        EntityMediumFishEgg = EntityMediumFishEggInit;
        GameRegistry.registerItem(EntityMediumFishEgg, "MediumFish Egg");

        MoCSpawnEggsHandler EntityPiranhaEggInit = new MoCSpawnEggsHandler(MoCEntityPiranha.class);
        EntityPiranhaEggInit.setUnlocalizedName("entity_piranha_egg");
        EntityPiranhaEgg = EntityPiranhaEggInit;
        GameRegistry.registerItem(EntityPiranhaEgg, "Piranha Egg");

        MoCSpawnEggsHandler EntityRayEggInit = new MoCSpawnEggsHandler(MoCEntityRay.class);
        EntityRayEggInit.setUnlocalizedName("entity_ray_egg");
        EntityRayEgg = EntityRayEggInit;
        GameRegistry.registerItem(EntityRayEgg, "Ray Egg");

        MoCSpawnEggsHandler EntitySharkEggInit = new MoCSpawnEggsHandler(MoCEntityShark.class);
        EntitySharkEggInit.setUnlocalizedName("entity_shark_egg");
        EntitySharkEgg = EntitySharkEggInit;
        GameRegistry.registerItem(EntitySharkEgg, "Shark Egg");

        MoCSpawnEggsHandler EntitySmallFishEggInit = new MoCSpawnEggsHandler(MoCEntitySmallFish.class);
        EntitySmallFishEggInit.setUnlocalizedName("entity_smallfish_egg");
        EntitySmallFishEgg = EntitySmallFishEggInit;
        GameRegistry.registerItem(EntitySmallFishEgg, "SmallFish Egg");

        MoCSpawnEggsHandler EntityFlameWraithEggInit = new MoCSpawnEggsHandler(MoCEntityFlameWraith.class);
        EntityFlameWraithEggInit.setUnlocalizedName("entity_flamewraith_egg");
        EntityFlameWraithEgg = EntityFlameWraithEggInit;
        GameRegistry.registerItem(EntityFlameWraithEgg, "FlameWraith Egg");

        MoCSpawnEggsHandler EntityGolemEggInit = new MoCSpawnEggsHandler(MoCEntityGolem.class);
        EntityGolemEggInit.setUnlocalizedName("entity_golem_egg");
        EntityGolemEgg = EntityGolemEggInit;
        GameRegistry.registerItem(EntityGolemEgg, "Golem Egg");

        MoCSpawnEggsHandler EntityHellRatEggInit = new MoCSpawnEggsHandler(MoCEntityHellRat.class);
        EntityHellRatEggInit.setUnlocalizedName("entity_hellrat_egg");
        EntityHellRatEgg = EntityHellRatEggInit;
        GameRegistry.registerItem(EntityHellRatEgg, "HellRat Egg");

        MoCSpawnEggsHandler EntityHorseMobEggInit = new MoCSpawnEggsHandler(MoCEntityHorseMob.class);
        EntityHorseMobEggInit.setUnlocalizedName("entity_horsemob_egg");
        EntityHorseMobEgg = EntityHorseMobEggInit;
        GameRegistry.registerItem(EntityHorseMobEgg, "HorseMob Egg");

        MoCSpawnEggsHandler EntityMiniGolemEggInit = new MoCSpawnEggsHandler(MoCEntityMiniGolem.class);
        EntityMiniGolemEggInit.setUnlocalizedName("entity_minigolem_egg");
        EntityMiniGolemEgg = EntityMiniGolemEggInit;
        GameRegistry.registerItem(EntityMiniGolemEgg, "MiniGolem Egg");

        MoCSpawnEggsHandler EntityOgreEggInit = new MoCSpawnEggsHandler(MoCEntityOgre.class);
        EntityOgreEggInit.setUnlocalizedName("entity_ogre_egg");
        EntityOgreEgg = EntityOgreEggInit;
        GameRegistry.registerItem(EntityOgreEgg, "Ogre Egg");

        MoCSpawnEggsHandler EntityRatEggInit = new MoCSpawnEggsHandler(MoCEntityRat.class);
        EntityRatEggInit.setUnlocalizedName("entity_rat_egg");
        EntityRatEgg = EntityRatEggInit;
        GameRegistry.registerItem(EntityRatEgg, "Rat Egg");

        MoCSpawnEggsHandler EntityScorpionEggInit = new MoCSpawnEggsHandler(MoCEntityScorpion.class);
        EntityScorpionEggInit.setUnlocalizedName("entity_scorpion_egg");
        EntityScorpionEgg = EntityScorpionEggInit;
        GameRegistry.registerItem(EntityScorpionEgg, "Scorpion Egg");

        MoCSpawnEggsHandler EntitySilverSkeletonEggInit = new MoCSpawnEggsHandler(MoCEntitySilverSkeleton.class);
        EntitySilverSkeletonEggInit.setUnlocalizedName("entity_silverskeleton_egg");
        EntitySilverSkeletonEgg = EntitySilverSkeletonEggInit;
        GameRegistry.registerItem(EntitySilverSkeletonEgg, "SilverSkeleton Egg");

        MoCSpawnEggsHandler EntityWraithEggInit = new MoCSpawnEggsHandler(MoCEntityWraith.class);
        EntityWraithEggInit.setUnlocalizedName("entity_wraith_egg");
        EntityWraithEgg = EntityWraithEggInit;
        GameRegistry.registerItem(EntityWraithEgg, "Wraith Egg");

        MoCSpawnEggsHandler EntityWWolfEggInit = new MoCSpawnEggsHandler(MoCEntityWWolf.class);
        EntityWWolfEggInit.setUnlocalizedName("entity_wwolf_egg");
        EntityWWolfEgg = EntityWWolfEggInit;
        GameRegistry.registerItem(EntityWWolfEgg, "WWolf Egg");

        MoCSpawnEggsHandler EntityBearEggInit = new MoCSpawnEggsHandler(MoCEntityBear.class);
        EntityBearEggInit.setUnlocalizedName("entity_bear_egg");
        EntityBearEgg = EntityBearEggInit;
        GameRegistry.registerItem(EntityBearEgg, "Bear Egg");

        MoCSpawnEggsHandler EntityBigCatEggInit = new MoCSpawnEggsHandler(MoCEntityBigCat.class);
        EntityBigCatEggInit.setUnlocalizedName("entity_bigcat_egg");
        EntityBigCatEgg = EntityBigCatEggInit;
        GameRegistry.registerItem(EntityBigCatEgg, "BigCat Egg");

        MoCSpawnEggsHandler EntityBirdEggInit = new MoCSpawnEggsHandler(MoCEntityBird.class);
        EntityBirdEggInit.setUnlocalizedName("entity_bird_egg");
        EntityBirdEgg = EntityBirdEggInit;
        GameRegistry.registerItem(EntityBirdEgg, "Bird Egg");

        MoCSpawnEggsHandler EntityBoarEggInit = new MoCSpawnEggsHandler(MoCEntityBoar.class);
        EntityBoarEggInit.setUnlocalizedName("entity_boar_egg");
        EntityBoarEgg = EntityBoarEggInit;
        GameRegistry.registerItem(EntityBoarEgg, "Boar Egg");

        MoCSpawnEggsHandler EntityBunnyEggInit = new MoCSpawnEggsHandler(MoCEntityBunny.class);
        EntityBunnyEggInit.setUnlocalizedName("entity_bunny_egg");
        EntityBunnyEgg = EntityBunnyEggInit;
        GameRegistry.registerItem(EntityBunnyEgg, "Bunny Egg");

        MoCSpawnEggsHandler EntityCrocodileEggInit = new MoCSpawnEggsHandler(MoCEntityCrocodile.class);
        EntityCrocodileEggInit.setUnlocalizedName("entity_crocodile_egg");
        EntityCrocodileEgg = EntityCrocodileEggInit;
        GameRegistry.registerItem(EntityCrocodileEgg, "Crocodile Egg");

        MoCSpawnEggsHandler EntityDeerEggInit = new MoCSpawnEggsHandler(MoCEntityDeer.class);
        EntityDeerEggInit.setUnlocalizedName("entity_deer_egg");
        EntityDeerEgg = EntityDeerEggInit;
        GameRegistry.registerItem(EntityDeerEgg, "Deer Egg");

        MoCSpawnEggsHandler EntityDuckEggInit = new MoCSpawnEggsHandler(MoCEntityDuck.class);
        EntityDuckEggInit.setUnlocalizedName("entity_duck_egg");
        EntityDuckEgg = EntityDuckEggInit;
        GameRegistry.registerItem(EntityDuckEgg, "Duck Egg");

        MoCSpawnEggsHandler EntityElephantEggInit = new MoCSpawnEggsHandler(MoCEntityElephant.class);
        EntityElephantEggInit.setUnlocalizedName("entity_elephant_egg");
        EntityElephantEgg = EntityElephantEggInit;
        GameRegistry.registerItem(EntityElephantEgg, "Elephant Egg");

        MoCSpawnEggsHandler EntityEntEggInit = new MoCSpawnEggsHandler(MoCEntityEnt.class);
        EntityEntEggInit.setUnlocalizedName("entity_ent_egg");
        EntityEntEgg = EntityEntEggInit;
        GameRegistry.registerItem(EntityEntEgg, "Ent Egg");

        MoCSpawnEggsHandler EntityFoxEggInit = new MoCSpawnEggsHandler(MoCEntityFox.class);
        EntityFoxEggInit.setUnlocalizedName("entity_fox_egg");
        EntityFoxEgg = EntityFoxEggInit;
        GameRegistry.registerItem(EntityFoxEgg, "Fox Egg");

        MoCSpawnEggsHandler EntityGoatEggInit = new MoCSpawnEggsHandler(MoCEntityGoat.class);
        EntityGoatEggInit.setUnlocalizedName("entity_goat_egg");
        EntityGoatEgg = EntityGoatEggInit;
        GameRegistry.registerItem(EntityGoatEgg, "Goat Egg");

        MoCSpawnEggsHandler EntityHorseEggInit = new MoCSpawnEggsHandler(MoCEntityHorse.class);
        EntityHorseEggInit.setUnlocalizedName("entity_horse_egg");
        EntityHorseEgg = EntityHorseEggInit;
        GameRegistry.registerItem(EntityHorseEgg, "Horse Egg");

        MoCSpawnEggsHandler EntityKittyEggInit = new MoCSpawnEggsHandler(MoCEntityKitty.class);
        EntityKittyEggInit.setUnlocalizedName("entity_kitty_egg");
        EntityKittyEgg = EntityKittyEggInit;
        GameRegistry.registerItem(EntityKittyEgg, "Kitty Egg");

        MoCSpawnEggsHandler EntityKomodoEggInit = new MoCSpawnEggsHandler(MoCEntityKomodo.class);
        EntityKomodoEggInit.setUnlocalizedName("entity_komodo_egg");
        EntityKomodoEgg = EntityKomodoEggInit;
        GameRegistry.registerItem(EntityKomodoEgg, "Komodo Egg");

        MoCSpawnEggsHandler EntityMoleEggInit = new MoCSpawnEggsHandler(MoCEntityMole.class);
        EntityMoleEggInit.setUnlocalizedName("entity_mole_egg");
        EntityMoleEgg = EntityMoleEggInit;
        GameRegistry.registerItem(EntityMoleEgg, "Mole Egg");

        MoCSpawnEggsHandler EntityMouseEggInit = new MoCSpawnEggsHandler(MoCEntityMouse.class);
        EntityMouseEggInit.setUnlocalizedName("entity_mouse_egg");
        EntityMouseEgg = EntityMouseEggInit;
        GameRegistry.registerItem(EntityMouseEgg, "Mouse Egg");

        MoCSpawnEggsHandler EntityOstrichEggInit = new MoCSpawnEggsHandler(MoCEntityOstrich.class);
        EntityOstrichEggInit.setUnlocalizedName("entity_ostrich_egg");
        EntityOstrichEgg = EntityOstrichEggInit;
        GameRegistry.registerItem(EntityOstrichEgg, "Ostrich Egg");

        MoCSpawnEggsHandler EntityRaccoonEggInit = new MoCSpawnEggsHandler(MoCEntityRaccoon.class);
        EntityRaccoonEggInit.setUnlocalizedName("entity_raccoon_egg");
        EntityRaccoonEgg = EntityRaccoonEggInit;
        GameRegistry.registerItem(EntityRaccoonEgg, "Raccoon Egg");

        MoCSpawnEggsHandler EntitySnakeEggInit = new MoCSpawnEggsHandler(MoCEntitySnake.class);
        EntitySnakeEggInit.setUnlocalizedName("entity_snake_egg");
        EntitySnakeEgg = EntitySnakeEggInit;
        GameRegistry.registerItem(EntitySnakeEgg, "Snake Egg");

        MoCSpawnEggsHandler EntityTurkeyEggInit = new MoCSpawnEggsHandler(MoCEntityTurkey.class);
        EntityTurkeyEggInit.setUnlocalizedName("entity_turkey_egg");
        EntityTurkeyEgg = EntityTurkeyEggInit;
        GameRegistry.registerItem(EntityTurkeyEgg, "Turkey Egg");

        MoCSpawnEggsHandler EntityTurtleEggInit = new MoCSpawnEggsHandler(MoCEntityTurtle.class);
        EntityTurtleEggInit.setUnlocalizedName("entity_turtle_egg");
        EntityTurtleEgg = EntityTurtleEggInit;
        GameRegistry.registerItem(EntityTurtleEgg, "Turtle Egg");

        MoCSpawnEggsHandler EntityWyvernEggInit = new MoCSpawnEggsHandler(MoCEntityWyvern.class);
        EntityWyvernEggInit.setUnlocalizedName("entity_wyvern_egg");
        EntityWyvernEgg = EntityWyvernEggInit;
        GameRegistry.registerItem(EntityWyvernEgg, "Wyvern Egg");

        MoCSpawnEggsHandler EntityWerewolfEggInit = new MoCSpawnEggsHandler(MoCEntityWerewolf.class);
        EntityWerewolfEggInit.setUnlocalizedName("entity_werewolf_egg");
        EntityWereWolfEgg = EntityWerewolfEggInit;
        GameRegistry.registerItem(EntityWereWolfEgg, "Werewolf Egg");

        */
    }
}
