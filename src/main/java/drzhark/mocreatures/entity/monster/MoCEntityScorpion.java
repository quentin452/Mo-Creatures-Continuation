package drzhark.mocreatures.entity.monster;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.entity.MoCEntityMob;
import drzhark.mocreatures.entity.passive.MoCEntityPetScorpion;
import drzhark.mocreatures.network.MoCMessageHandler;
import drzhark.mocreatures.network.message.MoCMessageAnimation;

public class MoCEntityScorpion extends MoCEntityMob {
    private boolean isPoisoning;
    private int poisontimer;
    public int mouthCounter;
    public int armCounter;
    private int hideCounter;

    public MoCEntityScorpion(World world) {
        super(world);
        setSize(1.4F, 0.9F);
        poisontimer = 0;
        setAdult(true);
        setEdad(20);

        if (MoCreatures.isServer()) {
            setHasBabies(rand.nextInt(4) == 0);
        }
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(15.0D);
    }

    @Override
    public void selectType() {
        checkSpawningBiome();
        if (getType() == 0) {
            setType(1);
        }
    }

    @Override
    public ResourceLocation getTexture() {
        switch (getType()) {
        case 1:
            return MoCreatures.proxy.getTexture("scorpiondirt.png");
        case 2:
            return MoCreatures.proxy.getTexture("scorpioncave.png");
        case 3:
            return MoCreatures.proxy.getTexture("scorpionnether.png");
        case 4:
            return MoCreatures.proxy.getTexture("scorpionfrost.png");
        default:
            return MoCreatures.proxy.getTexture("scorpiondirt.png");
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(22, (byte) 0); // isPicked - 0 false 1 true
        dataWatcher.addObject(23, (byte) 0); // has babies - 0 false 1 true
    }

    public boolean getHasBabies() {
        return getIsAdult() && (dataWatcher.getWatchableObjectByte(23) == 1);
    }

    public boolean getIsPicked() {
        return (dataWatcher.getWatchableObjectByte(22) == 1);
    }

    public boolean getIsPoisoning() {
        return isPoisoning;
    }

    public void setHasBabies(boolean flag) {
        byte input = (byte) (flag ? 1 : 0);
        dataWatcher.updateObject(23, input);
    }

    public void setPicked(boolean flag) {
        byte input = (byte) (flag ? 1 : 0);
        dataWatcher.updateObject(22, input);
    }

    public void setPoisoning(boolean flag) {
        if (flag && MoCreatures.isServer()) {
            MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageAnimation(this.getEntityId(), 0), new TargetPoint(this.worldObj.provider.dimensionId, this.posX, this.posY, this.posZ, 64));
        }
        isPoisoning = flag;
    }

    @Override
    public void performAnimation(int animationType) {
    	switch (animationType) {
    	case 0: //tail animation
    		setPoisoning(true);
    		break;
    	case 1: //arm swinging
    		armCounter = 1;
            //swingArm();
    		break;
    	case 3: //movement of mouth
    		mouthCounter = 1;
    	}
    }

    @Override
    public float getMoveSpeed() {
        return 0.8F;
    }

    @Override
    public boolean isOnLadder() {
        return isCollidedHorizontally;
    }

    public boolean climbing() {
        return !onGround && isOnLadder();
    }

    /**
     * finds shelter from sunlight
     */
    private int shelterSearchTime;

    protected void findSunLightShelter() {
        Vec3 shelter = this.findPossibleShelter();

        if (shelter == null) {
            shelterSearchTime++;
            if (shelterSearchTime > 200) {
                shelterSearchTime = 0;

                if (worldObj.isDaytime()) {
                    this.setOnFire(5);
                } else {
                    int x = MathHelper.floor_double(posX + rand.nextInt(13) - 6);
                    int z = MathHelper.floor_double(posZ + rand.nextInt(13) - 6);
                    getNavigator().tryMoveToXYZ(x, posY, z, moveSpeed);
                }
            }
        } else {
            double xCoord = shelter.xCoord;
            double yCoord = shelter.yCoord;
            double zCoord = shelter.zCoord;

            if (!Double.isNaN(xCoord) && !Double.isNaN(yCoord) && !Double.isNaN(zCoord)) {
                this.getNavigator().tryMoveToXYZ(xCoord, yCoord, zCoord, this.getMoveSpeed() / 2F);
            }
        }
    }

