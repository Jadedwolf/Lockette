//
// This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//

package com.ejfirestar.lockette.listeners;

// Imports.
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.ejfirestar.lockette.config.Configuration;
import com.ejfirestar.lockette.utils.Utils;

public class EntityExplodeListener implements Listener {

   @EventHandler( priority = EventPriority.LOW, ignoreCancelled = true )
   public void onEntityExplode( EntityExplodeEvent event ) {
      if (event.isCancelled())
         return;

      if (Configuration.useExplosionProtection()) {
         // Check the block list for any protected blocks, and cancel the event if any are found.
         for (Block block : event.blockList()) {

            if ( Utils.isProtected(block) && 
                 (block.getTypeId() == Material.CHEST.getId() || 
                  block.getTypeId() == Material.DISPENSER.getId() || 
                  block.getTypeId() == Material.FURNACE.getId() ||
                  block.getTypeId() == Material.BURNING_FURNACE.getId() || 
                  block.getTypeId() == Material.BREWING_STAND.getId()) ) {
               
               event.blockList().remove(block);
            }
         }
      }
   }
}
