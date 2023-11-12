package drzhark.mocreatures.utils;

import java.util.logging.Logger;

import org.apache.logging.log4j.LogManager;

import cpw.mods.fml.common.FMLLog;
import drzhark.mocreatures.MoCreatures;

public class MoCLog {

   public static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(MoCreatures.MODID);

   public static void initLog() {
       logger.info("Starting MoCreatures " + MoCreatures.VERSION);
       logger.info("Copyright (c) DrZhark, Bloodshot, BlockDaddy 2013");
   }

}
