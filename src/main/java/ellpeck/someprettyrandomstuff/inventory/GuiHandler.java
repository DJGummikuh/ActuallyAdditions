package ellpeck.someprettyrandomstuff.inventory;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import ellpeck.someprettyrandomstuff.SomePrettyRandomStuff;
import ellpeck.someprettyrandomstuff.tile.TileEntityBase;
import ellpeck.someprettyrandomstuff.tile.TileEntityGrinder;
import ellpeck.someprettyrandomstuff.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler{

    @Override
    public Object getServerGuiElement(int id, EntityPlayer entityPlayer, World world, int x, int y, int z){
        switch (id){
            case FEEDER_ID:
                TileEntityBase tileFeeder = (TileEntityBase)world.getTileEntity(x, y, z);
                return new ContainerFeeder(entityPlayer.inventory, tileFeeder);
            case GIANT_CHEST_ID:
                TileEntityBase tileChest = (TileEntityBase)world.getTileEntity(x, y, z);
                return new ContainerGiantChest(entityPlayer.inventory, tileChest);
            case CRAFTER_ID:
                return new ContainerCrafter(entityPlayer);
            case GRINDER_ID:
                TileEntityBase tileGrinder = (TileEntityGrinder)world.getTileEntity(x, y, z);
                return new ContainerGrinder(entityPlayer.inventory, tileGrinder, false);
            case GRINDER_DOUBLE_ID:
                TileEntityBase tileGrinderDouble = (TileEntityGrinder)world.getTileEntity(x, y, z);
                return new ContainerGrinder(entityPlayer.inventory, tileGrinderDouble, true);
            case FURNACE_DOUBLE_ID:
                TileEntityBase tileFurnace = (TileEntityBase)world.getTileEntity(x, y, z);
                return new ContainerFurnaceDouble(entityPlayer.inventory, tileFurnace);
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer entityPlayer, World world, int x, int y, int z){
        switch (id){
            case FEEDER_ID:
                TileEntityBase tileFeeder = (TileEntityBase)world.getTileEntity(x, y, z);
                return new GuiFeeder(entityPlayer.inventory, tileFeeder);
            case GIANT_CHEST_ID:
                TileEntityBase tileChest = (TileEntityBase)world.getTileEntity(x, y, z);
                return new GuiGiantChest(entityPlayer.inventory, tileChest);
            case CRAFTER_ID:
                return new GuiCrafter(entityPlayer);
            case GRINDER_ID:
                TileEntityBase tileGrinder = (TileEntityGrinder)world.getTileEntity(x, y, z);
                return new GuiGrinder(entityPlayer.inventory, tileGrinder, false);
            case GRINDER_DOUBLE_ID:
                TileEntityBase tileGrinderDouble = (TileEntityGrinder)world.getTileEntity(x, y, z);
                return new GuiGrinder(entityPlayer.inventory, tileGrinderDouble, true);
            case FURNACE_DOUBLE_ID:
                TileEntityBase tileFurnace = (TileEntityBase)world.getTileEntity(x, y, z);
                return new GuiFurnaceDouble(entityPlayer.inventory, tileFurnace);
            default:
                return null;
        }
    }

    public static final int FEEDER_ID = 0;
    public static final int GIANT_CHEST_ID = 1;
    public static final int CRAFTER_ID = 2;
    public static final int GRINDER_ID = 3;
    public static final int GRINDER_DOUBLE_ID = 4;
    public static final int FURNACE_DOUBLE_ID = 5;

    public static void init(){
        Util.logInfo("Initializing GuiHandler...");
        NetworkRegistry.INSTANCE.registerGuiHandler(SomePrettyRandomStuff.instance, new GuiHandler());
    }
}