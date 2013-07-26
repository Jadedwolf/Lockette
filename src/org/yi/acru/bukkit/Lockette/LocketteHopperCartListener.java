/*
 * This code was originally written Deadlock989
 * https://github.com/Deadlock989/Lockette
 */
package org.yi.acru.bukkit.Lockette;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.PluginManager;

public class LocketteHopperCartListener implements Listener {

   private static Lockette plugin;

   public LocketteHopperCartListener( Lockette instance ) {
      plugin = instance;
   }

   public void registerEvents( ) {
      PluginManager pm = plugin.getServer().getPluginManager();
      pm.registerEvents(this, plugin);
   }

   @EventHandler( priority = EventPriority.HIGHEST )
   public void onHopperCartTransaction( InventoryMoveItemEvent event ) {
      if (event.isCancelled())
         return;
      InventoryHolder sourceholder = event.getSource().getHolder();
      Block b = (( BlockState ) sourceholder).getBlock();
      if (!Lockette.isProtected(b))
         return;
      InventoryHolder destholder = event.getDestination().getHolder();
      if (!(destholder instanceof HopperMinecart))
         return;
      event.setCancelled(true);
   }

}