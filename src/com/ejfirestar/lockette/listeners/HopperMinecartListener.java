/*
 * This code was originally written Deadlock989
 * https://github.com/Deadlock989/Lockette
 */
package com.ejfirestar.lockette.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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
      if (event.isCancelled())
         return;
      InventoryHolder sourceholder = event.getSource().getHolder();
      Block b = (( BlockState ) sourceholder).getBlock();
      if (!Utils.isProtected(b))
         return;
      InventoryHolder destholder = event.getDestination().getHolder();
      if (!(destholder instanceof HopperMinecart))
         return;
      event.setCancelled(true);
   }

}