package drzhark.mocreatures.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public interface IMoCEntity {

    void riderIsDisconnecting(boolean flag);// = false;

    boolean forceUpdates();

    void selectType();

    String getName();

    void setName(String name);

    boolean getIsTamed();

    void setTamed(boolean flag);

    boolean getIsAdult();

    void setAdult(boolean flag);

    boolean checkSpawningBiome();

    boolean getCanSpawnHere();

    /**
     * Used to synchronize animations between server and clients
     *
     * @param i
     *            = animationType
     */
    void performAnimation(int i);

    boolean renderName();

    int nameYOffset();

    /**
     * Used to ajust the Yoffset when using ropes
     *
     * @return
     */
    double roperYOffset();

    /**
     * The entity holding the rope
     *
     * @return
     */
    Entity getRoper();

    boolean updateMount();

    /**
     * method used to sync jump client/server
     */
    void makeEntityJump();

    void makeEntityDive();

    float getSizeFactor();

    float getAdjustedYOffset();

    String getOwnerName();

    void setOwner(String username);

    void setArmorType(byte i);

    int getType();

    void setType(int i);

    void dismountEntity();

    int rollRotationOffset();

    int pitchRotationOffset();

    void setEdad(int i);

    int getEdad();

    int yawRotationOffset();

    float getAdjustedZOffset();

    float getAdjustedXOffset();

    ResourceLocation getTexture();
}
