package com.ejfirestar.lockette.config;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.ejfirestar.lockette.Lockette;

public class L10N {

   private static YamlConfiguration config = null;
   private static Lockette lockette;

   private static String altPrivate;
   private static String altMoreUsers;
   private static String altEveryone;
   private static String altOperators;
   private static String altTimer;
   private static String altFee;

   public static void loadConfig( Lockette plugin, String file_name ) {
      lockette = plugin;
      loadStrings(file_name);
   }

   public static void loadStrings( String fileName ) {

      boolean stringChanged = false;
      String tempString;
      File stringsFile = new File(lockette.getDataFolder(), fileName);

      // Close the strings file if already loaded.
      if (config != null) {
         // Should automatically garbage collect.
         config = null;
      }

      // Load the strings file.
      config = new YamlConfiguration();
      try {
         config.load(stringsFile);
      }
      catch (InvalidConfigurationException ex) {
         Bukkit.getLogger().warning("[Lockette] Error loading " + fileName + ": " + ex.getMessage());

         if (!fileName.equals("strings-en.yml")) {
            loadStrings("strings-en.yml");
            return;
         }
         else
            Bukkit.getLogger().warning("[Lockette] Returning to default strings.");
      }
      catch (Exception ex) {}

      // To remove French tags from the default strings file, and to not save to alternate strings files.
      boolean original = false;
      if (fileName.equals("strings-en.yml")) {
         original = true;

         config.set("language", "English");

         // Force to be first.
         if (original) {
            try {
               config.save(stringsFile);
               config.load(stringsFile);
            }
            catch (Exception ex) {}
         }

         config.set("author", "Acru");
         config.set("editors", "");
         config.set("version", 0);
      }

      // Report language.

      tempString = config.getString("language");
      if ((tempString == null) || tempString.isEmpty()) {
         Bukkit.getLogger().info("[Lockette] Loading strings file " + fileName);
      }
      else
         Bukkit.getLogger().info("[Lockette] Loading strings file for " + tempString + " by " + config.getString("author"));

      // Load in the alternate sign strings.

      altPrivate = config.getString("alternate-private-tag");
      if ((altPrivate == null) || altPrivate.isEmpty() || (original && altPrivate.equals("Priv�"))) {
         altPrivate = "Private";
         config.set("alternate-private-tag", altPrivate);
      }
      altPrivate = "[" + altPrivate + "]";

      altMoreUsers = config.getString("alternate-moreusers-tag");
      if ((altMoreUsers == null) || altMoreUsers.isEmpty() || (original && altMoreUsers.equals("Autre Noms"))) {
         altMoreUsers = "More Users";
         config.set("alternate-moreusers-tag", altMoreUsers);
         stringChanged = true;
      }
      altMoreUsers = "[" + altMoreUsers + "]";

      altEveryone = config.getString("alternate-everyone-tag");
      if ((altEveryone == null) || altEveryone.isEmpty() || (original && altEveryone.equals("Tout le Monde"))) {
         altEveryone = "Everyone";
         config.set("alternate-everyone-tag", altEveryone);
         stringChanged = true;
      }
      altEveryone = "[" + altEveryone + "]";

      altOperators = config.getString("alternate-operators-tag");
      if ((altOperators == null) || altOperators.isEmpty() || (original && altOperators.equals("Op�rateurs"))) {
         altOperators = "Operators";
         config.set("alternate-operators-tag", altOperators);
         stringChanged = true;
      }
      altOperators = "[" + altOperators + "]";

      altTimer = config.getString("alternate-timer-tag");
      if ((altTimer == null) || altTimer.isEmpty() || (original && altTimer.equals("Minuterie"))) {
         altTimer = "Timer";
         config.set("alternate-timer-tag", altTimer);
         stringChanged = true;
      }

      altFee = config.getString("alternate-fee-tag");
      if ((altFee == null) || altFee.isEmpty()) {
         altFee = "Fee";
         config.set("alternate-fee-tag", altFee);
         stringChanged = true;
      }

      // Check all the message strings.

      // Messages for onBlockPlace.
      tempString = config.getString("msg-user-conflict-door");
      if (tempString == null) {
         config.set("msg-user-conflict-door", "Conflicting door removed!");
         stringChanged = true;
      }
      tempString = config.getString("msg-user-illegal");
      if (tempString == null) {
         config.set("msg-user-illegal", "Illegal chest removed!");
         stringChanged = true;
      }
      tempString = config.getString("msg-user-resize-owned");
      if (tempString == null) {
         config.set("msg-user-resize-owned", "You cannot resize a chest claimed by ***.");
         stringChanged = true;
      }
      tempString = config.getString("msg-help-chest");
      if (tempString == null) {
         config.set("msg-help-chest", "Place a sign headed [Private] next to a chest to lock it.");
         stringChanged = true;
      }

      // Messages for onBlockBreak.
      tempString = config.getString("msg-owner-release");
      if (tempString == null) {
         config.set("msg-owner-release", "You have released a container!");
         stringChanged = true;
      }
      tempString = config.getString("msg-admin-release");
      if (tempString == null) {
         config.set("msg-admin-release", "(Admin) @@@ has broken open a container owned by ***!");
         stringChanged = true;
      }
      tempString = config.getString("msg-user-release-owned");
      if (tempString == null) {
         config.set("msg-user-release-owned", "You cannot release a container claimed by ***.");
         stringChanged = true;
      }
      tempString = config.getString("msg-owner-remove");
      if (tempString == null) {
         config.set("msg-owner-remove", "You have removed users from a container!");
         stringChanged = true;
      }
      tempString = config.getString("msg-user-remove-owned");
      if (tempString == null) {
         config.set("msg-user-remove-owned", "You cannot remove users from a container claimed by ***.");
         stringChanged = true;
      }
      tempString = config.getString("msg-user-break-owned");
      if (tempString == null) {
         config.set("msg-user-break-owned", "You cannot break a container claimed by ***.");
         stringChanged = true;
      }

      // Messages for onBlockDamage.
      tempString = config.getString("msg-user-denied-door");
      if (tempString == null) {
         config.set("msg-user-denied-door", "You don't have permission to use this door.");
         stringChanged = true;
      }

      // Messages for onBlockRightClick.
      tempString = config.getString("msg-user-touch-fee");
      if (tempString == null) {
         config.set("msg-user-touch-fee", "A fee of ### will be paid to ***, to open.");
         stringChanged = true;
      }
      tempString = config.getString("msg-user-touch-owned");
      if (tempString == null) {
         config.set("msg-user-touch-owned", "This container has been claimed by ***.");
         stringChanged = true;
      }
      tempString = config.getString("msg-help-select");
      if (tempString == null) {
         config.set("msg-help-select", "Sign selected, use /lockette <line number> <text> to edit.");
         stringChanged = true;
      }

      // Messages for onBlockInteract.
      tempString = config.getString("msg-admin-bypass");
      if (tempString == null) {
         config.set("msg-admin-bypass", "Bypassed a door owned by ***, be sure to close it behind you.");
         stringChanged = true;
      }
      tempString = config.getString("msg-admin-snoop");
      if (tempString == null) {
         config.set("msg-admin-snoop", "(Admin) @@@ has snooped around in a container owned by ***!");
         stringChanged = true;
      }
      tempString = config.getString("msg-user-denied");
      if (tempString == null) {
         config.set("msg-user-denied", "You don't have permission to open this container.");
         stringChanged = true;
      }

      // Messages for onSignChange.
      tempString = config.getString("msg-error-zone");
      if (tempString == null) {
         config.set("msg-error-zone", "This zone is protected by ***.");
         stringChanged = true;
      }
      tempString = config.getString("msg-error-permission");
      if (tempString == null) {
         config.set("msg-error-permission", "Permission to lock container denied.");
         stringChanged = true;
      }
      else if (tempString.equals("Permission to lock containers denied.")) {
         config.set("msg-error-permission", "Permission to lock container denied.");
         stringChanged = true;
      }
      tempString = config.getString("msg-error-claim");
      if (tempString == null) {
         config.set("msg-error-claim", "No unclaimed container nearby to make Private!");
         stringChanged = true;
      }
      tempString = config.getString("msg-error-claim-conflict");
      if (tempString == null) {
         config.set("msg-error-claim-conflict", "Conflict with an existing protected door.");
         stringChanged = true;
      }
      tempString = config.getString("msg-admin-claim-error");
      if (tempString == null) {
         config.set("msg-admin-claim-error", "Player *** is not online, be sure you have the correct name.");
         stringChanged = true;
      }
      tempString = config.getString("msg-admin-claim");
      if (tempString == null) {
         config.set("msg-admin-claim", "You have claimed a container for ***.");
         stringChanged = true;
      }
      tempString = config.getString("msg-owner-claim");
      if (tempString == null) {
         config.set("msg-owner-claim", "You have claimed a container!");
         stringChanged = true;
      }
      tempString = config.getString("msg-error-adduser-owned");
      if (tempString == null) {
         config.set("msg-error-adduser-owned", "You cannot add users to a container claimed by ***.");
         stringChanged = true;
      }
      tempString = config.getString("msg-error-adduser");
      if (tempString == null) {
         config.set("msg-error-adduser", "No claimed container nearby to add users to!");
         stringChanged = true;
      }
      tempString = config.getString("msg-owner-adduser");
      if (tempString == null) {
         config.set("msg-owner-adduser", "You have added users to a container!");
         stringChanged = true;
      }

      // Messages for onPlayerCommand.
      if (original) {
         config.set("msg-help-command1", "&C/lockette <line number> <text> - Edits signs on locked containers. Right click on the sign to edit.");
         config.set("msg-help-command2", "&C/lockette fix - Fixes an automatic door that is in the wrong position. Look at the door to edit.");
         config.set("msg-help-command3", "&C/lockette reload - Reloads the configuration files. Operators only.");
         config.set("msg-help-command4", "&C/lockette version - Reports Lockette version.");
         stringChanged = true;
      }

      tempString = config.getString("msg-admin-reload");
      if (tempString == null) {
         config.set("msg-admin-reload", "Reloading plugin configuration files.");
         stringChanged = true;
      }
      tempString = config.getString("msg-error-fix");
      if (tempString == null) {
         config.set("msg-error-fix", "No owned door found.");
         stringChanged = true;
      }
      tempString = config.getString("msg-error-edit");
      if (tempString == null) {
         config.set("msg-error-edit", "First select a sign by right clicking it.");
         stringChanged = true;
      }
      tempString = config.getString("msg-owner-edit");
      if (tempString == null) {
         config.set("msg-owner-edit", "Sign edited successfully.");
         stringChanged = true;
      }

      /*
      
      tempString = strings.getString("");
      if(tempString == null){
         strings.set("", "");
         stringChanged = true;
      }
      
      */

      if (original)
         if (stringChanged) {
            try {
               config.save(stringsFile);
            }
            catch (Exception ex) {}
         }
   }

