/*
 * This file ("LivingDropEvent.java") is part of the Actually Additions Mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://github.com/Ellpeck/ActuallyAdditions/blob/master/README.md
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015 Ellpeck
 */

package ellpeck.actuallyadditions.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ellpeck.actuallyadditions.config.values.ConfigBoolValues;
import ellpeck.actuallyadditions.items.InitItems;
import ellpeck.actuallyadditions.items.metalists.TheMiscItems;
import ellpeck.actuallyadditions.items.metalists.TheSpecialDrops;
import ellpeck.actuallyadditions.util.Util;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

public class LivingDropEvent{

    @SubscribeEvent
    public void onEntityDropEvent(LivingDropsEvent event){
        if(event.source.getEntity() instanceof EntityPlayer){
            //Drop Special Items (Solidified Experience, Pearl Shards etc.)
            for(int i = 0; i < TheSpecialDrops.values().length; i++){
                TheSpecialDrops theDrop = TheSpecialDrops.values()[i];
                if(theDrop.canDrop && theDrop.dropFrom.isAssignableFrom(event.entityLiving.getClass())){
                    if(Util.RANDOM.nextInt(100)+1 <= theDrop.chance){
                        event.entityLiving.entityDropItem(new ItemStack(InitItems.itemSpecialDrop, Util.RANDOM.nextInt(theDrop.maxAmount)+1, theDrop.ordinal()), 0);
                    }
                }
            }

            //Drop Cobwebs from Spiders
            if(ConfigBoolValues.DO_SPIDER_DROPS.isEnabled() && event.entityLiving instanceof EntitySpider){
                if(Util.RANDOM.nextInt(500) <= 0){
                    event.entityLiving.entityDropItem(new ItemStack(Blocks.web, Util.RANDOM.nextInt(2)+1), 0);
                }
            }

            //Drop Wings from Bats
            if(ConfigBoolValues.DO_BAT_DROPS.isEnabled() && event.entityLiving instanceof EntityBat){
                if(Util.RANDOM.nextInt(30) <= 0){
                    event.entityLiving.entityDropItem(new ItemStack(InitItems.itemMisc, Util.RANDOM.nextInt(2)+1, TheMiscItems.BAT_WING.ordinal()), 0);
                }
            }
        }
    }
}
