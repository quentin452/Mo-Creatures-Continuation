package drzhark.mocreatures.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import drzhark.mocreatures.MoCreatures;

public class MultiItemBlock extends ItemBlock {

    public MultiItemBlock(Block block) {
        super(block);
        setHasSubtypes(true);
        //setItemName("multiBlock"); //TODO
        this.setUnlocalizedName("multiBlock");
    }

    @Override
    public int getMetadata (int damageValue) {
        return damageValue;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        int damage = itemstack.getItemDamage();

        if (damage >= 0 && damage < MoCreatures.multiBlockNames.size()) {
            return getUnlocalizedName() + "." + MoCreatures.multiBlockNames.get(damage);
        } else {
            return getUnlocalizedName() + ".invalidBlock";
        }
    }
}
