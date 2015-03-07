package ellpeck.someprettyrandomstuff.inventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ellpeck.someprettyrandomstuff.tile.TileEntityBase;
import ellpeck.someprettyrandomstuff.util.Util;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiGiantChest extends GuiContainer{

    private static final ResourceLocation resLoc = Util.getGuiLocation("guiGiantChest");

    public GuiGiantChest(InventoryPlayer inventory, TileEntityBase tile){
        super(new ContainerGiantChest(inventory, tile));

        this.xSize = 242;
        this.ySize = 244+86;
    }

    @Override
    public void drawGuiContainerBackgroundLayer(float f, int x, int y){
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(resLoc);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 242, 244);
        this.mc.getTextureManager().bindTexture(Util.GUI_INVENTORY_LOCATION);
        this.drawTexturedModalRect(this.guiLeft+33, this.guiTop+244, 0, 0, 176, 86);
    }
}