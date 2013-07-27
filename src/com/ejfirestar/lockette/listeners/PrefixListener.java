//
// This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//

package com.ejfirestar.lockette.listeners;

// Imports.
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import com.ejfirestar.lockette.config.Configuration;
import com.ejfirestar.lockette.config.L10N;

public class PrefixListener implements Listener {

   @EventHandler( priority = EventPriority.LOWEST, ignoreCancelled = true )
   public void onSignChange( SignChangeEvent event ) {
      //if(event.isCancelled()) return;

      Block block = event.getBlock();
      Player player = event.getPlayer();
      boolean typeWallSign = (block.getTypeId() == Material.WALL_SIGN.getId());
      boolean typeSignPost = (block.getTypeId() == Material.SIGN_POST.getId());

      // Check to see if it is a sign change packet for an existing protected sign.
      // No longer needed in builds around 556+, but I am leaving this here for now.
      // Needed again as of build 1093...  :<

      if (typeWallSign) {
         Sign sign = ( Sign ) block.getState();
         String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "");

         if (text.equalsIgnoreCase("[Private]") || text.equalsIgnoreCase(L10N.getAltPrivate()) || text.equalsIgnoreCase("[More Users]")
               || text.equalsIgnoreCase(L10N.getAltMoreUsers())) {
            // Okay, sign already exists and someone managed to send an event to replace.
            // Cancel it!  Also, set event text to sign text, just in case.
            // And check for this later in queue.

            event.setCancelled(true);
            event.setLine(0, sign.getLine(0));
            event.setLine(1, sign.getLine(1));
            event.setLine(2, sign.getLine(2));
            event.setLine(3, sign.getLine(3));
            Bukkit.getLogger().info("[Lockette] " + player.getName() + " just tried to change a non-editable sign. (Bukkit bug, or plugin conflict?)");
            return;
         }
      }
      else if (typeSignPost) {

      }
      else {
         // Not a sign, wtf!
         event.setCancelled(true);
         Bukkit.getLogger().info("[Lockette] " + player.getName() + " just tried to set text for a non-sign. (Bukkit bug, or hacked client?)");
         return;
      }

      // Colorizer code.
      if (Configuration.useColorTags()) {
         event.setLine(0, event.getLine(0).replaceAll("&([0-9A-Fa-f])", "\u00A7$1"));
         event.setLine(1, event.getLine(1).replaceAll("&([0-9A-Fa-f])", "\u00A7$1"));
         event.setLine(2, event.getLine(2).replaceAll("&([0-9A-Fa-f])", "\u00A7$1"));
         event.setLine(3, event.getLine(3).replaceAll("&([0-9A-Fa-f])", "\u00A7$1"));
      }
   }
}
