package com.ejfirestar.lockette;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.yi.acru.bukkit.PluginCore;

import com.ejfirestar.lockette.config.Configuration;
import com.ejfirestar.lockette.listeners.BlockListener;
import com.ejfirestar.lockette.listeners.EntityExplodeListener;
import com.ejfirestar.lockette.listeners.HopperMinecartListener;
import com.ejfirestar.lockette.listeners.PlayerListener;
import com.ejfirestar.lockette.listeners.PrefixListener;
import com.ejfirestar.lockette.listeners.WorldListener;

/**
 * @author ioncann0ns
 * @author 00firestar00
 */
public class Lockette extends PluginCore {
   
   private Level log_level = Level.ALL;
   private DoorCloser door_closer = new DoorCloser(this);

   @Override
   public void onEnable( ) {
      /* Load Configuration */
      Bukkit.getLogger().setLevel(log_level);
      Configuration.loadConfig(this);

      // Start a scheduled task, for closing doors.
      if (Configuration.protectDoors() || Configuration.protectTrapdoors()) {
         if (door_closer.start()) {
            Bukkit.getLogger().severe("[" + getDescription().getName() + "] Failed to register door closing task!");
         }
      }
      else {
         door_closer.stop();
      }

      /* Register Listeners */
      Bukkit.getPluginManager().registerEvents(new BlockListener(this), this);
      Bukkit.getPluginManager().registerEvents(new EntityExplodeListener(), this);
      Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
      Bukkit.getPluginManager().registerEvents(new PrefixListener(), this);
      Bukkit.getPluginManager().registerEvents(new WorldListener(), this);
      Bukkit.getPluginManager().registerEvents(new HopperMinecartListener(), this);

   }

   @Override
   public void onDisable( ) {
      if (Configuration.protectDoors() || Configuration.protectTrapdoors()) {
         Bukkit.getLogger().info("[" + getDescription().getName() + "] Closing all automatic doors.");
         door_closer.cleanup();
      }
   }

   public DoorCloser getDoorCloser( ) {
      return door_closer;
   }
   
   public boolean usingExternalPermissions(){
      if(!Configuration.enablePermissions()) return(false);
      return(super.usingExternalPermissions());
      //return(usePermissions);
   }
   
   public boolean usingExternalZones(){
      return(super.usingExternalZones());
   }
}
