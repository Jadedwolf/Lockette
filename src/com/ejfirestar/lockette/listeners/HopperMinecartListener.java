/*
 * This code was originally written Deadlock989
 * https://github.com/Deadlock989/Lockette
 */
package com.ejfirestar.lockette.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;

import com.ejfirestar.lockette.utils.Utils;

public class HopperMinecartListener implements Listener {

   @EventHandler( priority = EventPriority.HIGHEST )
   public void onHopperCartTransaction( InventoryMoveItemEvent event ) {
      if (event.isCancelled()) {
         return;
      }

      InventoryHolder source_holder = event.getSource().getHolder();
      InventoryHolder dest_holder = event.getDestination().getHolder();

      if (source_holder instanceof Block && dest_holder instanceof HopperMinecart) {
         if (Utils.isProtected(( Block ) source_holder)) {
            event.setCancelled(true);
         }
      }
   }
}