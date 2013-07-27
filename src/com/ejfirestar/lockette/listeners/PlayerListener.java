//
// This file is a component of Lockette for Bukkit, and was written by Acru Jovian.
// Distributed under the The Non-Profit Open Software License version 3.0 (NPOSL-3.0)
// http://www.opensource.org/licenses/NOSL3.0
//

package com.ejfirestar.lockette.listeners;

// Imports.
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.ejfirestar.lockette.Lockette;
import com.ejfirestar.lockette.config.Configuration;
import com.ejfirestar.lockette.config.L10N;
import com.ejfirestar.lockette.utils.Utils;

public class PlayerListener implements Listener {

   private static Lockette lockette;
   private final static HashMap<String, Block> playerList = new HashMap<String, Block>();

   public PlayerListener( Lockette plugin ) {
      lockette = plugin;
   }

   @EventHandler( priority = EventPriority.NORMAL, ignoreCancelled = true )
   public void onPlayerCommandPreprocess( PlayerCommandPreprocessEvent event ) {
      String[] command = event.getMessage().split(" ", 3);

      if (command.length < 1)
         return;
      if (!(command[0].equalsIgnoreCase("/lockette") || command[0].equalsIgnoreCase("/lock")))
         return;
      event.setCancelled(true);

      Player player = event.getPlayer();

      // Reload config files, for admins only.
      if (command.length == 2) {
         if (command[1].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("lockette.admin.reload"))
               return;

            Configuration.loadConfig(lockette);

            L10N.localizedMessage( player, Configuration.broadcastReload(), "msg-admin-reload");
            return;
         }

         if (command[1].equalsIgnoreCase("version")) {
            player.sendMessage(ChatColor.RED + "Lockette version " + lockette.getDescription().getVersion() + " loaded");
            return;
         }

         if (command[1].equalsIgnoreCase("fix")) {
            if (fixDoor(player)) {
               L10N.localizedMessage( player, null, "msg-error-fix");
            }
            return;
         }
      }