   public static void localizedMessage( Player player, String broadcast, String key ) {
      localizedMessage(player, broadcast, key, null, null);
   }

   public static void localizedMessage( Player player, String broadcast, String key, String sub ) {
      localizedMessage(player, broadcast, key, sub, null);
   }

   public static void localizedMessage( Player player, String broadcast, String key, String sub, String num ) {

      String color = "";
      FileConfiguration properties = lockette.getConfig();

      if (key.startsWith("msg-user-")) {
         if (broadcast == null)
            if (!properties.getBoolean("enable-messages-user", true))
               return;
         color = ChatColor.YELLOW.toString();
      }
      else if (key.startsWith("msg-owner-")) {
         if (broadcast == null)
            if (!properties.getBoolean("enable-messages-owner", false))
               return;
         color = ChatColor.GOLD.toString();
      }
      else if (key.startsWith("msg-admin-")) {
         if (broadcast == null)
            if (!properties.getBoolean("enable-messages-admin", false))
               return;
         color = ChatColor.RED.toString();
      }
      else if (key.startsWith("msg-error-")) {
         if (broadcast == null)
            if (!properties.getBoolean("enable-messages-error", false))
               return;
         color = ChatColor.RED.toString();
      }
      else if (key.startsWith("msg-help-")) {
         if (broadcast == null)
            if (!properties.getBoolean("enable-messages-help", false))
               return;
         color = ChatColor.GOLD.toString();
      }

      // Fetch the requested message string.
      String message = config.getString(key);
      if ((message == null) || message.isEmpty())
         return;

      // Do place holder substitution.
      message = message.replaceAll("&([0-9A-Fa-f])", "\u00A7$1");
      if (sub != null)
         message = message.replaceAll("\\*\\*\\*", sub + color);
      if (num != null)
         message = message.replaceAll("###", num);
      if (player != null)
         message = message.replaceAll("@@@", player.getName());

      // Send out the formatted message.
      if (broadcast != null) {
         message = color + "[Lockette] " + message;
         if (broadcast == null || broadcast.isEmpty() || message == null || message.isEmpty()) {
            return;
         }

         Player[] players = Bukkit.getServer().getOnlinePlayers();

         if (broadcast.charAt(0) == '[') {
            for (int x = 0; x < players.length; x++) {
               if (lockette.inGroup(players[x].getWorld(), players[x], broadcast)) {
                  players[x].sendMessage(message);
               }
            }
         }
         else {
            for (int x = 0; x < players.length; x++)
               if (broadcast.equalsIgnoreCase(players[x].getName())) {
                  players[x].sendMessage(message);
               }
         }
      }
      else if (player != null)
         player.sendMessage(color + "[Lockette] " + message);
   }

   public static String getAltPrivate( ) {
      return altPrivate;
   }

   public static String getAltMoreUsers( ) {
      return altMoreUsers;
   }

   public static String getAltEveryone( ) {
      return altEveryone;
   }

   public static String getAltOperators( ) {
      return altOperators;
   }

   public static String getAltTimer( ) {
      return altTimer;
   }

   public static String getAltFee( ) {
      return altFee;
   }
}