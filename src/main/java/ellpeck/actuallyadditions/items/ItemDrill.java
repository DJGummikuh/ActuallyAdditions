/*
 * This file ("ItemDrill.java") is part of the Actually Additions Mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://github.com/Ellpeck/ActuallyAdditions/blob/master/README.md
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015 Ellpeck
 */

package ellpeck.actuallyadditions.items;

import cofh.api.energy.IEnergyContainerItem;
import com.google.common.collect.Multimap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ellpeck.actuallyadditions.ActuallyAdditions;
import ellpeck.actuallyadditions.config.ConfigValues;
import ellpeck.actuallyadditions.inventory.GuiHandler;
import ellpeck.actuallyadditions.util.ItemUtil;
import ellpeck.actuallyadditions.util.ModUtil;
import ellpeck.actuallyadditions.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class ItemDrill extends ItemEnergy{

    @SideOnly(Side.CLIENT)
    private IIcon emeraldIcon;
    @SideOnly(Side.CLIENT)
    private IIcon purpleIcon;

    private static final int ENERGY_USE = 100;

    public ItemDrill(){
        super(500000, 5000);
        this.setMaxDamage(0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int par1){
        return par1 == 0 ? this.itemIcon : (par1 == 2 ? this.purpleIcon : this.emeraldIcon);
    }

    @Override
    public int getMetadata(int damage){
        return damage;
    }

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tabs, List list){
        super.getSubItems(item, tabs, list);

        this.addDrillStack(list, 1);
        this.addDrillStack(list, 2);
    }

    private void addDrillStack(List list, int meta){
        ItemStack stackFull = new ItemStack(this, 1, meta);
        this.setEnergy(stackFull, this.getMaxEnergyStored(stackFull));
        list.add(stackFull);

        ItemStack stackEmpty = new ItemStack(this, 1, meta);
        this.setEnergy(stackEmpty, 0);
        list.add(stackEmpty);
    }

    @Override
    //Places Blocks if the Placing Upgrade is installed
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int hitSide, float hitX, float hitY, float hitZ){
        ItemStack upgrade = this.getHasUpgradeAsStack(stack, ItemDrillUpgrade.UpgradeType.PLACER);
        if(upgrade != null){
            int slot = ItemDrillUpgrade.getSlotToPlaceFrom(upgrade);
            if(slot >= 0 && slot < InventoryPlayer.getHotbarSize()){
                ItemStack anEquip = player.inventory.getStackInSlot(slot);
                if(anEquip != null && anEquip != stack){
                    ItemStack equip = anEquip.copy();
                    if(!world.isRemote){
                        //tryPlaceItemIntoWorld could throw an Exception
                        try{
                            //Places the Block into the World
                            if(equip.tryPlaceItemIntoWorld(player, world, x, y, z, hitSide, hitX, hitY, hitZ)){
                                if(!player.capabilities.isCreativeMode){
                                    player.inventory.setInventorySlotContents(slot, equip.stackSize <= 0 ? null : equip.copy());
                                }
                                //Synchronizes the Client
                                player.inventoryContainer.detectAndSendChanges();
                                return true;
                            }
                        }
                        //Notify the Player and log the Exception
                        catch(Exception e){
                            player.addChatComponentMessage(new ChatComponentText("Ouch! That really hurt! You must have done something wrong, don't do that again please!"));
                            ModUtil.LOGGER.error("Player "+player.getCommandSenderName()+" who should place a Block using a Drill at "+player.posX+", "+player.posY+", "+player.posZ+" in World "+world.provider.dimensionId+" threw an Exception! Don't let that happen again!");
                        }
                    }
                    else{
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if a certain Upgrade is installed and returns it as an ItemStack
     *
     * @param stack   The Drill
     * @param upgrade The Upgrade to be checked
     * @return The Upgrade, if it's installed
     */
    public ItemStack getHasUpgradeAsStack(ItemStack stack, ItemDrillUpgrade.UpgradeType upgrade){
        NBTTagCompound compound = stack.getTagCompound();
        if(compound == null){
            return null;
        }

        ItemStack[] slots = this.getSlotsFromNBT(stack);
        if(slots != null && slots.length > 0){
            for(ItemStack slotStack : slots){
                if(slotStack != null && slotStack.getItem() instanceof ItemDrillUpgrade){
                    if(((ItemDrillUpgrade)slotStack.getItem()).type == upgrade){
                        return slotStack;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets all of the Slots from NBT
     *
     * @param stack The Drill
     * @return All of the Slots
     */
    public ItemStack[] getSlotsFromNBT(ItemStack stack){
        NBTTagCompound compound = stack.getTagCompound();
        if(compound == null){
            return null;
        }

        int slotAmount = compound.getInteger("SlotAmount");
        ItemStack[] slots = new ItemStack[slotAmount];

        if(slots.length > 0){
            NBTTagList tagList = compound.getTagList("Items", 10);
            for(int i = 0; i < tagList.tagCount(); i++){
                NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
                byte slotIndex = tagCompound.getByte("Slot");
                if(slotIndex >= 0 && slotIndex < slots.length){
                    slots[slotIndex] = ItemStack.loadItemStackFromNBT(tagCompound);
                }
            }
        }
        return slots;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
        if(!world.isRemote && player.isSneaking() && stack == player.getCurrentEquippedItem()){
            player.openGui(ActuallyAdditions.instance, GuiHandler.GuiTypes.DRILL.ordinal(), world, (int)player.posX, (int)player.posY, (int)player.posZ);
        }
        return stack;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase entity1, EntityLivingBase entity2){
        int use = this.getEnergyUsePerBlock(stack);
        if(this.getEnergyStored(stack) >= use){
            this.extractEnergy(stack, use, false);
        }
        return true;
    }

    //Checks for Energy Containers in the Upgrade Slots and charges the Drill from them
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5){
        ItemStack[] slots = this.getSlotsFromNBT(stack);
        if(slots != null && slots.length > 0){
            for(ItemStack slotStack : slots){
                if(slotStack != null && slotStack.getItem() instanceof IEnergyContainerItem){
                    if(this.getEnergyStored(stack) < this.getMaxEnergyStored(stack)){
                        int energy = ((IEnergyContainerItem)slotStack.getItem()).getEnergyStored(slotStack);
                        if(energy > 0){
                            //Charge the Drill and discharge the "Upgrade"
                            int toReceive = ((IEnergyContainerItem)stack.getItem()).receiveEnergy(stack, energy, true);
                            int actualReceive = ((IEnergyContainerItem)slotStack.getItem()).extractEnergy(slotStack, toReceive, false);
                            ((IEnergyContainerItem)stack.getItem()).receiveEnergy(stack, actualReceive, false);

                        }
                    }
                }
            }
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack){
        return EnumRarity.epic;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconReg){
        this.itemIcon = iconReg.registerIcon(ModUtil.MOD_ID_LOWER+":"+this.getName());
        this.emeraldIcon = iconReg.registerIcon(ModUtil.MOD_ID_LOWER+":"+this.getName()+"Emerald");
        this.purpleIcon = iconReg.registerIcon(ModUtil.MOD_ID_LOWER+":"+this.getName()+"Purple");
    }

    @Override
    public String getName(){
        return "itemDrill";
    }

    @Override
    public Multimap getAttributeModifiers(ItemStack stack){
        Multimap map = super.getAttributeModifiers(stack);
        map.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Drill Modifier", this.getEnergyStored(stack) >= ENERGY_USE ? 8.0F : 0.1F, 0));
        return map;
    }

    @Override
    public float getDigSpeed(ItemStack stack, Block block, int meta){
        return this.getEnergyStored(stack) >= this.getEnergyUsePerBlock(stack) ? (this.hasExtraWhitelist(block) || block.getHarvestTool(meta) == null || block.getHarvestTool(meta).isEmpty() || this.getToolClasses(stack).contains(block.getHarvestTool(meta)) ? this.getEfficiencyFromUpgrade(stack) : 1.0F) : 0.1F;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player){
        boolean toReturn = false;
        int use = this.getEnergyUsePerBlock(stack);
        if(this.getEnergyStored(stack) >= use){
            //Enchants the Drill depending on the Upgrades it has
            if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.SILK_TOUCH)){
                ItemUtil.addEnchantment(stack, Enchantment.silkTouch, 1);
            }
            else{
                if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.FORTUNE)){
                    ItemUtil.addEnchantment(stack, Enchantment.fortune, this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.FORTUNE_II) ? 3 : 1);
                }
            }

            //Breaks the Blocks
            if(!player.isSneaking() && this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.THREE_BY_THREE)){
                if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.FIVE_BY_FIVE)){
                    toReturn = this.breakBlocks(stack, 2, player.worldObj, x, y, z, player);
                }
                else{
                    toReturn = this.breakBlocks(stack, 1, player.worldObj, x, y, z, player);
                }
            }
            else{
                toReturn = this.breakBlocks(stack, 0, player.worldObj, x, y, z, player);
            }

            //Removes Enchantments added above
            ItemUtil.removeEnchantment(stack, Enchantment.silkTouch);
            ItemUtil.removeEnchantment(stack, Enchantment.fortune);
        }
        return toReturn;
    }

    @Override
    public boolean canHarvestBlock(Block block, ItemStack stack){
        int harvestLevel = this.getHarvestLevel(stack, "");
        return this.getEnergyStored(stack) >= this.getEnergyUsePerBlock(stack) && (this.hasExtraWhitelist(block) || block.getMaterial().isToolNotRequired() || (block == Blocks.snow_layer || block == Blocks.snow || (block == Blocks.obsidian ? harvestLevel >= 3 : (block != Blocks.diamond_block && block != Blocks.diamond_ore ? (block != Blocks.emerald_ore && block != Blocks.emerald_block ? (block != Blocks.gold_block && block != Blocks.gold_ore ? (block != Blocks.iron_block && block != Blocks.iron_ore ? (block != Blocks.lapis_block && block != Blocks.lapis_ore ? (block != Blocks.redstone_ore && block != Blocks.lit_redstone_ore ? (block.getMaterial() == Material.rock || (block.getMaterial() == Material.iron || block.getMaterial() == Material.anvil)) : harvestLevel >= 2) : harvestLevel >= 1) : harvestLevel >= 1) : harvestLevel >= 2) : harvestLevel >= 2) : harvestLevel >= 2))));
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack){
        HashSet<String> hashSet = new HashSet<String>();
        hashSet.add("pickaxe");
        hashSet.add("shovel");
        return hashSet;
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass){
        return 4;
    }

    /**
     * Gets the Energy that is used per Block broken
     *
     * @param stack The Drill
     * @return The Energy use per Block
     */
    public int getEnergyUsePerBlock(ItemStack stack){
        int use = ENERGY_USE;

        //Speed
        if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.SPEED)){
            use += 50;
            if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.SPEED_II)){
                use += 75;
                if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.SPEED_III)){
                    use += 175;
                }
            }
        }

        //Silk Touch
        if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.SILK_TOUCH)){
            use += 100;
        }

        //Fortune
        if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.FORTUNE)){
            use += 40;
            if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.FORTUNE_II)){
                use += 80;
            }
        }

        //Size
        if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.THREE_BY_THREE)){
            use += 10;
            if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.FIVE_BY_FIVE)){
                use += 30;
            }
        }

        return use;
    }

    /**
     * Checks if a certain Upgrade is applied
     *
     * @param stack   The Drill
     * @param upgrade The Upgrade to be checked
     * @return Is the Upgrade applied?
     */
    public boolean getHasUpgrade(ItemStack stack, ItemDrillUpgrade.UpgradeType upgrade){
        return this.getHasUpgradeAsStack(stack, upgrade) != null;
    }

    /**
     * Gets the Mining Speed of the Drill
     *
     * @param stack The Drill
     * @return The Mining Speed depending on the Speed Upgrades
     */
    public float getEfficiencyFromUpgrade(ItemStack stack){
        float efficiency = 8.0F;
        if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.SPEED)){
            if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.SPEED_II)){
                if(this.getHasUpgrade(stack, ItemDrillUpgrade.UpgradeType.SPEED_III)){
                    efficiency += 37.0F;
                }
                else{
                    efficiency += 25.0F;
                }
            }
            else{
                efficiency += 8.0F;
            }
        }
        return efficiency;
    }

    /**
     * Writes all of the Slots to NBT
     *
     * @param slots The Slots
     * @param stack The Drill
     */
    public void writeSlotsToNBT(ItemStack[] slots, ItemStack stack){
        NBTTagCompound compound = stack.getTagCompound();
        if(compound == null){
            compound = new NBTTagCompound();
        }

        if(slots != null && slots.length > 0){
            compound.setInteger("SlotAmount", slots.length);
            NBTTagList tagList = new NBTTagList();
            for(int currentIndex = 0; currentIndex < slots.length; currentIndex++){
                if(slots[currentIndex] != null){
                    NBTTagCompound tagCompound = new NBTTagCompound();
                    tagCompound.setByte("Slot", (byte)currentIndex);
                    slots[currentIndex].writeToNBT(tagCompound);
                    tagList.appendTag(tagCompound);
                }
            }
            compound.setTag("Items", tagList);
        }
        stack.setTagCompound(compound);
    }

    /**
     * Breaks Blocks in a certain Radius
     * Has to be called on both Server and Client
     *
     * @param stack  The Drill
     * @param radius The Radius to break Blocks in (0 means only 1 Block will be broken!)
     * @param world  The World
     * @param x      The X Coord of the main Block to break
     * @param y      The Y Coord of the main Block to break
     * @param z      The Z Coord of the main Block to break
     * @param player The Player who breaks the Blocks
     */
    public boolean breakBlocks(ItemStack stack, int radius, World world, int x, int y, int z, EntityPlayer player){
        int xRange = radius;
        int yRange = radius;
        int zRange = 0;

        //Block hit
        MovingObjectPosition pos = WorldUtil.getNearestBlockWithDefaultReachDistance(world, player);
        if(pos == null){
            return false;
        }

        //Corrects Blocks to hit depending on Side of original Block hit
        int side = pos.sideHit;
        if(side == 0 || side == 1){
            zRange = radius;
            yRange = 0;
        }
        if(side == 4 || side == 5){
            xRange = 0;
            zRange = radius;
        }

        //Not defined later because main Block is getting broken below
        float mainHardness = world.getBlock(x, y, z).getBlockHardness(world, x, y, z);

        //Break Middle Block first
        int use = this.getEnergyUsePerBlock(stack);
        if(this.getEnergyStored(stack) >= use){
            if(!this.tryHarvestBlock(world, x, y, z, false, stack, player, use)){
                return false;
            }
        }
        else{
            return false;
        }

        //Break Blocks around
        if(radius > 0 && mainHardness >= 0.2F){
            for(int xPos = x-xRange; xPos <= x+xRange; xPos++){
                for(int yPos = y-yRange; yPos <= y+yRange; yPos++){
                    for(int zPos = z-zRange; zPos <= z+zRange; zPos++){
                        if(!(x == xPos && y == yPos && z == zPos)){
                            if(this.getEnergyStored(stack) >= use){
                                //Only break Blocks around that are (about) as hard or softer
                                if(world.getBlock(xPos, yPos, zPos).getBlockHardness(world, xPos, yPos, zPos) <= mainHardness+5.0F){
                                    this.tryHarvestBlock(world, xPos, yPos, zPos, true, stack, player, use);
                                }
                            }
                            else{
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Tries to harvest a certain Block
     * Breaks the Block, drops Particles etc.
     * Has to be called on both Server and Client
     *
     * @param world   The World
     * @param xPos    The X Position of the Block to break
     * @param yPos    The Y Position of the Block to break
     * @param zPos    The Z Position of the Block to break
     * @param isExtra If the Block is the Block that was looked at when breaking or an additional Block
     * @param stack   The Drill
     * @param player  The Player breaking the Blocks
     * @param use     The Energy that should be extracted per Block
     */
    private boolean tryHarvestBlock(World world, int xPos, int yPos, int zPos, boolean isExtra, ItemStack stack, EntityPlayer player, int use){
        Block block = world.getBlock(xPos, yPos, zPos);
        float hardness = block.getBlockHardness(world, xPos, yPos, zPos);
        int meta = world.getBlockMetadata(xPos, yPos, zPos);
        boolean canHarvest = (ForgeHooks.canHarvestBlock(block, player, meta) || this.canHarvestBlock(block, stack)) && (!isExtra || this.getDigSpeed(stack, block, meta) > 1.0F);
        if(hardness >= 0.0F && (!isExtra || (canHarvest && !block.hasTileEntity(meta)))){
            this.extractEnergy(stack, use, false);
            //Break the Block
            return WorldUtil.playerHarvestBlock(world, xPos, yPos, zPos, player);
        }
        return false;
    }

    private boolean hasExtraWhitelist(Block block){
        String name = Block.blockRegistry.getNameForObject(block);
        if(name != null){
            for(String list : ConfigValues.drillExtraminingWhitelist){
                if(list.equals(name)){
                    return true;
                }
            }
        }
        return false;
    }
}