      // Edit sign text.
      if ((command.length == 2) || (command.length == 3)) {
         if (command[1].equals("1") || command[1].equals("2") || command[1].equals("3") || command[1].equals("4")) {
            Block block = playerList.get(player.getName());
            //boolean		error = false;

            // Check if the selected block is a valid sign.

            if (block == null) {
               L10N.localizedMessage( player, null, "msg-error-edit");
               return;
            }
            else if (block.getTypeId() != Material.WALL_SIGN.getId()) {
               L10N.localizedMessage( player, null, "msg-error-edit");
               return;
            }

            Sign sign = ( Sign ) block.getState();
            Sign owner = sign;
            String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();
            boolean privateSign;

            // Check if it is our sign that is selected.

            if (text.equals("[private]") || text.equalsIgnoreCase(L10N.getAltPrivate()))
               privateSign = true;
            else if (text.equals("[more users]") || text.equalsIgnoreCase(L10N.getAltMoreUsers())) {
               privateSign = false;

               Block checkBlock = Utils.getSignAttachedBlock(block);
               if (checkBlock == null) {
                  L10N.localizedMessage( player, null, "msg-error-edit");
                  return;
               }

               Block signBlock = Utils.findBlockOwner(checkBlock, null, false);
               if (signBlock == null) {
                  L10N.localizedMessage( player, null, "msg-error-edit");
                  return;
               }

               owner = ( Sign ) signBlock.getState();
            }
            else {
               L10N.localizedMessage( player, null, "msg-error-edit");
               return;
            }

            int length = player.getName().length();

            if (length > 15)
               length = 15;

            // Check owner.
            if (owner.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length)) || Configuration.useDebugMode()) {
               int line = Integer.parseInt(command[1]) - 1;

               // Disallow editing [Private] line 1/2 here.
               if (!Configuration.useDebugMode()) {
                  if (line <= 0)
                     return;
                  else if (line <= 1)
                     if (privateSign)
                        return;
               }

               if (command.length == 3) {
                  length = command[2].length();

                  if (length > 15)
                     length = 15;
                  if (Configuration.useColorTags())
                     sign.setLine(line, command[2].substring(0, length).replaceAll("&([0-9A-Fa-f])", "\u00A7$1"));
                  else
                     sign.setLine(line, command[2].substring(0, length));
               }
               else
                  sign.setLine(line, "");
               sign.update();

               L10N.localizedMessage( player, null, "msg-owner-edit");
               return;
            }
            else {
               L10N.localizedMessage( player, null, "msg-error-edit");
               return;
            }
         }
      }

      // If none of the above, print out the help text.
      // Commands:
      // reload
      // 2-4 <text> - sign editing
      // link - linking?
      // set <value> <string> - config?

      L10N.localizedMessage( player, null, "msg-help-command1");
      L10N.localizedMessage( player, null, "msg-help-command2");
      L10N.localizedMessage( player, null, "msg-help-command3");
      L10N.localizedMessage( player, null, "msg-help-command4");
      L10N.localizedMessage( player, null, "msg-help-command5");
      L10N.localizedMessage( player, null, "msg-help-command6");
      L10N.localizedMessage( player, null, "msg-help-command7");
      L10N.localizedMessage( player, null, "msg-help-command8");
      L10N.localizedMessage( player, null, "msg-help-command9");
   }

   @EventHandler( priority = EventPriority.LOW, ignoreCancelled = true )
   public void onPlayerInteract( PlayerInteractEvent event ) {
      if (!event.hasBlock())
         return;

      Action action = event.getAction();
      Player player = event.getPlayer();
      Block block = event.getClickedBlock();
      int type = block.getTypeId();
      BlockFace face = event.getBlockFace();
      ItemStack item;

      if (action == Action.RIGHT_CLICK_BLOCK) {

         // Check we are allowed to used this trapdoor
         if (Configuration.protectTrapdoors() && type == Material.TRAP_DOOR.getId()) {

            if (interactDoor(block, player))
               return;

            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
            return;
         }

         // Check we are allowed to used this door
         if (Configuration.protectDoors()
               && ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId()))) {
            if (interactDoor(block, player))
               return;

            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
            return;
         }

         if (type == Material.WALL_SIGN.getId()) {
            interactSign(block, player);
            return;
         }

         if ((type == Material.CHEST.getId()) || (type == Material.TRAPPED_CHEST.getId())) {
            // Try at making a 1.7->1.8 chest fixer.
            Utils.rotateChestOrientation(block, face);
         }

         if ((type == Material.CHEST.getId()) || (type == Material.TRAPPED_CHEST.getId()) || (type == Material.DISPENSER.getId())
               || (type == Material.DROPPER.getId()) || (type == Material.FURNACE.getId()) || (type == Material.BURNING_FURNACE.getId())
               || (type == Material.BREWING_STAND.getId()) || Utils.isInList(type, Configuration.getCustomBlockList())) {

            // Trying something out....
            if (Configuration.useQuickProtect()) {
               if (event.hasItem()) {
                  if ((face != BlockFace.UP) && (face != BlockFace.DOWN)) {
                     item = event.getItem();

                     if (item.getTypeId() == Material.SIGN.getId()) {
                        Block checkBlock = block.getRelative(face);

                        type = checkBlock.getTypeId();

                        if (type == Material.AIR.getId()) {
                           boolean place = false;

                           if (Utils.isProtected(block)) {
                              // Add a users sign only if owner.
                              if (Utils.isOwner(block, player.getName()))
                                 place = true;
                           }
                           else
                              place = true;
                           //if(Lockette.altPrivate == null){}//if(Lockette.altMoreUsers == null){}
                           if (place) {
                              //player.sendMessage(ChatColor.RED + "Lockette: Using a sign on a container");

                              event.setUseItemInHand(Result.ALLOW); //? seems to work in 568
                              event.setUseInteractedBlock(Result.DENY);
                              return;
                           }
                        }
                     }
                  }
               }
            }
            if (interactContainer(block, player))
               return;

            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
            return;
         }

         if (type == Material.DIRT.getId())
            if (event.hasItem()) {
               item = event.getItem();

               type = item.getTypeId();

               if ((type == Material.DIAMOND_HOE.getId()) || (type == Material.GOLD_HOE.getId()) || (type == Material.IRON_HOE.getId())
                     || (type == Material.STONE_HOE.getId()) || (type == Material.WOOD_HOE.getId())) {
                  Block checkBlock = block.getRelative(BlockFace.UP);

                  type = checkBlock.getTypeId();

                  if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId())) {
                     event.setUseInteractedBlock(Result.DENY);
                     return;
                  }

                  if (hasAttachedTrapDoor(block)) {
                     event.setUseInteractedBlock(Result.DENY);
                     return;
                  }
               }
            }

      }
      else if (action == Action.LEFT_CLICK_BLOCK) {
         if (Configuration.protectTrapdoors())
            if (type == Material.TRAP_DOOR.getId()) {
               if (interactDoor(block, player))
                  return;

               event.setUseInteractedBlock(Result.DENY);
               event.setUseItemInHand(Result.DENY);
               return;
            }

         if (Configuration.protectDoors())
            if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId())) {
               if (interactDoor(block, player))
                  return;

               event.setUseInteractedBlock(Result.DENY);
               event.setUseItemInHand(Result.DENY);
               return;
            }
      }
   }

   @EventHandler( priority = EventPriority.NORMAL )
   public void onPlayerQuit( PlayerQuitEvent event ) {
      Player player = event.getPlayer();

      // Player left, so forget about them.
      playerList.remove(player.getName());
   }

   // Returns true if it should be allowed, false if it should be canceled.
   private static boolean interactDoor( Block block, Player player ) {
      Block signBlock = Utils.findBlockOwner(block, null, false);

      if (signBlock == null)
         return (true);

      boolean wooden = ((block.getTypeId() == Material.WOODEN_DOOR.getId()) || (block.getTypeId() == Material.FENCE_GATE.getId()));
      boolean trap = false;

      if (Configuration.protectTrapdoors())
         if (block.getTypeId() == Material.TRAP_DOOR.getId()) {
            wooden = true;
            trap = true;
         }

      // Someone touched an owned door, lets see if they are allowed.

      boolean allow = false;

      if (canInteract(block, signBlock, player, true))
         allow = true;

      if (allow) {
         List<Block> list = Utils.toggleDoors(block, Utils.getSignAttachedBlock(signBlock), wooden, trap);

         int delta = Utils.getSignOption(signBlock, "timer", L10N.getAltTimer(), Configuration.getDoorTimer());

         lockette.getDoorCloser().add(list, delta != 0, delta);
         return (true);
      }

      // Report only once, unless a different block is clicked.
      if (block.equals(playerList.get(player.getName())))
         return (false);
      playerList.put(player.getName(), block);
      L10N.localizedMessage( player, null, "msg-user-denied-door");
      return (false);
   }

   private static void interactSign( Block block, Player player ) {
      Sign sign = ( Sign ) block.getState();
      String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();
      Block signBlock = block;

      // Check if it is our sign that was clicked.

      if (text.equals("[private]") || text.equalsIgnoreCase(L10N.getAltPrivate())) {}
      else if (text.equals("[more users]") || text.equalsIgnoreCase(L10N.getAltMoreUsers())) {
         Block checkBlock = Utils.getSignAttachedBlock(block);
         if (checkBlock == null)
            return;

         signBlock = Utils.findBlockOwner(checkBlock, null, false);
         if (signBlock == null)
            return;

         sign = ( Sign ) signBlock.getState();
      }
      else
         return;

      int length = player.getName().length();

      if (length > 15)
         length = 15;

      // Check owner.
      if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length)) || Configuration.useDebugMode()) {
         if (!block.equals(playerList.get(player.getName()))) {
            // Associate the user with the owned sign.
            playerList.put(player.getName(), block);
            L10N.localizedMessage( player, null, "msg-help-select");
         }
      }
      else if (!block.equals(playerList.get(player.getName()))) {
         // Only print this message once as well.
         playerList.put(player.getName(), block);
         L10N.localizedMessage( player, null, "msg-user-touch-owned", sign.getLine(1));
      }
   }

   // Returns true if it should be allowed, false if it should be canceled.
   private static boolean interactContainer( Block block, Player player ) {
      Block signBlock = Utils.findBlockOwner(block, null, false);

      if (signBlock == null)
         return (true);

      // Someone touched an owned container, lets see if they are allowed.

      if (canInteract(block, signBlock, player, false))
         return (true);

      // Don't have permission.

      // Report only once, unless a different block is clicked.
      if (block.equals(playerList.get(player.getName())))
         return (false);
      playerList.put(player.getName(), block);
      L10N.localizedMessage( player, null, "msg-user-denied");
      return (false);
   }

   // Block is the container or door, signBlock is the owning [Private] sign.
   // Returns true if it should be allowed, false if it should be canceled.
   private static boolean canInteract( Block block, Block signBlock, Player player, boolean isDoor ) {
      // Check if the block is owned first.

      // Moved to outer..

      // Lets see if the player is allowed to touch...

      Sign sign = ( Sign ) signBlock.getState();
      int length = player.getName().length();
      String line;

      if (length > 15)
         length = 15;

      // Check owner.

      line = sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "");

      if (line.equals(player.getName().substring(0, length)))
         return (true);
      if (lockette.inGroup(block.getWorld(), player, line))
         return (true);

      // Check main two users.

      int y;

      for (y = 2; y <= 3; ++y)
         if (!sign.getLine(y).isEmpty()) {
            line = sign.getLine(y).replaceAll("(?i)\u00A7[0-F]", "");

            if (lockette.inGroup(block.getWorld(), player, line))
               return (true);
            if (line.equalsIgnoreCase(player.getName().substring(0, length)))
               return (true);
         }

      // Check for more users.

      List<Block> list = Utils.findBlockUsers(block, signBlock);
      int x, count = list.size();
      Sign sign2;

      for (x = 0; x < count; ++x) {
         sign2 = ( Sign ) list.get(x).getState();

         for (y = 1; y <= 3; ++y)
            if (!sign2.getLine(y).isEmpty()) {
               line = sign2.getLine(y).replaceAll("(?i)\u00A7[0-F]", "");

               if (lockette.inGroup(block.getWorld(), player, line))
                  return (true);
               if (line.equalsIgnoreCase(player.getName().substring(0, length)))
                  return (true);
            }
      }

      // Check admin list last.

      boolean snoop = false;

      if (isDoor) {
         if (Configuration.allowAdminBypass()) {
            if (player.hasPermission("lockette.admin.bypass"))
               snoop = true;

            if (snoop) {
               Bukkit.getLogger()
                     .info("[" + lockette.getDescription().getName() + "] (Admin) " + player.getName() + " has bypassed a door owned by "
                           + sign.getLine(1));

               L10N.localizedMessage( player, null, "msg-admin-bypass", sign.getLine(1));
               return (true);
            }
         }
      }
      else if (Configuration.allowAdminSnoop()) {
         if (player.hasPermission("lockette.admin.snoop"))
            snoop = true;

         if (snoop) {
            Bukkit.getLogger().info(
                  "[" + lockette.getDescription().getName() + "] (Admin) " + player.getName() + " has snooped around in a container owned by "
                        + sign.getLine(1) + "!");

            L10N.localizedMessage( player, Configuration.broadcastSnoop(), "msg-admin-snoop", sign.getLine(1));
            return (true);
         }
      }

      // Don't have permission.

      return (false);
   }

   // Returns true if a door wasn't changed.
   private static boolean fixDoor( Player player ) {
      Block block = player.getTargetBlock(null, 10);
      int type = block.getTypeId();
      boolean doCheck = false;

      // Check if the block being looked at is a door block.

      if (Configuration.protectTrapdoors()) {
         if (type == Material.TRAP_DOOR.getId())
            doCheck = true;
      }

      if (Configuration.protectDoors()) {
         if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId()))
            doCheck = true;
      }

      if (!doCheck)
         return (true);

      Block signBlock = Utils.findBlockOwner(block, null, false);

      if (signBlock == null)
         return (true);

      Sign sign = ( Sign ) signBlock.getState();
      int length = player.getName().length();

      if (length > 15)
         length = 15;

      // Check owner only.
      if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {

         if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId())) {
            Utils.toggleDoors(block, null, true, false);
         }
         else if ((type == Material.TRAP_DOOR.getId()) || (type == Material.FENCE_GATE.getId())) {
            Utils.toggleDoors(block, null, false, false);
         }
         return (false);
      }

      return (true);
   }

   public static boolean hasAttachedTrapDoor( Block block ) {
      Block checkBlock;
      int type;
      int face;

      checkBlock = block.getRelative(BlockFace.NORTH);
      type = checkBlock.getTypeId();
      if (type == Material.TRAP_DOOR.getId()) {
         face = checkBlock.getData() & 0x3;
         if (face == 2)
            return (true);
      }

      checkBlock = block.getRelative(BlockFace.EAST);
      type = checkBlock.getTypeId();
      if (type == Material.TRAP_DOOR.getId()) {
         face = checkBlock.getData() & 0x3;
         if (face == 0)
            return (true);
      }

      checkBlock = block.getRelative(BlockFace.SOUTH);
      type = checkBlock.getTypeId();
      if (type == Material.TRAP_DOOR.getId()) {
         face = checkBlock.getData() & 0x3;
         if (face == 3)
            return (true);
      }

      checkBlock = block.getRelative(BlockFace.WEST);
      type = checkBlock.getTypeId();
      if (type == Material.TRAP_DOOR.getId()) {
         face = checkBlock.getData() & 0x3;
         if (face == 1)
            return (true);
      }

      return (false);
   }
}
