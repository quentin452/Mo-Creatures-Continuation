package drzhark.mocreatures.item;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drzhark.mocreatures.MoCreatures;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class MoCItemWeapon extends ItemSword {
    private int specialWeaponType = 0;
    private boolean breakable = false;

    public MoCItemWeapon(String name, ToolMaterial material) {
        super(material);
        GameRegistry.registerItem(this, name);
        this.setCreativeTab(MoCreatures.tabMoC);
        this.setUnlocalizedName(name);
    }

    /**
     * 
     * @param par1
     * @param par2ToolMaterial
     * @param damageType
     *            0 = default, 1 = poison, 2 = slow down, 3 = fire, 4 =
     *            confusion, 5 = blindness
     */
    public MoCItemWeapon(String name, ToolMaterial material, int damageType, boolean fragile) {
        this(name, material);
        this.specialWeaponType = damageType;
        this.breakable = fragile;
    }

    /**
     * Current implementations of this method in child classes do not use the
     * entry argument beside ev. They just raise the damage on the stack.
     */
    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(breakable ? 10 : 1, attacker);
        int potionTime = 100;
        switch (specialWeaponType) {
        case 1: //poison
        	target.addPotionEffect(new PotionEffect(Potion.poison.id, potionTime, 0));
            break;
        case 2: //frost slowdown
        	target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, potionTime, 0));
            break;
        case 3: //fire
        	target.setFire(10);
            break;
        case 4: //confusion
        	target.addPotionEffect(new PotionEffect(Potion.confusion.id, potionTime, 0));
            break;
        case 5: //blindness
        	target.addPotionEffect(new PotionEffect(Potion.blindness.id, potionTime, 0));
            break;
        default:
            break;
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister) {
        this.itemIcon = iconRegister.registerIcon("mocreatures"+ this.getUnlocalizedName().replaceFirst("item.", ":"));
    }

}
