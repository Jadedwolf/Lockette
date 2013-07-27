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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.yi.acru.bukkit.PluginCore;

import com.ejfirestar.lockette.Lockette;
import com.ejfirestar.lockette.config.Configuration;
import com.ejfirestar.lockette.config.L10N;
import com.ejfirestar.lockette.utils.Utils;

public class BlockListener implements Listener {
   
   private final HashMap<String, Block> playerList = new HashMap<String, Block>();
   private static Lockette lockette;

   // Facings are reversed as we are attaching signs to blocks.
   static byte faceList[] = { 5, 3, 4, 2 }; // SOUTH, WEST, NORTH, EAST
   static {
      if (BlockFace.NORTH.getModX() != -1) {
         // Post CraftBukkit 2502
         faceList[0] = 3; // SOUTH
         faceList[1] = 4; // WEST
         faceList[2] = 2; // NORTH
         faceList[3] = 5; // EAST
      }
   }

   final int materialList[] = { Material.CHEST.getId(), Material.TRAPPED_CHEST.getId(), Material.DISPENSER.getId(), Material.DROPPER.getId(),
         Material.FURNACE.getId(), Material.BURNING_FURNACE.getId(), Material.BREWING_STAND.getId(), Material.TRAP_DOOR.getId(),
         Material.WOODEN_DOOR.getId(), Material.IRON_DOOR_BLOCK.getId(), Material.FENCE_GATE.getId() };
   final int materialListFurnaces[] = { Material.FURNACE.getId(), Material.BURNING_FURNACE.getId() };
   final int materialListDoors[] = { Material.WOODEN_DOOR.getId(), Material.IRON_DOOR_BLOCK.getId(), Material.FENCE_GATE.getId() };
   final int materialListBad[] = { 50, 63, 64, 65, 68, 71, 75, 76, 96 };//,12,13,18,46// sand, gravel, leaves, tnt

   public BlockListener( Lockette plugin ) {
      lockette = plugin;
   }

