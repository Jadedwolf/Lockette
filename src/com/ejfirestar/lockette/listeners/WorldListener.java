//
// This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//

package com.ejfirestar.lockette.listeners;

//Imports.
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import com.ejfirestar.lockette.config.Configuration;
import com.ejfirestar.lockette.utils.Utils;

public class WorldListener implements Listener {

   @EventHandler( priority = EventPriority.LOW, ignoreCancelled = true )
   public void onStructureGrow( StructureGrowEvent event ) {
      if (event.isCancelled())
         return;

      List<BlockState> blockList = event.getBlocks();
      int x, count = blockList.size();
      Block block;

      // Check the block list for any protected blocks, and cancel the event if any are found.
      for (x = 0; x < count; ++x) {
         block = blockList.get(x).getBlock();

         if (Utils.isProtected(block)) {
            event.setCancelled(true);
            return;
         }

         if (Configuration.useExplosionProtection()) {
            if ((block.getTypeId() == Material.CHEST.getId()) || (block.getTypeId() == Material.DISPENSER.getId())
                  || (block.getTypeId() == Material.FURNACE.getId()) || (block.getTypeId() == Material.BURNING_FURNACE.getId())
                  || (block.getTypeId() == Material.BREWING_STAND.getId())) {
               event.setCancelled(true);
               return;
            }
         }
      }
   }
}
