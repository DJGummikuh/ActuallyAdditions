package ellpeck.actuallyadditions.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ellpeck.actuallyadditions.config.values.ConfigIntValues;
import ellpeck.actuallyadditions.util.INameableItem;
import ellpeck.actuallyadditions.util.ModUtil;
import ellpeck.actuallyadditions.util.WorldPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.IGrowable;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import java.util.ArrayList;
import java.util.Random;

public class ItemGrowthRing extends ItemEnergy implements INameableItem{

    private static final int RANGE = ConfigIntValues.GROWTH_RING_RANGE.getValue();
    private static final int ENERGY_USED_PER_TICK = ConfigIntValues.GROWTH_RING_ENERGY_USE.getValue();
    //The waiting time per growth cycle
    private static final int WAIT_TIME = ConfigIntValues.GROWTH_RING_COOLDOWN.getValue();
    //The amount of Growth Ticks given to random plants around
    private static final int GROWTH_TICKS_PER_CYCLE = ConfigIntValues.GROWTH_RING_GROWTH_PER_CYCLE.getValue();

    public ItemGrowthRing(){
        super(1000000, 5000, 2);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5){
        if(!(entity instanceof EntityPlayer) || world.isRemote) return;

        EntityPlayer player = (EntityPlayer)entity;
        ItemStack equipped = player.getCurrentEquippedItem();

        if(equipped != null && equipped == stack && this.getEnergyStored(stack) >= ENERGY_USED_PER_TICK){
            ArrayList<WorldPos> blocks = new ArrayList<WorldPos>();

            if(stack.stackTagCompound == null) stack.setTagCompound(new NBTTagCompound());
            int waitTime = stack.stackTagCompound.getInteger("WaitTime");

            //Adding all possible Blocks
            if(waitTime >= WAIT_TIME){
                for(int x = -RANGE; x < RANGE+1; x++){
                    for(int z = -RANGE; z < RANGE+1; z++){
                        for(int y = -RANGE; y < RANGE+1; y++){
                            int theX = (int)player.posX+x;
                            int theY = (int)player.posY+y;
                            int theZ = (int)player.posZ+z;
                            Block theBlock = world.getBlock(theX, theY, theZ);
                            if((theBlock instanceof IGrowable || theBlock instanceof IPlantable) && !(theBlock instanceof BlockGrass)){
                                blocks.add(new WorldPos(world, theX, theY, theZ));
                            }
                        }
                    }
                }

                //Fertilizing the Blocks
                if(!blocks.isEmpty()){
                    for(int i = 0; i < GROWTH_TICKS_PER_CYCLE; i++){
                        WorldPos pos = blocks.get(new Random().nextInt(blocks.size()));

                        int metaBefore = pos.getMetadata();
                        pos.getBlock().updateTick(world, pos.getX(), pos.getY(), pos.getZ(), world.rand);

                        //Show Particles if Metadata changed
                        if(pos.getMetadata() != metaBefore){
                            pos.getWorld().playAuxSFX(2005, pos.getX(), pos.getY(), pos.getZ(), 0);
                        }
                    }
                }

                stack.stackTagCompound.setInteger("WaitTime", 0);
            }
            else stack.stackTagCompound.setInteger("WaitTime", waitTime+1);

            //Use Energy every tick
            if(!player.capabilities.isCreativeMode){
                this.extractEnergy(stack, ENERGY_USED_PER_TICK, false);
            }
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack){
        return EnumRarity.epic;
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass){
        return this.itemIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconReg){
        this.itemIcon = iconReg.registerIcon(ModUtil.MOD_ID_LOWER + ":" + this.getName());
    }

    @Override
    public String getName(){
        return "itemGrowthRing";
    }
}