   @EventHandler( priority = EventPriority.LOW, ignoreCancelled = true )
   public void onBlockBreak( BlockBreakEvent event ) {

      Player player = event.getPlayer();
      Block block = event.getBlock();
      int type = block.getTypeId();

      if (event.isCancelled())
         if (type != Material.WOODEN_DOOR.getId())
            return;

      // Someone is breaking a block, lets see if they are allowed.

      if (type == Material.WALL_SIGN.getId()) {
         if (block.getData() == 0) {
            // Fix for mcMMO error.
            block.setData(( byte ) 5);
         }

         Sign sign = ( Sign ) block.getState();
         String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

         if (text.equals("[private]") || text.equalsIgnoreCase(L10N.getAltPrivate())) {
            int length = player.getName().length();

            if (length > 15)
               length = 15;

            // Check owner.
            if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
               //Block		checkBlock = getSignAttachedBlock(block);
               //if(checkBlock == null) checkBlock = block;

               //if((checkBlock.getTypeId() != Material.WOODEN_DOOR.getId()) && (checkBlock.getTypeId() != Material.IRON_DOOR_BLOCK.getId())){
               Bukkit.getLogger().info("[" + lockette.getDescription().getName() + "] " + player.getName() + " has released a container.");
               //}
               //else lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " has released a door.");

               L10N.localizedMessage(player, null, "msg-owner-release");
               return;
            }

            // At this point, check admin.

            if (Configuration.allowAdminBreak()) {
               boolean snoop = false;

               if (player.hasPermission("lockette.admin.break"))
                  snoop = true;

               if (snoop) {
                  Bukkit.getLogger().info(
                        "[" + lockette.getDescription().getName() + "] (Admin) " + player.getName() + " has broken open a container owned by "
                              + sign.getLine(1) + "!");

                  L10N.localizedMessage(player, Configuration.broadcastBreak(), "msg-admin-release", sign.getLine(1));
                  return;
               }
            }

            event.setCancelled(true);
            sign.update();

            L10N.localizedMessage(player, null, "msg-user-release-owned", sign.getLine(1));
         }
         else if (text.equals("[more users]") || text.equalsIgnoreCase(L10N.getAltMoreUsers())) {
            Block checkBlock = Utils.getSignAttachedBlock(block);
            if (checkBlock == null)
               return;

            Block signBlock = Utils.findBlockOwner(checkBlock, null, false);
            if (signBlock == null)
               return;

            Sign sign2 = ( Sign ) signBlock.getState();
            int length = player.getName().length();

            if (length > 15)
               length = 15;

            if (sign2.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
               L10N.localizedMessage(player, null, "msg-owner-remove");
               return;
            }

            event.setCancelled(true);
            sign.update();

            L10N.localizedMessage(player, null, "msg-user-remove-owned", sign2.getLine(1));
         }
      }
      else {
         Block signBlock = Utils.findBlockOwner(block, null, false);

         if (signBlock == null)
            return;

         Sign sign = ( Sign ) signBlock.getState();
         int length = player.getName().length();

         if (length > 15)
            length = 15;

         // Check owner.
         if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
            if (Utils.findBlockOwnerBreak(block) != null) {
               // This block has the sign attached.  (Or the the door above the block.)

               Bukkit.getLogger().info("[" + lockette.getDescription().getName() + "] " + player.getName() + " has released a container.");
            }
            else {
               // Partial release for chest/doors, the sign may now be invalid for doors, but is always valid for chests.

               if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId())) {
                  // Check for invalid signs somehow?
                  // But valid signs can be collided anyways... so probably doesn't matter.  (Unless this is prevented too.)
               }
            }
            return;
         }

         event.setCancelled(true);
         //if(!enhancedEvents){
         //	// Fix for broken doors in build xxx-560.
         //	if(type == Material.WOODEN_DOOR.getId()) toggleSingleDoor(block);
         //}

         L10N.localizedMessage(player, null, "msg-user-break-owned", sign.getLine(1));
      }
   }

   @EventHandler( priority = EventPriority.LOW )
   public void onBlockPistonExtend( BlockPistonExtendEvent event ) {

      Block block = event.getBlock();

      // Check the block list for any protected blocks, and cancel the event if any are found.

      Block checkBlock;
      List<Block> blockList = event.getBlocks();
      int x, count = blockList.size();

      for (x = 0; x < count; ++x) {
         checkBlock = blockList.get(x);

         if (Utils.isProtected(checkBlock)) {
            event.setCancelled(true);
            return;
         }
      }

      // The above misses doors at the end of the chain, in the space the blocks are being pushed into.

      checkBlock = block.getRelative(getPistonFacing(block), event.getLength() + 1);

      if (Utils.isProtected(checkBlock)) {
         event.setCancelled(true);
         return;
      }
   }

   @EventHandler( priority = EventPriority.LOW, ignoreCancelled = true )
   public void onBlockPistonRetract( BlockPistonRetractEvent event ) {

      if (!(event.isSticky()))
         return;

      Block block = event.getBlock();
      Block checkBlock = block.getRelative(getPistonFacing(block), 2);
      //Block		checkBlock = event.getRetractLocation().getBlock();
      int type = checkBlock.getTypeId();

      // Skip those mats that cannot be pulled.

      if (type == Material.CHEST.getId())
         return;
      if (type == Material.TRAPPED_CHEST.getId())
         return;
      if (type == Material.DISPENSER.getId())
         return;
      if (type == Material.DROPPER.getId())
         return;
      if (type == Material.FURNACE.getId())
         return;
      if (type == Material.BURNING_FURNACE.getId())
         return;
      if (type == Material.WOODEN_DOOR.getId())
         return;
      if (type == Material.IRON_DOOR_BLOCK.getId())
         return;
      //if(type == Material.TRAP_DOOR.getId()) don't return

      if (Utils.isProtected(checkBlock))
         event.setCancelled(true);
   }

   @EventHandler( priority = EventPriority.LOW, ignoreCancelled = true )
   public void onBlockPlace( BlockPlaceEvent event ) {

      if (event.isCancelled())
         return;

      Player player = event.getPlayer();
      Block block = event.getBlockPlaced();
      int type = block.getTypeId();
      Block against = event.getBlockAgainst();
      Block checkBlock;
      Block signBlock;

      // Check if someone accidentally put any block on an owned sign.

      if (against.getTypeId() == Material.WALL_SIGN.getId()) {
         // Only cancel it for our signs.
         Sign sign = ( Sign ) against.getState();
         String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

         if (text.equals("[private]") || text.equalsIgnoreCase(L10N.getAltPrivate()) || text.equals("[more users]") || text.equalsIgnoreCase(L10N.getAltMoreUsers())) {
            event.setCancelled(true);
            return;
         }
      }

      // Check the placing of a door by a door here.
      // Though it is usually an item, not a block?  Is this still needed?

      if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.TRAP_DOOR.getId())
            || (type == Material.FENCE_GATE.getId())) {
         //player.sendMessage(ChatColor.DARK_PURPLE + "Lockette: Door block block has been placed");

         if (canBuildDoor(block, against, player))
            return;

         event.setCancelled(true);

         L10N.localizedMessage(player, null, "msg-user-conflict-door");
         return;
      }

      if (Configuration.useQuickProtect()) {
         if (type == Material.WALL_SIGN.getId()) {
            checkBlock = Utils.getSignAttachedBlock(block);

            if (checkBlock == null)
               return;

            type = checkBlock.getTypeId();

            if ((type == Material.CHEST.getId()) || (type == Material.TRAPPED_CHEST.getId()) || (type == Material.DISPENSER.getId())
                  || (type == Material.DROPPER.getId()) || (type == Material.FURNACE.getId()) || (type == Material.BURNING_FURNACE.getId())
                  || (type == Material.BREWING_STAND.getId()) || Utils.isInList(type, Configuration.getCustomBlockList())) {

               Sign sign = ( Sign ) block.getState();

               int length = player.getName().length();

               if (length > 15)
                  length = 15;

               if (Utils.isProtected(checkBlock)) {
                  // Add a users sign only if owner.
                  if (Utils.isOwner(checkBlock, player.getName())) {
                     sign.setLine(0, L10N.getAltMoreUsers());
                     sign.setLine(1, L10N.getAltEveryone());
                     sign.setLine(2, "");
                     sign.setLine(3, "");
                     sign.update(true);

                     L10N.localizedMessage(player, null, "msg-owner-adduser");
                  }
                  else
                     event.setCancelled(true);

                  return;
               }
               else {
                  // Check for permission first.
                  if (!checkPermissions(player, block, checkBlock)) {
                     event.setCancelled(true);

                     L10N.localizedMessage(player, null, "msg-error-permission");
                     return;
                  }
                  sign.setLine(0, L10N.getAltPrivate());
                  sign.setLine(1, player.getName());
                  sign.setLine(2, "");
                  sign.setLine(3, "");
                  sign.update(true);

                  Bukkit.getLogger().info("[" + lockette.getDescription().getName() + "] " + player.getName() + " has protected a block or door.");

                  L10N.localizedMessage(player, null, "msg-owner-claim");
               }
            }

            return;
         }
      }

      // The rest is for placing chests and hoppers only.		

      if ((type == Material.CHEST.getId()) || (type == Material.TRAPPED_CHEST.getId())) {

         // Count nearby chests to find illegal sized chests.

         int chests = Utils.findChestCountNear(block);

         if (chests > 1) {
            event.setCancelled(true);

            L10N.localizedMessage(player, null, "msg-user-illegal");
            return;
         }

         signBlock = Utils.findBlockOwner(block, null, false);

         if (signBlock != null) {
            // Expanding a private chest, see if its allowed.

            Sign sign = ( Sign ) signBlock.getState();
            int length = player.getName().length();

            if (length > 15)
               length = 15;

            // Check owner.
            if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length)))
               return;

            // If we got here, then not allowed.

            event.setCancelled(true);

            L10N.localizedMessage(player, null, "msg-user-resize-owned", sign.getLine(1));
         }
         else {
            // Only send one helpful message per user per session.

            if (playerList.get(player.getName()) == null) {
               // Associate the user with a non-null block, and print a helpful message.
               playerList.put(player.getName(), block);
               L10N.localizedMessage(player, null, "msg-help-chest");
            }
         }
      }

      // Hoppers from here.

      if (type == Material.HOPPER.getId()) {

         checkBlock = block.getRelative(BlockFace.UP);
         type = checkBlock.getTypeId();

         if ((type == Material.CHEST.getId()) || (type == Material.DISPENSER.getId()) || (type == Material.DROPPER.getId())
               || (type == Material.FURNACE.getId()) || (type == Material.BURNING_FURNACE.getId()) || (type == Material.BREWING_STAND.getId())
               || Utils.isInList(type, Configuration.getCustomBlockList())) {

            if (!validateOwner(checkBlock, player)) {

               event.setCancelled(true);

               L10N.localizedMessage(player, null, "msg-user-denied");
               return;
            }
         }

         checkBlock = block.getRelative(BlockFace.DOWN);
         type = checkBlock.getTypeId();

         if ((type == Material.CHEST.getId()) || (type == Material.DISPENSER.getId()) || (type == Material.DROPPER.getId())
               || (type == Material.FURNACE.getId()) || (type == Material.BURNING_FURNACE.getId()) || (type == Material.BREWING_STAND.getId())
               || Utils.isInList(type, Configuration.getCustomBlockList())) {

            if (!validateOwner(checkBlock, player)) {

               event.setCancelled(true);

               L10N.localizedMessage(player, null, "msg-user-denied");
               return;
            }
         }

      }

   }

   /**
    * Check permissions and external sources to see if we are allowed to place a private sign here
    * 
    * @return true if permitted
    */
   private boolean checkPermissions( Player player, Block block, Block checkBlock ) {

      int type = checkBlock.getTypeId();

      if (lockette.usingExternalZones()) {
         if (!lockette.canBuild(player, block)) {

            L10N.localizedMessage(player, null, "msg-error-zone", PluginCore.lastZoneDeny());
            return false;
         }

         if (!lockette.canBuild(player, checkBlock)) {

            L10N.localizedMessage(player, null, "msg-error-zone", PluginCore.lastZoneDeny());
            return false;
         }
      }

      if (lockette.usingExternalPermissions()) {
         boolean create = false;

         if (player.hasPermission("lockette.create.all"))
            create = true;
         else if (type == Material.CHEST.getId()) {
            if (player.hasPermission("lockette.user.create.chest"))
               create = true;
         }
         else if ((type == Material.FURNACE.getId()) || (type == Material.BURNING_FURNACE.getId())) {
            if (player.hasPermission("lockette.user.create.furnace"))
               create = true;
         }
         else if (type == Material.DISPENSER.getId()) {
            if (player.hasPermission("lockette.user.create.dispenser"))
               create = true;
         }
         else if (type == Material.DROPPER.getId()) {
            if (player.hasPermission("lockette.user.create.dropper"))
               create = true;
         }
         else if (type == Material.BREWING_STAND.getId()) {
            if (player.hasPermission("lockette.user.create.brewingstand"))
               create = true;
         }
         else if (Utils.isInList(type, Configuration.getCustomBlockList())) {
            if (player.hasPermission("lockette.user.create.custom"))
               create = true;
         }

         return create;
      }

      return true;
   }

   /**
    * Check for a private sign and check we are the owner of this block.
    * 
    * @param block
    * @param player
    * @return true if no owner or we are the owner named on the private sign.
    */
   private boolean validateOwner( Block block, Player player ) {

      Block signBlock = Utils.findBlockOwner(block, null, false);

      // No sign block so has no owner.
      if (signBlock == null)
         return true;

      Sign sign = ( Sign ) signBlock.getState();
      int length = player.getName().length();

      if (length > 15)
         length = 15;

      // Check owner.
      if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length)))
         return true;

      // Owner doesn't match so deny.
      return false;
   }

   @EventHandler( priority = EventPriority.LOW, ignoreCancelled = true )
   public void onBlockRedstoneChange( BlockRedstoneEvent event ) {

      Block block = event.getBlock();
      int type = block.getTypeId();
      boolean doCheck = false;

      if (Configuration.protectTrapdoors()) {
         if (type == Material.TRAP_DOOR.getId())
            doCheck = true;
      }

      if (Configuration.protectDoors()) {
         if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId()))
            doCheck = true;
      }

      if (doCheck) {
         // Lets see if everyone is allowed to activate.
         Block signBlock = Utils.findBlockOwner(block, null, false);

         if (signBlock == null)
            return;

         // Check main three users.

         Sign sign = ( Sign ) signBlock.getState();
         String line;
         int y;

         for (y = 1; y <= 3; ++y)
            if (!sign.getLine(y).isEmpty()) {
               line = sign.getLine(y).replaceAll("(?i)\u00A7[0-F]", "");

               if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(L10N.getAltEveryone()))
                  return;
            }

         // Check for more users.

         List<Block> list = Utils.findBlockUsers(block, signBlock);
         int x, count = list.size();

         for (x = 0; x < count; ++x) {
            sign = ( Sign ) list.get(x).getState();

            for (y = 1; y <= 3; ++y)
               if (!sign.getLine(y).isEmpty()) {
                  line = sign.getLine(y).replaceAll("(?i)\u00A7[0-F]", "");

                  if (line.equalsIgnoreCase("[Everyone]") || line.equalsIgnoreCase(L10N.getAltEveryone()))
                     return;
               }
         }

         // Don't have permission.

         event.setNewCurrent(event.getOldCurrent());
      }
   }

   @EventHandler( priority = EventPriority.LOW, ignoreCancelled = true )
   public void onSignChange( SignChangeEvent event ) {

      //if(event.isCancelled()) return;

      Player player = event.getPlayer();
      Block block = event.getBlock();
      boolean typeWallSign = (block.getTypeId() == Material.WALL_SIGN.getId());
      boolean typeSignPost = (block.getTypeId() == Material.SIGN_POST.getId());

      // But also need this along with stuff in PrefixListener

      if (typeWallSign) {
         Sign sign = ( Sign ) block.getState();
         String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "");

         if (text.equalsIgnoreCase("[Private]") || text.equalsIgnoreCase(L10N.getAltPrivate()) || text.equalsIgnoreCase("[More Users]")
               || text.equalsIgnoreCase(L10N.getAltMoreUsers())) {
            if (event.isCancelled())
               return;
            //event.setCancelled(true);
            //return;
         }
      }
      else if (typeSignPost) {

      }
      else {
         // Not a sign, wtf!
         event.setCancelled(true);
         return;
      }

      // Check for a new [Private] or [More Users] sign.

      String text = event.getLine(0).replaceAll("(?i)\u00A7[0-F]", "");

      if (text.equalsIgnoreCase("[Private]") || text.equalsIgnoreCase(L10N.getAltPrivate())) {
         //Player		player = event.getPlayer();
         //Block		block = event.getBlock();
         //boolean		typeWallSign = (block.getTypeId() == Material.WALL_SIGN.getId());
         boolean doChests = true, doFurnaces = true, doDispensers = true, doDroppers = true;
         boolean doBrewingStands = true, doCustoms = true;
         boolean doTrapDoors = true, doDoors = true;

         // Check for permission first.

         if (lockette.usingExternalZones()) {
            if (!lockette.canBuild(player, block)) {
               event.setLine(0, "[?]");

               L10N.localizedMessage(player, null, "msg-error-zone", PluginCore.lastZoneDeny());
               return;
            }
         }

         if (lockette.usingExternalPermissions()) {
            boolean create = false;

            doChests = false;
            doFurnaces = false;
            doDispensers = false;
            doDroppers = false;
            doBrewingStands = false;
            doCustoms = false;
            doTrapDoors = false;
            doDoors = false;

            if (player.hasPermission("lockette.create.all")) {
               create = true;
               doChests = true;
               doFurnaces = true;
               doDispensers = true;
               doDroppers = true;
               doBrewingStands = true;
               doCustoms = true;
               doTrapDoors = true;
               doDoors = true;
            }
            else {
               if (player.hasPermission("lockette.user.create.chest")) {
                  create = true;
                  doChests = true;
               }
               if (player.hasPermission("lockette.user.create.furnace")) {
                  create = true;
                  doFurnaces = true;
               }
               if (player.hasPermission("lockette.user.create.dispenser")) {
                  create = true;
                  doDispensers = true;
               }
               if (player.hasPermission("lockette.user.create.dropper")) {
                  create = true;
                  doDroppers = true;
               }
               if (player.hasPermission("lockette.user.create.brewingstand")) {
                  create = true;
                  doBrewingStands = true;
               }
               if (player.hasPermission("lockette.user.create.custom")) {
                  create = true;
                  doCustoms = true;
               }
               if (player.hasPermission("lockette.user.create.trapdoor")) {
                  create = true;
                  doTrapDoors = true;
               }
               if (player.hasPermission("lockette.user.create.door")) {
                  create = true;
                  doDoors = true;
               }
            }

            if (!create) {
               event.setLine(0, "[?]");

               L10N.localizedMessage(player, null, "msg-error-permission");
               return;
            }
         }

         int x;
         Block checkBlock[] = new Block[4];
         byte face = 0;
         int type = 0;
         boolean conflict = false;
         boolean deny = false;
         boolean zonedeny = false;

         // Check wall sign attached block for trap doors.

         if (Configuration.protectTrapdoors())
            if (typeWallSign) {
               checkBlock[3] = Utils.getSignAttachedBlock(block);

               if (checkBlock[3] != null)
                  if (!isInList(checkBlock[3].getTypeId(), materialListBad)) {
                     checkBlock[0] = checkBlock[3].getRelative(BlockFace.NORTH);
                     checkBlock[1] = checkBlock[3].getRelative(BlockFace.EAST);
                     checkBlock[2] = checkBlock[3].getRelative(BlockFace.SOUTH);
                     checkBlock[3] = checkBlock[3].getRelative(BlockFace.WEST);

                     for (x = 0; x < 4; ++x) {
                        if (checkBlock[x].getTypeId() == Material.TRAP_DOOR.getId()) {
                           if (Utils.findBlockOwner(checkBlock[x], block, true) == null) {
                              if (!doTrapDoors)
                                 deny = true;
                              else {
                                 face = block.getData();
                                 type = 4;
                                 break;
                              }
                           }
                        }
                     }
                     /*
                      * if(findBlockOwner(checkBlock[3], block,
                      * true) == null){ if(!doTrapDoors) deny = true;
                      * else{ face = block.getData(); type = 4; } }
                      */
                  }
            }

         // Check wall sign attached block for doors, above and below.

         if (Configuration.protectDoors())
            if (typeWallSign) {
               checkBlock[0] = Utils.getSignAttachedBlock(block);

               if (checkBlock[0] != null)
                  if (!isInList(checkBlock[0].getTypeId(), materialListBad)) {
                     checkBlock[1] = checkBlock[0].getRelative(BlockFace.UP);
                     checkBlock[2] = checkBlock[0].getRelative(BlockFace.DOWN);

                     if (isInList(checkBlock[1].getTypeId(), materialListDoors)) {
                        if (Utils.findBlockOwner(checkBlock[1], block, true) == null) {
                           if (isInList(checkBlock[2].getTypeId(), materialListDoors)) {
                              if (Utils.findBlockOwner(checkBlock[2], block, true) == null) {
                                 // unclaimed (unowned above, unowned below)
                                 if (!doDoors)
                                    deny = true;
                                 else {
                                    face = block.getData();
                                    type = 5;
                                 }
                              }
                              // else conflict (unowned above, but already owned below)
                              else
                                 conflict = true;
                           }
                           else {
                              // unclaimed (unowned above, empty below)
                              if (!doDoors)
                                 deny = true;
                              else {
                                 face = block.getData();
                                 type = 5;
                              }
                           }
                        }
                        else
                           conflict = true;
                     }
                     else if (isInList(checkBlock[2].getTypeId(), materialListDoors)) {
                        if (Utils.findBlockOwner(checkBlock[2], block, true) == null) {
                           // unclaimed (empty above, unowned below)
                           if (!doDoors)
                              deny = true;
                           else {
                              face = block.getData();
                              type = 5;
                           }
                        }
                        // else claimed (+ conflict) (empty above, already owned below)
                        else
                           conflict = true;
                     }
                     // else none (empty above, empty below)
                  }
            }

         // Reset trapdoor face if there is a conflict with a door.
         if (conflict == true) {
            face = 0;
            type = 0;
         }

         if (face == 0) {
            int lastType;

            // Check for chests first, dispensers second, furnaces third.

            checkBlock[0] = block.getRelative(BlockFace.NORTH);
            checkBlock[1] = block.getRelative(BlockFace.EAST);
            checkBlock[2] = block.getRelative(BlockFace.SOUTH);
            checkBlock[3] = block.getRelative(BlockFace.WEST);

            for (x = 0; x < 4; ++x) {
               if (lockette.usingExternalZones()) {
                  if (!lockette.canBuild(player, checkBlock[x])) {
                     zonedeny = true;
                     continue;
                  }
               }

               // Check if allowed by type.
               if ((checkBlock[x].getTypeId() == Material.CHEST.getId()) || (checkBlock[x].getTypeId() == Material.TRAPPED_CHEST.getId())) {
                  if (!doChests) {
                     deny = true;
                     continue;
                  }
                  lastType = 1;
               }
               else if (isInList(checkBlock[x].getTypeId(), materialListFurnaces)) {
                  if (!doFurnaces) {
                     deny = true;
                     continue;
                  }
                  lastType = 2;
               }
               else if (checkBlock[x].getTypeId() == Material.DISPENSER.getId()) {
                  if (!doDispensers) {
                     deny = true;
                     continue;
                  }
                  lastType = 3;
               }
               else if (checkBlock[x].getTypeId() == Material.DROPPER.getId()) {
                  if (!doDroppers) {
                     deny = true;
                     continue;
                  }
                  lastType = 8;
               }
               else if (checkBlock[x].getTypeId() == Material.BREWING_STAND.getId()) {
                  if (!doBrewingStands) {
                     deny = true;
                     continue;
                  }
                  lastType = 6;
               }
               else if (Utils.isInList(checkBlock[x].getTypeId(), Configuration.getCustomBlockList())) {
                  if (!doCustoms) {
                     deny = true;
                     continue;
                  }
                  lastType = 7;
               }
               else if (checkBlock[x].getTypeId() == Material.TRAP_DOOR.getId()) {
                  if (!Configuration.protectTrapdoors())
                     continue;
                  if (!doTrapDoors) {
                     deny = true;
                     continue;
                  }
                  lastType = 4;
               }
               else if (isInList(checkBlock[x].getTypeId(), materialListDoors)) {
                  if (!Configuration.protectDoors())
                     continue;
                  if (!doDoors) {
                     deny = true;
                     continue;
                  }
                  lastType = 5;
               }
               else
                  continue;

               // Allowed, lets see if it is claimed.
               if (Utils.findBlockOwner(checkBlock[x], block, true) == null) {
                  face = faceList[x];
                  type = lastType;
                  break;
               }
               // For when the last type is a door, and it is conflicting.
               else {
                  if (Configuration.protectTrapdoors())
                     if (doTrapDoors) {
                        if (checkBlock[x].getTypeId() == Material.TRAP_DOOR.getId()) {
                           conflict = true;
                        }
                     }
                  if (Configuration.protectDoors())
                     if (doDoors) {
                        if (isInList(checkBlock[x].getTypeId(), materialListDoors)) {
                           conflict = true;
                        }
                     }
               }
            }
         }

         // None found, send a message.

         if (face == 0) {
            event.setLine(0, "[?]");

            if (conflict)
               L10N.localizedMessage(player, null, "msg-error-claim-conflict");
            else if (zonedeny)
               L10N.localizedMessage(player, null, "msg-error-zone", PluginCore.lastZoneDeny());
            else if (deny)
               L10N.localizedMessage(player, null, "msg-error-permission");
            else
               L10N.localizedMessage(player, null, "msg-error-claim");
            return;
         }

         // Claim it...

         boolean anyone = true;
         int length = player.getName().length();

         if (event.getLine(1).isEmpty())
            anyone = false;
         if (length > 15)
            length = 15;

         // In case some other plugin messed with the cancel state.
         event.setCancelled(false);

         if (anyone) {
            // Check if allowed by type.
            if (type == 1) { // Chest
               if (!player.hasPermission("lockette.admin.create.chest"))
                  anyone = false;
            }
            else if (type == 2) { // Furnace
               if (!player.hasPermission("lockette.admin.create.furnace"))
                  anyone = false;
            }
            else if (type == 3) { // Dispenser
               if (!player.hasPermission("lockette.admin.create.dispenser"))
                  anyone = false;
            }
            else if (type == 8) { // Dropper
               if (!player.hasPermission("lockette.admin.create.dropper"))
                  anyone = false;
            }
            else if (type == 6) { // Brewing Stand
               if (!player.hasPermission("lockette.admin.create.brewingstand"))
                  anyone = false;
            }
            else if (type == 7) { // Custom
               if (!player.hasPermission("lockette.admin.create.custom"))
                  anyone = false;
            }
            else if (type == 4) { // Trap Door
               if (!player.hasPermission("lockette.admin.create.trapdoor"))
                  anyone = false;
            }
            else if (type == 5) { // Door
               if (!player.hasPermission("lockette.admin.create.door"))
                  anyone = false;
            }
            else
               anyone = false;
         }

         if (!anyone)
            event.setLine(1, player.getName().substring(0, length));

         if (!typeWallSign) {
            // Set to wall type.
            block.setType(Material.WALL_SIGN);
            block.setData(face);

            // Re-set the text.
            Sign sign = ( Sign ) block.getState();

            sign.setLine(0, event.getLine(0));
            sign.setLine(1, event.getLine(1));
            sign.setLine(2, event.getLine(2));
            sign.setLine(3, event.getLine(3));
            sign.update(true);
         }
         else
            block.setData(face);

         // All done!

         if (anyone) {
            Bukkit.getLogger().info(
                  "[" + lockette.getDescription().getName() + "] (Admin) " + player.getName() + " has claimed a container for " + event.getLine(1) + ".");

            if (!lockette.playerOnline(event.getLine(1)))
               L10N.localizedMessage(player, null, "msg-admin-claim-error", event.getLine(1));
            else
               L10N.localizedMessage(player, null, "msg-admin-claim", event.getLine(1));
         }
         else {
            Bukkit.getLogger().info("[" + lockette.getDescription().getName() + "] " + player.getName() + " has claimed a container.");

            L10N.localizedMessage(player, null, "msg-owner-claim");
         }
      }
      else if (text.equalsIgnoreCase("[More Users]") || text.equalsIgnoreCase(L10N.getAltMoreUsers())) {
         //Player		player = event.getPlayer();
         //Block		block = event.getBlock();
         //boolean		typeWallSign = (block.getTypeId() == Material.WALL_SIGN.getId());

         int x;
         Block checkBlock[] = new Block[4];
         Block signBlock = null;
         Sign sign = null;
         byte face = 0;
         //int			type = 0;

         int length = player.getName().length();

         if (length > 15)
            length = 15;

         // Check wall sign attached block for owner.

         if (Configuration.protectDoors() || Configuration.protectTrapdoors()) {
            if (typeWallSign) {
               checkBlock[0] = Utils.getSignAttachedBlock(block);

               if (checkBlock[0] != null)
                  if (!isInList(checkBlock[0].getTypeId(), materialListBad)) {
                     signBlock = Utils.findBlockOwner(checkBlock[0], null, false);

                     if (signBlock != null) {
                        sign = ( Sign ) signBlock.getState();

                        // Check owner.
                        if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
                           face = block.getData();
                        }
                     }
                  }
            }
         }

         if (face == 0) {
            // Check for chests first, dispensers second, furnaces third.

            checkBlock[0] = block.getRelative(BlockFace.NORTH);
            checkBlock[1] = block.getRelative(BlockFace.EAST);
            checkBlock[2] = block.getRelative(BlockFace.SOUTH);
            checkBlock[3] = block.getRelative(BlockFace.WEST);

            for (x = 0; x < 4; ++x) {
               if (!isInList(checkBlock[x].getTypeId(), materialList))
                  continue;

               if (!Configuration.protectTrapdoors()) {
                  if (checkBlock[x].getTypeId() == Material.TRAP_DOOR.getId())
                     continue;
               }

               if (!Configuration.protectDoors()) {
                  if (isInList(checkBlock[x].getTypeId(), materialListDoors))
                     continue;
               }

               signBlock = Utils.findBlockOwner(checkBlock[x], null, false);

               if (signBlock != null) {
                  sign = ( Sign ) signBlock.getState();

                  // Check owner.
                  if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(player.getName().substring(0, length))) {
                     face = faceList[x];
                     //type = y;
                     break;
                  }
               }
            }
         }

         // None found, send a message.

         if (face == 0) {
            event.setLine(0, "[?]");
            if (sign != null) {
               L10N.localizedMessage(player, null, "msg-error-adduser-owned", sign.getLine(1));
            }
            else {
               L10N.localizedMessage(player, null, "msg-error-adduser");
            }
            return;
         }

         // Add the users sign.

         // In case some other plugin messed with the cancel state.
         event.setCancelled(false);
         if (!typeWallSign) {
            // Set to wall type.
            block.setType(Material.WALL_SIGN);
            block.setData(face);

            // Re-set the text.
            //Sign		
            sign = ( Sign ) block.getState();

            sign.setLine(0, event.getLine(0));
            sign.setLine(1, event.getLine(1));
            sign.setLine(2, event.getLine(2));
            sign.setLine(3, event.getLine(3));
            sign.update(true);

         }
         else
            block.setData(face);

         // All done!

         L10N.localizedMessage(player, null, "msg-owner-adduser");
      }
   }

   //********************************************************************************************************************
   // Start of utility section

   // Returns true if it should be allowed, false if it should be canceled.
   private static boolean canBuildDoor( Block block, Block against, Player player ) {

      Block checkBlock;
      //Sign		sign;
      //int			length = player.getName().length();

      //if(length > 15) length = 15;

      // Check block below for doors or block to side for trapdoors.

      if (!Utils.isOwner(against, player.getName()))
         return (false);

      if (Configuration.protectTrapdoors())
         if (block.getTypeId() == Material.TRAP_DOOR.getId()) {
            //if(!isOwner(getTrapDoorAttachedBlock(block), player.getName())) return(false);
            //if(!isOwner(block, player.getName())) return(false); // Failed as block data is bad, same as above.
            //if(!isOwner(against, player.getName())) return(false);
            return (true);
         }

      // Check block above door.

      if (!Utils.isOwner(against.getRelative(BlockFace.UP, 3), player.getName()))
         return (false);

      // Check neighboring doors.

      checkBlock = block.getRelative(BlockFace.NORTH);
      if (checkBlock.getTypeId() == block.getTypeId()) {
         if (!Utils.isOwner(checkBlock, player.getName()))
            return (false);
      }

      checkBlock = block.getRelative(BlockFace.EAST);
      if (checkBlock.getTypeId() == block.getTypeId()) {
         if (!Utils.isOwner(checkBlock, player.getName()))
            return (false);
      }

      checkBlock = block.getRelative(BlockFace.SOUTH);
      if (checkBlock.getTypeId() == block.getTypeId()) {
         if (!Utils.isOwner(checkBlock, player.getName()))
            return (false);
      }

      checkBlock = block.getRelative(BlockFace.WEST);
      if (checkBlock.getTypeId() == block.getTypeId()) {
         if (!Utils.isOwner(checkBlock, player.getName()))
            return (false);
      }

      return (true);
   }

   private BlockFace getPistonFacing( Block block ) {
      int type = block.getTypeId();

      if ((type != Material.PISTON_BASE.getId()) && (type != Material.PISTON_STICKY_BASE.getId()) && (type != Material.PISTON_EXTENSION.getId())) {
         return BlockFace.SELF;
      }

      int face = block.getData() & 0x7;

      switch (face) {
      case 0:
         return BlockFace.DOWN;
      case 1:
         return BlockFace.UP;
      case 2:
         return BlockFace.NORTH;
      case 3:
         return BlockFace.SOUTH;
      case 4:
         return BlockFace.WEST;
      case 5:
         return BlockFace.EAST;
      }

      return BlockFace.SELF;
   }
   
   private boolean isInList(int target, int[] list) {

      if (list == null)
         return (false);
      for (int x = 0; x < list.length; ++x)
         if (target == list[x])
            return (true);
      return (false);
   }
}
