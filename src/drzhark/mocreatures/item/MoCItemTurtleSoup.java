package drzhark.mocreatures.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MoCItemTurtleSoup extends MoCItemFood {

    public MoCItemTurtleSoup(String name, int j) {
        super(name, j);
        maxStackSize = 1;
    }

    public MoCItemTurtleSoup(String name, int j, float f, boolean doWolvesLike) {
        super(name, j, f, doWolvesLike);
        maxStackSize = 1;
    }

    @Override
    public ItemStack onEaten(ItemStack stack, World worldIn, EntityPlayer entityPlayer) {
        super.onEaten(stack, worldIn, entityPlayer);
        return new ItemStack(Items.bowl);
    }

}
