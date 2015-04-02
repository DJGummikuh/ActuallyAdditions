package ellpeck.actuallyadditions.inventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ellpeck.actuallyadditions.inventory.slot.SlotOutput;
import ellpeck.actuallyadditions.tile.TileEntityBase;
import ellpeck.actuallyadditions.tile.TileEntityItemRepairer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class ContainerRepairer extends Container{

    private TileEntityItemRepairer tileRepairer;

    private int lastCoalTime;
    private int lastCoalTimeLeft;

    public ContainerRepairer(InventoryPlayer inventory, TileEntityBase tile){
        this.tileRepairer = (TileEntityItemRepairer)tile;

        this.addSlotToContainer(new Slot(this.tileRepairer, TileEntityItemRepairer.SLOT_COAL, 80, 21));

        this.addSlotToContainer(new Slot(this.tileRepairer, TileEntityItemRepairer.SLOT_INPUT, 47, 53));
        this.addSlotToContainer(new SlotOutput(this.tileRepairer, TileEntityItemRepairer.SLOT_OUTPUT, 109, 53));

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 9; j++){
                this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 97 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++){
            this.addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 155));
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting iCraft){
        super.addCraftingToCrafters(iCraft);
        iCraft.sendProgressBarUpdate(this, 0, this.tileRepairer.coalTime);
        iCraft.sendProgressBarUpdate(this, 1, this.tileRepairer.coalTimeLeft);
    }

    @Override
    public void detectAndSendChanges(){
        super.detectAndSendChanges();
        for(Object crafter : this.crafters){
            ICrafting iCraft = (ICrafting)crafter;

            if(this.lastCoalTime != this.tileRepairer.coalTime) iCraft.sendProgressBarUpdate(this, 0, this.tileRepairer.coalTime);
            if(this.lastCoalTimeLeft != this.tileRepairer.coalTimeLeft) iCraft.sendProgressBarUpdate(this, 1, this.tileRepairer.coalTimeLeft);
        }

        this.lastCoalTime = this.tileRepairer.coalTime;
        this.lastCoalTimeLeft = this.tileRepairer.coalTimeLeft;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int par1, int par2){
        if(par1 == 0) this.tileRepairer.coalTime = par2;
        if(par1 == 1) this.tileRepairer.coalTimeLeft = par2;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player){
        return this.tileRepairer.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot){
        final int inventoryStart = 3;
        final int inventoryEnd = inventoryStart+26;
        final int hotbarStart = inventoryEnd+1;
        final int hotbarEnd = hotbarStart+8;

        Slot theSlot = (Slot)this.inventorySlots.get(slot);
        if(theSlot.getHasStack()){
            ItemStack currentStack = theSlot.getStack();
            ItemStack newStack = currentStack.copy();

            if(slot <= hotbarEnd && slot >= inventoryStart){
                if(TileEntityItemRepairer.canBeRepaired(currentStack)){
                    this.mergeItemStack(newStack, TileEntityItemRepairer.SLOT_INPUT, TileEntityItemRepairer.SLOT_INPUT+1, false);
                }

                if(TileEntityFurnace.getItemBurnTime(currentStack) > 0){
                    this.mergeItemStack(newStack, TileEntityItemRepairer.SLOT_COAL, TileEntityItemRepairer.SLOT_COAL+1, false);
                }
            }

            if(slot <= hotbarEnd && slot >= hotbarStart){
                this.mergeItemStack(newStack, inventoryStart, inventoryEnd+1, false);
            }

            else if(slot <= inventoryEnd && slot >= inventoryStart){
                this.mergeItemStack(newStack, hotbarStart, hotbarEnd+1, false);
            }

            else if(slot < inventoryStart){
                this.mergeItemStack(newStack, inventoryStart, hotbarEnd+1, false);
            }

            if(newStack.stackSize == 0) theSlot.putStack(null);
            else theSlot.onSlotChanged();
            if(newStack.stackSize == currentStack.stackSize) return null;
            theSlot.onPickupFromSlot(player, newStack);

            return currentStack;
        }
        return null;
    }
}