    private void setOnFire(int duration) {
        this.setFire(duration);
    }

    /**
     * Does it want to hide?
     *
     * @return
     */
    private boolean wantsToHide() {
        return (worldObj.isDaytime()); //&& worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.posX), (int) this.boundingBox.minY, MathHelper.floor_double(this.posZ)));
    }

    private Vec3 lastDestination;
    private int ticksCounter = 0;

    @Override
    public void onLivingUpdate() {
        ticksCounter++;

        if (MoCreatures.isServer() && wantsToHide() && ticksCounter % 20 == 0) {
            Vec3 newDestination = findPossibleShelter();

            if (newDestination != null && (lastDestination == null || !newDestination.equals(lastDestination))) {
                lastDestination = newDestination;
                findSunLightShelter();
            }
        }

        if (!onGround && (ridingEntity != null)) {
            rotationYaw = ridingEntity.rotationYaw;
        }
        if (getIsAdult() && fleeingTick > 0) {
            fleeingTick = 0;
        }

        if (mouthCounter != 0 && mouthCounter++ > 50) {
            mouthCounter = 0;
        }

        if (MoCreatures.isServer() && (armCounter == 10 || armCounter == 40)) {
            worldObj.playSoundAtEntity(this, "mocreatures:scorpionclaw", 1.0F, 1.0F + ((rand.nextFloat() - rand.nextFloat()) * 0.2F));
        }

        if (armCounter != 0 && armCounter++ > 24) {
            armCounter = 0;
        }

        if (getIsPoisoning()) {
            poisontimer++;
            if (poisontimer == 1) {
                worldObj.playSoundAtEntity(this, "mocreatures:scorpionsting", 1.0F, 1.0F + ((rand.nextFloat() - rand.nextFloat()) * 0.2F));
            }
            if (poisontimer > 50) {
                poisontimer = 0;
                setPoisoning(false);
            }
        }

        if (MoCreatures.isServer() && !getIsAdult() && (rand.nextInt(200) == 0)) {
            setEdad(getEdad() + 1);
            if (getEdad() >= 120) {
                setAdult(true);
            }
        }

        super.onLivingUpdate();
    }

