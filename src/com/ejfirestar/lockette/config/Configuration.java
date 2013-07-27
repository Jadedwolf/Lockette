package com.ejfirestar.lockette.config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.ejfirestar.lockette.Lockette;

/**
 * Handles options that are configured via config.yml
 */
public class Configuration //TODO Non-static. Load upon construction.
{
   private static File config_file;
   private static File data_folder;

   private static boolean message_user;
   private static boolean message_owner;
   private static boolean message_admin;
   private static boolean message_error;
   private static boolean message_help;
   private static boolean explosion_protection;
   private static boolean chest_rotation;
   private static boolean enable_permissions;
   private static boolean protection_doors;
   private static boolean protection_trapdoors;
   private static boolean admin_snoop;
   private static boolean admin_bypass;
   private static boolean admin_break;
   private static boolean quick_protect;
   private static boolean color_tags;
   private static boolean debug_mode;

   private static int door_timer;

   private static List<Object> custom_blocks = null;
   private static List<Object> disabled_plugins = null;

   private static String broadcast_snoop;
   private static String broadcast_break;
   private static String broadcast_reload;
   private static String file_name;

   public static void loadConfig( Lockette plugin ) {
      loadConfigFile(plugin.getDataFolder());
      L10N.loadConfig(plugin, file_name);
   }

   /**
    * @param The location the plugin configuration folder is located.
    *           Loads the config file for the plugin.
    */
   @SuppressWarnings( "unchecked" )
   private static void loadConfigFile( File data_folder ) {
      Configuration.data_folder = data_folder;
      config_file = new File(data_folder, "config.yml");

      //write the config file, will return false if error
      if (writeConfig(config_file, "config.yml")) {
         try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(config_file);

            message_user = config.getBoolean("enable-messages-user", true);
            message_owner = config.getBoolean("enable-messages-owner", false);
            message_admin = config.getBoolean("enable-messages-admin", true);
            message_error = config.getBoolean("enable-messages-error", true);
            message_help = config.getBoolean("enable-messages-help", true);

            explosion_protection = config.getBoolean("explosion-protection-all", true);
            chest_rotation = config.getBoolean("enable-chest-rotation", false);

            enable_permissions = config.getBoolean("enable-permissions", false);
            protection_doors = config.getBoolean("enable-protection-doors", true);
            protection_trapdoors = config.getBoolean("enable-protection-trapdoors", true);

            admin_snoop = config.getBoolean("allow-admin-snoop", false);
            admin_bypass = config.getBoolean("allow-admin-bypass", true);
            admin_break = config.getBoolean("allow-admin-break", true);

            quick_protect = config.getBoolean("enable-quick-protect", true);
            color_tags = config.getBoolean("enable-color-tags", true);
            debug_mode = config.getBoolean("enable-debug", false);

            door_timer = config.getInt("default-door-timer", 0);

            custom_blocks = ( List<Object> ) config.getList("custom-lockable-block-list");
            if (!custom_blocks.isEmpty()) {
               Bukkit.getLogger().info("[Lockette] Custom lockable block list: " + custom_blocks.toString());
            }

            disabled_plugins = ( List<Object> ) config.getList("linked-plugin-ignore-list");
            if (!disabled_plugins.isEmpty()) {
               Bukkit.getLogger().info("[Lockette] Ignoring linked plugins: " + disabled_plugins.toString());
            }

            broadcast_snoop = config.getString("broadcast-snoop-target");
            broadcast_break = config.getString("broadcast-break-target");
            broadcast_reload = config.getString("broadcast-reload-target");

            file_name = config.getString("strings-file-name");
         }
         catch (Exception ex) {
            //TODO:Utils.error(Lockette.NAME, ex);
            //Utils.warning(Lockette.NAME, "Configuration file failed to load!");
         }
      }
      else {
         //TODO:Utils.warning(Lockette.NAME, "Using default values.");
      }
   }

   /**
    * Writes the config file to specified location for easier access.
    * 
    * @param read_file
    *           the input file.
    * @param write_file
    *           the file to write based on the input file.
    * @return true if the file was created successfully, or already exists.
    *         false if the file was not created successfully.
    */
   private static Boolean writeConfig( File output_file, String input_file ) {
      if (output_file.exists()) {
         return true;
      }

      InputStream in = null;
      OutputStream out = null;

      try {
         data_folder.mkdirs();
         output_file.delete();
         output_file.createNewFile();

         in = Lockette.class.getResourceAsStream('/' + input_file);
         out = new BufferedOutputStream(new FileOutputStream(output_file));

         byte[] buffer = new byte[1024];
         int num_read = 0;
         while ((num_read = in.read(buffer)) != -1) {
            out.write(buffer, 0, num_read);
         }
         //TODO:Utils.message(Lockette.NAME, "Configuration was created successfully");
         return true;
      }
      catch (IOException ex) {
         //TODO:Utils.warning(Lockette.NAME, "Failed to create configuration.");
         //Utils.warning(Lockette.NAME, "Do you have permission to write to this location?");
         //Utils.error(Lockette.NAME, ex);
         return false;
      }
      finally {
         try {
            if (in != null)
               in.close();

            if (out != null)
               out.close();
         }
         catch (IOException ioe) {
            //TODO:Utils.error(Lockette.NAME, ioe);
            return false;
         }
      }
   }

   public static boolean MessageUser( ) {
      return message_user;
   }

   public static boolean MessageOwner( ) {
      return message_owner;
   }

   public static boolean MessageAdmin( ) {
      return message_admin;
   }

   public static boolean MessageError( ) {
      return message_error;
   }

   public static boolean MessageHelp( ) {
      return message_help;
   }

   public static boolean useExplosionProtection( ) {
      return explosion_protection;
   }

   public static boolean allowChestRotation( ) {
      return chest_rotation;
   }

   public static boolean enablePermissions( ) {
      return enable_permissions;
   }

   public static boolean protectDoors( ) {
      return protection_doors;
   }

   public static boolean protectTrapdoors( ) {
      return protection_trapdoors;
   }

   public static boolean allowAdminSnoop( ) {
      return admin_snoop;
   }

   public static boolean allowAdminBypass( ) {
      return admin_bypass;
   }

   public static boolean allowAdminBreak( ) {
      return admin_break;
   }

   public static boolean useDebugMode( ) {
      return debug_mode;
   }

   public static boolean useColorTags( ) {
      return color_tags;
   }

   public static boolean useQuickProtect( ) {
      return quick_protect;
   }

   public static int getDoorTimer( ) {
      return door_timer;
   }

   public static List<Object> getCustomBlockList( ) {
      return custom_blocks;
   }

   public static List<Object> getDisabledPluginsList( ) {
      return disabled_plugins;
   }

   public static String broadcastSnoop( ) {
      return broadcast_snoop;
   }

   public static String broadcastBreak( ) {
      return broadcast_break;
   }

   public static String broadcastReload( ) {
      return broadcast_reload;
   }
}