    @Override
    public boolean attackEntityFrom(DamageSource damagesource, float i) {
        if (super.attackEntityFrom(damagesource, i)) {
            Entity entity = damagesource.getEntity();

            if ((entity != null) && (entity != this) && (worldObj.difficultySetting.getDifficultyId() > 0) && getIsAdult()) {
                entityToAttack = entity;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected Entity findPlayerToAttack() {
    	//only attacks player at night
        if (worldObj.difficultySetting.getDifficultyId() > 0 && (!worldObj.isDaytime()) && getIsAdult()) {
            EntityPlayer entityplayer = worldObj.getClosestVulnerablePlayerToEntity(this, 12D);
            if ((entityplayer != null)) { return entityplayer; } {
                if ((rand.nextInt(80) == 0)) {
                    EntityLivingBase entityliving = getClosestEntityLiving(this, 10D);
                if (entityliving != null && !(entityliving instanceof EntityPlayer) && this.canEntityBeSeen(entityliving)) // blood - add LoS requirement
                    return entityliving;
                }
            }
        }
        return null;
    }

    @Override
    public boolean entitiesToIgnore(Entity entity) {
        return ((super.entitiesToIgnore(entity)) || (this.getIsTamed() && entity instanceof MoCEntityScorpion && ((MoCEntityScorpion) entity).getIsTamed()));
    }

    @Override
    protected void attackEntity(Entity entity, float f) {
        if ((f > 2.0F) && (f < 6F) && (rand.nextInt(50) == 0)) {
            if (onGround) {
                double d = entity.posX - posX;
                double d1 = entity.posZ - posZ;
                float f1 = MathHelper.sqrt_double((d * d) + (d1 * d1));
                motionX = ((d / f1) * 0.5D * 0.8D) + (motionX * 0.2D);
                motionZ = ((d1 / f1) * 0.5D * 0.8D) + (motionZ * 0.2D);
                motionY = 0.4D;
            }
        } else if (attackTime <= 0 && (f < 3.0D) && (entity.boundingBox.maxY > boundingBox.minY) && (entity.boundingBox.minY < boundingBox.maxY)) {
            attackTime = 20;
            boolean flag = (entity instanceof EntityPlayer);
            if (!getIsPoisoning() && rand.nextInt(5) == 0 && entity instanceof EntityLivingBase) {
                setPoisoning(true);
                switch (getType()) {
                case 0: //regular scorpions
                case 1:
                case 2:
                	((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.poison.id, 70, 0));
                	break;
                case 3: //red scorpions
                    if (MoCreatures.isServer() && !worldObj.provider.isHellWorld) {
                        ((EntityLivingBase) entity).setFire(15);
                    }
                    break;
                case 4: //blue scorpions
                	((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 70, 0));
                }
            } else {
                entity.attackEntityFrom(DamageSource.causeMobDamage(this), 1);
                swingArm();
            }
        }
    }

    public void swingArm() {
        if (MoCreatures.isServer()) {
            MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageAnimation(this.getEntityId(), 1), new TargetPoint(this.worldObj.provider.dimensionId, this.posX, this.posY, this.posZ, 64));
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    public boolean swingingTail() {
        return getIsPoisoning() && poisontimer < 15;
    }

    @Override
    public void onDeath(DamageSource damagesource) {
        super.onDeath(damagesource);

        if (MoCreatures.isServer() && getIsAdult() && getHasBabies()) {
            int k = rand.nextInt(5);
            for (int i = 0; i < k; i++) {
                MoCEntityPetScorpion entityscorpy = new MoCEntityPetScorpion(worldObj);
                entityscorpy.setPosition(posX, posY, posZ);
                entityscorpy.setAdult(false);
                entityscorpy.setType(getType());
                worldObj.spawnEntityInWorld(entityscorpy);
                worldObj.playSoundAtEntity(this, "mob.chickenplop", 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.2F) + 1.0F);
            }
        }
    }

    @Override
    protected String getDeathSound() {
        return "mocreatures:scorpiondying";
    }

    @Override
    protected String getHurtSound() {
        return "mocreatures:scorpionhurt";
    }

    @Override
    protected String getLivingSound() {
        if (MoCreatures.isServer()) {
            MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageAnimation(this.getEntityId(), 3), new TargetPoint(this.worldObj.provider.dimensionId, this.posX, this.posY, this.posZ, 64));
        }
        return "mocreatures:scorpiongrunt";
    }

    @Override
    protected Item getDropItem() {
        if (!getIsAdult()) { return Items.string; }

        boolean flag = (rand.nextInt(100) < MoCreatures.proxy.rareItemDropChance);

        switch (getType()) {
        case 1:
        	return flag ? MoCreatures.scorpStingDirt : MoCreatures.chitin;
        case 2:
        	return flag ? MoCreatures.scorpStingCave : MoCreatures.chitinCave;
        case 3:
        	return flag ? MoCreatures.scorpStingNether : MoCreatures.chitinNether;
        case 4:
        	return flag ? MoCreatures.scorpStingFrost : MoCreatures.chitinFrost;
        default:
            return Items.string;
        }
    }

    @Override
    protected void dropFewItems(boolean flag, int x) {
        if (!flag) return;
        Item item = this.getDropItem();
        if (item != null && (rand.nextInt(3) == 0)) {
                this.dropItem(item, 1);

        }
    }

    @Override
    public boolean getCanSpawnHere() {
        return (isValidLightLevel() && getCanSpawnHereLiving() && getCanSpawnHereCreature());
    }

    @Override
    public boolean checkSpawningBiome() {
        if (worldObj.provider.isHellWorld) {
            setType(3);
            isImmuneToFire = true;
            return true;
        }

        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(boundingBox.minY);
        int k = MathHelper.floor_double(posZ);

        BiomeGenBase currentbiome = MoCTools.Biomekind(worldObj, i, j, k);

        if (BiomeDictionary.isBiomeOfType(currentbiome, Type.FROZEN)) {
            setType(4);
        }
        else if (!worldObj.canBlockSeeTheSky(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)) && (posY < 50D)) {
            setType(2);
            return true;
        }
        return true;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        super.readEntityFromNBT(nbttagcompound);
        setHasBabies(nbttagcompound.getBoolean("Babies"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setBoolean("Babies", getHasBabies());
    }

    @Override
    public boolean isAIEnabled() {
        return wantsToHide() && (entityToAttack == null) && (hideCounter < 50);
    }

    @Override
    public int getTalkInterval() {
        return 300;
    }

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    public float getAdjustedYOffset() {
        return 30F;
    }

}
