package com.ejfirestar.lockette.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import com.ejfirestar.lockette.config.Configuration;
import com.ejfirestar.lockette.config.L10N;

public class Utils {

   public static boolean isProtected( Block block ) {

      int type = block.getTypeId();

      if (type == Material.WALL_SIGN.getId()) {
         Sign sign = ( Sign ) block.getState();
         String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

         if (text.equals("[private]") || text.equalsIgnoreCase(L10N.getAltPrivate())) {
            return true;
         }
         else if (text.equals("[more users]") || text.equalsIgnoreCase(L10N.getAltMoreUsers())) {
            Block checkBlock = getSignAttachedBlock(block);

            if (checkBlock != null)
               if (findBlockOwner(checkBlock, null, false) != null) {
                  return true;
               }
         }
      }
      else if (findBlockOwner(block, null, false) != null) {
         return true;
      }

      return false;
   }

   public static boolean isOwner( Block block, String name ) {

      Block checkBlock = findBlockOwner(block, null, false);

      if (checkBlock == null)
         return (true);

      Sign sign = ( Sign ) checkBlock.getState();
      int length = name.length();

      if (length > 15)
         length = 15;

      // Check owner only.
      if (sign.getLine(1).replaceAll("(?i)\u00A7[0-F]", "").equals(name.substring(0, length))) {
         return (true);
      }

      return (false);
   }

   // Version for finding conflicts, when creating a new sign.
   // Ignore the sign being made, in case another plugin has set the text of the sign prematurely.
   public static Block findBlockOwner( Block block, Block ignoreBlock, boolean iterateFurther ) {
      int type = block.getTypeId();
      Location ignore;

      if (ignoreBlock != null)
         ignore = ignoreBlock.getLocation();
      else
         ignore = null;

      // Check known block types.

      if ((type == Material.CHEST.getId()) || (type == Material.TRAPPED_CHEST.getId())) {
         return (findBlockOwnerBase(block, ignore, true, false, false, false, false));
      }

      if ((type == Material.DISPENSER.getId()) || (type == Material.DROPPER.getId()) || (type == Material.FURNACE.getId())
            || (type == Material.BURNING_FURNACE.getId()) || (type == Material.BREWING_STAND.getId())
            || isInList(type, Configuration.getCustomBlockList())) {
         return (findBlockOwnerBase(block, ignore, false, false, false, false, false));
      }

      if (Configuration.protectTrapdoors()) {
         if (type == Material.TRAP_DOOR.getId()) {
            // Need to check block it is attached to as well as other attached trap doors.
            //return(findBlockOwnerBase(block, ignore, false, false, false, false, false));
            return (findBlockOwner(getTrapDoorAttachedBlock(block), ignoreBlock, false));
         }
      }

      if (Configuration.protectDoors()) {
         if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId())) {
            return (findBlockOwnerBase(block, ignore, true, true, true, true, iterateFurther));
         }
      }

      Block checkBlock, result;

      if (Configuration.protectTrapdoors()) {
         // Check base block, as it might have the sign and it isn't checked below.

         checkBlock = findBlockOwnerBase(block, ignore, false, false, false, false, false);
         if (checkBlock != null)
            return (checkBlock);

         // Need to check if there is a trap door attached to the block, and check for a sign attached there.

         checkBlock = block.getRelative(BlockFace.NORTH);
         if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId()) {
            if ((checkBlock.getData() & 0x3) == 2) {
               checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, false, false);
               if (checkBlock != null)
                  return (checkBlock);
            }
         }

         checkBlock = block.getRelative(BlockFace.EAST);
         if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId()) {
            if ((checkBlock.getData() & 0x3) == 0) {
               checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, false, false);
               if (checkBlock != null)
                  return (checkBlock);
            }
         }

         checkBlock = block.getRelative(BlockFace.SOUTH);
         if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId()) {
            if ((checkBlock.getData() & 0x3) == 3) {
               checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, false, false);
               if (checkBlock != null)
                  return (checkBlock);
            }
         }

         checkBlock = block.getRelative(BlockFace.WEST);
         if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId()) {
            if ((checkBlock.getData() & 0x3) == 1) {
               checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, false, false);
               if (checkBlock != null)
                  return (checkBlock);
            }
         }
      }

      if (Configuration.protectDoors()) {
         // Don't check the block but check for doors above then below the block, which includes the block.

         checkBlock = block.getRelative(BlockFace.UP);
         type = checkBlock.getTypeId();
         if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId())) {
            // Handle door above type.

            result = findBlockOwnerBase(checkBlock, ignore, true, true, true, true, iterateFurther);
            if (result != null)
               return (result);
         }

         // This is needed to protect the other block above double doors.

         checkBlock = block.getRelative(BlockFace.DOWN);
         type = checkBlock.getTypeId();
         if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId())) {
            // For door below only.
            // Don't include the block below door, as a sign there would not protect the target block.

            Block checkBlock2 = checkBlock.getRelative(BlockFace.DOWN);
            type = checkBlock2.getTypeId();
            if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId())) {
               return (findBlockOwnerBase(checkBlock2, ignore, true, true, false, true, iterateFurther));
            }
            else {
               return (findBlockOwnerBase(checkBlock, ignore, true, true, false, true, iterateFurther));
            }
         }
      }

      return (null);
   }

   // Version for determining if a container is released.
   // Should return non-null if destroying the block will surely cause the the sign to fall off.
   // Okay for trap doors, though could be optimized.
   public static Block findBlockOwnerBreak( Block block ) {
      int type = block.getTypeId();

      // Check known block types.

      if ((type == Material.CHEST.getId()) || (type == Material.TRAPPED_CHEST.getId())) {
         return (findBlockOwnerBase(block, null, false, false, false, false, false));
      }
      if ((type == Material.DISPENSER.getId()) || (type == Material.DROPPER.getId()) || (type == Material.FURNACE.getId())
            || (type == Material.BURNING_FURNACE.getId()) || (type == Material.BREWING_STAND.getId())
            || isInList(type, Configuration.getCustomBlockList())) {
         return (findBlockOwnerBase(block, null, false, false, false, false, false));
      }
      if (Configuration.protectTrapdoors())
         if (type == Material.TRAP_DOOR.getId()) {
            return (findBlockOwnerBase(block, null, false, false, false, false, false));
         }
      if (Configuration.protectDoors())
         if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId())) {
            return (findBlockOwnerBase(block, null, false, true, true, false, false));
         }

      Block checkBlock;

      // This should be edited if invalid signs can be destroyed..........
      checkBlock = findBlockOwnerBase(block, null, false, false, false, false, false);
      if (checkBlock != null)
         return (checkBlock);

      if (Configuration.protectTrapdoors()) {
         // Need to check if there is a trap door attached to the block, and check for a sign attached there.
         // This is the bit that could be optimized.

         checkBlock = block.getRelative(BlockFace.NORTH);
         if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId()) {
            if ((checkBlock.getData() & 0x3) == 2) {
               checkBlock = findBlockOwnerBase(checkBlock, null, false, false, false, false, false);
               if (checkBlock != null)
                  return (checkBlock);
            }
         }

         checkBlock = block.getRelative(BlockFace.EAST);
         if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId()) {
            if ((checkBlock.getData() & 0x3) == 0) {
               checkBlock = findBlockOwnerBase(checkBlock, null, false, false, false, false, false);
               if (checkBlock != null)
                  return (checkBlock);
            }
         }

         checkBlock = block.getRelative(BlockFace.SOUTH);
         if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId()) {
            if ((checkBlock.getData() & 0x3) == 3) {
               checkBlock = findBlockOwnerBase(checkBlock, null, false, false, false, false, false);
               if (checkBlock != null)
                  return (checkBlock);
            }
         }

         checkBlock = block.getRelative(BlockFace.WEST);
         if (checkBlock.getTypeId() == Material.TRAP_DOOR.getId()) {
            if ((checkBlock.getData() & 0x3) == 1) {
               checkBlock = findBlockOwnerBase(checkBlock, null, false, false, false, false, false);
               if (checkBlock != null)
                  return (checkBlock);
            }
         }
      }

      if (Configuration.protectDoors()) {
         // Need to check if there is a door above block, and check for a sign attached there.

         checkBlock = block.getRelative(BlockFace.UP);
         type = checkBlock.getTypeId();

         if ((type != Material.WOODEN_DOOR.getId()) && (type != Material.IRON_DOOR_BLOCK.getId()) && (type != Material.FENCE_GATE.getId())) {
            // Handle door above type.

            return (findBlockOwnerBase(checkBlock, null, false, true, true, false, false));
         }
      }

      return (null);
   }

   // Should only be called by the above related functions.
   // Should generally not be passed a hinge block, only a known container or door.
   private static Block findBlockOwnerBase( Block block, Location ignore, boolean iterate, boolean iterateUp, boolean iterateDown,
         boolean includeEnds, boolean iterateFurther ) {
      Block checkBlock;
      int type;
      byte face;
      boolean doCheck;

      // Check up and down along door surfaces, with a recursive call and iterate false.

      if (iterateUp) {
         checkBlock = block.getRelative(BlockFace.UP);
         type = checkBlock.getTypeId();

         if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId())) {
            checkBlock = findBlockOwnerBase(checkBlock, ignore, false, iterateUp, false, includeEnds, false);
         }
         else if (includeEnds)
            checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, includeEnds, false);
         else
            checkBlock = null;

         if (checkBlock != null)
            return (checkBlock);
      }

      if (iterateDown) {
         checkBlock = block.getRelative(BlockFace.DOWN);
         type = checkBlock.getTypeId();

         if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId())) {
            checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, iterateDown, includeEnds, false);
         }
         else if (includeEnds)
            checkBlock = findBlockOwnerBase(checkBlock, ignore, false, false, false, includeEnds, false);
         else
            checkBlock = null;

         if (checkBlock != null)
            return (checkBlock);
      }

      // Check around the originating block, in the order NESW.
      // If a sign is found and it is not the ignored block, check the text.
      // If it is not a sign and iterate is true, do a recursive call with iterate false.
      // (Or further, though this currently backtracks slightly.)

      checkBlock = block.getRelative(BlockFace.NORTH);
      if (checkBlock.getTypeId() == Material.WALL_SIGN.getId()) {
         face = checkBlock.getData();
         if (face == 2) {
            // Ignore a sign being created.

            if (ignore == null)
               doCheck = true;
            else if (checkBlock.getLocation().equals(ignore))
               doCheck = false;
            else
               doCheck = true;

            if (doCheck) {
               Sign sign = ( Sign ) checkBlock.getState();
               String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

               if (text.equals("[private]") || text.equalsIgnoreCase(L10N.getAltPrivate()))
                  return (checkBlock);
            }
         }
      }
      else if (iterate)
         if (checkBlock.getTypeId() == block.getTypeId()) {
            checkBlock = findBlockOwnerBase(checkBlock, ignore, iterateFurther, iterateUp, iterateDown, includeEnds, false);
            if (checkBlock != null)
               return (checkBlock);
         }

      checkBlock = block.getRelative(BlockFace.EAST);
      if (checkBlock.getTypeId() == Material.WALL_SIGN.getId()) {
         face = checkBlock.getData();
         if (face == 5) {
            // Ignore a sign being created.

            if (ignore == null)
               doCheck = true;
            else if (checkBlock.getLocation().equals(ignore))
               doCheck = false;
            else
               doCheck = true;

            if (doCheck) {
               Sign sign = ( Sign ) checkBlock.getState();
               String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

               if (text.equals("[private]") || text.equalsIgnoreCase(L10N.getAltPrivate()))
                  return (checkBlock);
            }
         }
      }
      else if (iterate)
         if (checkBlock.getTypeId() == block.getTypeId()) {
            checkBlock = findBlockOwnerBase(checkBlock, ignore, iterateFurther, iterateUp, iterateDown, includeEnds, false);
            if (checkBlock != null)
               return (checkBlock);
         }

      checkBlock = block.getRelative(BlockFace.SOUTH);
      if (checkBlock.getTypeId() == Material.WALL_SIGN.getId()) {
         face = checkBlock.getData();
         if (face == 3) {
            // Ignore a sign being created.

            if (ignore == null)
               doCheck = true;
            else if (checkBlock.getLocation().equals(ignore))
               doCheck = false;
            else
               doCheck = true;

            if (doCheck) {
               Sign sign = ( Sign ) checkBlock.getState();
               String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

               if (text.equals("[private]") || text.equalsIgnoreCase(L10N.getAltPrivate()))
                  return (checkBlock);
            }
         }
      }
      else if (iterate)
         if (checkBlock.getTypeId() == block.getTypeId()) {
            checkBlock = findBlockOwnerBase(checkBlock, ignore, iterateFurther, iterateUp, iterateDown, includeEnds, false);
            if (checkBlock != null)
               return (checkBlock);
         }

      checkBlock = block.getRelative(BlockFace.WEST);
      if (checkBlock.getTypeId() == Material.WALL_SIGN.getId()) {
         face = checkBlock.getData();
         if (face == 4) {
            // Ignore a sign being created.

            if (ignore == null)
               doCheck = true;
            else if (checkBlock.getLocation().equals(ignore))
               doCheck = false;
            else
               doCheck = true;

            if (doCheck) {
               Sign sign = ( Sign ) checkBlock.getState();
               String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

               if (text.equals("[private]") || text.equalsIgnoreCase(L10N.getAltPrivate()))
                  return (checkBlock);
            }
         }
      }
      else if (iterate)
         if (checkBlock.getTypeId() == block.getTypeId()) {
            checkBlock = findBlockOwnerBase(checkBlock, ignore, iterateFurther, iterateUp, iterateDown, includeEnds, false);
            if (checkBlock != null)
               return (checkBlock);
         }

      return (null);
   }

   public static List<Block> findBlockUsers( Block block, Block signBlock ) {
      int type = block.getTypeId();

      if ((type == Material.CHEST.getId()) || (type == Material.TRAPPED_CHEST.getId()))
         return (findBlockUsersBase(block, true, false, false, false, 0));
      if (Configuration.protectTrapdoors())
         if (type == Material.TRAP_DOOR.getId()) {
            return (findBlockUsersBase(getTrapDoorAttachedBlock(block), false, false, false, true, 0));
         }
      if (Configuration.protectDoors())
         if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId())) {
            return (findBlockUsersBase(block, true, true, true, false, signBlock.getY()));
         }
      return (findBlockUsersBase(block, false, false, false, false, 0));
   }

   private static List<Block> findBlockUsersBase( Block block, boolean iterate, boolean iterateUp, boolean iterateDown, boolean traps, int includeYPos ) {
      Block checkBlock;
      int type;
      byte face;
      List<Block> list = new ArrayList<Block>();

      // Experimental door code, check up and down.

      if (iterateUp) {
         checkBlock = block.getRelative(BlockFace.UP);
         type = checkBlock.getTypeId();

         if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId())) {
            list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, false, false, includeYPos));
         }
         // Limitation for more users sign.
         else if (checkBlock.getY() == includeYPos)
            list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
      }

      if (iterateDown) {
         checkBlock = block.getRelative(BlockFace.DOWN);
         type = checkBlock.getTypeId();

         if ((type == Material.WOODEN_DOOR.getId()) || (type == Material.IRON_DOOR_BLOCK.getId()) || (type == Material.FENCE_GATE.getId())) {
            list.addAll(findBlockUsersBase(checkBlock, false, false, iterateDown, false, includeYPos));
         }
         // No limitation here.
         else
            list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
      }

      // Check around the originating block, in the order NESW.

      checkBlock = block.getRelative(BlockFace.NORTH);
      type = checkBlock.getTypeId();
      if (type == Material.WALL_SIGN.getId()) {
         face = checkBlock.getData();
         if (face == 2) {
            Sign sign = ( Sign ) checkBlock.getState();
            String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

            if (text.equals("[more users]") || text.equalsIgnoreCase(L10N.getAltMoreUsers()))
               list.add(checkBlock);
         }
      }
      else if (iterate) {
         if (type == block.getTypeId()) {
            list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, iterateDown, false, includeYPos));
         }
      }
      else if (traps)
         if (type == Material.TRAP_DOOR.getId()) {
            face = checkBlock.getData();
            if ((face & 3) == 2) {
               list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
            }
         }

      checkBlock = block.getRelative(BlockFace.EAST);
      type = checkBlock.getTypeId();
      if (type == Material.WALL_SIGN.getId()) {
         face = checkBlock.getData();
         if (face == 5) {
            Sign sign = ( Sign ) checkBlock.getState();
            String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

            if (text.equals("[more users]") || text.equalsIgnoreCase(L10N.getAltMoreUsers()))
               list.add(checkBlock);
         }
      }
      else if (iterate) {
         if (type == block.getTypeId()) {
            list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, iterateDown, false, includeYPos));
         }
      }
      else if (traps)
         if (type == Material.TRAP_DOOR.getId()) {
            face = checkBlock.getData();
            if ((face & 3) == 0) {
               list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
            }
         }

      checkBlock = block.getRelative(BlockFace.SOUTH);
      type = checkBlock.getTypeId();
      if (type == Material.WALL_SIGN.getId()) {
         face = checkBlock.getData();
         if (face == 3) {
            Sign sign = ( Sign ) checkBlock.getState();
            String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

            if (text.equals("[more users]") || text.equalsIgnoreCase(L10N.getAltMoreUsers()))
               list.add(checkBlock);
         }
      }
      else if (iterate) {
         if (type == block.getTypeId()) {
            list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, iterateDown, false, includeYPos));
         }
      }
      else if (traps)
         if (type == Material.TRAP_DOOR.getId()) {
            face = checkBlock.getData();
            if ((face & 3) == 3) {
               list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
            }
         }

      checkBlock = block.getRelative(BlockFace.WEST);
      type = checkBlock.getTypeId();
      if (type == Material.WALL_SIGN.getId()) {
         face = checkBlock.getData();
         if (face == 4) {
            Sign sign = ( Sign ) checkBlock.getState();
            String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase();

            if (text.equals("[more users]") || text.equalsIgnoreCase(L10N.getAltMoreUsers()))
               list.add(checkBlock);
         }
      }
      else if (iterate) {
         if (type == block.getTypeId()) {
            list.addAll(findBlockUsersBase(checkBlock, false, iterateUp, iterateDown, false, includeYPos));
         }
      }
      else if (traps)
         if (type == Material.TRAP_DOOR.getId()) {
            face = checkBlock.getData();
            if ((face & 3) == 1) {
               list.addAll(findBlockUsersBase(checkBlock, false, false, false, false, includeYPos));
            }
         }

      return (list);
   }

   public static void rotateChestOrientation( Block block, BlockFace blockFace ) {

      if ((block.getTypeId() != Material.CHEST.getId()) && (block.getTypeId() != Material.TRAPPED_CHEST.getId()))
         return;
      if (!Configuration.allowChestRotation())
         if (block.getData() != 0)
            return;

      byte face;

      if (blockFace == BlockFace.NORTH)
         face = 2;
      else if (blockFace == BlockFace.EAST)
         face = 5;
      else if (blockFace == BlockFace.SOUTH)
         face = 3;
      else if (blockFace == BlockFace.WEST)
         face = 4;
      else
         return;

      Block checkBlock;

      checkBlock = block.getRelative(BlockFace.NORTH);
      if (((checkBlock.getTypeId() == Material.CHEST.getId()) || (checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()))
            && (checkBlock.getTypeId() == block.getTypeId())) {
         if ((face == 4) || (face == 5)) {
            block.setData(face);
            checkBlock.setData(face);
         }
         return;
      }

      checkBlock = block.getRelative(BlockFace.EAST);
      if (((checkBlock.getTypeId() == Material.CHEST.getId()) || (checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()))
            && (checkBlock.getTypeId() == block.getTypeId())) {
         if ((face == 2) || (face == 3)) {
            block.setData(face);
            checkBlock.setData(face);
         }
         return;
      }

      checkBlock = block.getRelative(BlockFace.SOUTH);
      if (((checkBlock.getTypeId() == Material.CHEST.getId()) || (checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()))
            && (checkBlock.getTypeId() == block.getTypeId())) {
         if ((face == 4) || (face == 5)) {
            block.setData(face);
            checkBlock.setData(face);
         }
         return;
      }

      checkBlock = block.getRelative(BlockFace.WEST);
      if (((checkBlock.getTypeId() == Material.CHEST.getId()) || (checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()))
            && (checkBlock.getTypeId() == block.getTypeId())) {
         if ((face == 2) || (face == 3)) {
            block.setData(face);
            checkBlock.setData(face);
         }
         return;
      }

      block.setData(face);
   }

   public static Block getSignAttachedBlock( Block block ) {
      if (block.getTypeId() != Material.WALL_SIGN.getId()) {
         return null;
      }

      int face = block.getData() & 0x7;

      if (face == 3) {
         return block.getRelative(BlockFace.NORTH);
      }
      if (face == 4) {
         return block.getRelative(BlockFace.EAST);
      }
      if (face == 2) {
         return block.getRelative(BlockFace.SOUTH);
      }
      if (face == 5) {
         return block.getRelative(BlockFace.WEST);
      }

      return null;
   }

   public static Block getTrapDoorAttachedBlock( Block block ) {
      if (block.getTypeId() != Material.TRAP_DOOR.getId())
         return null;

      int face = block.getData() & 0x3;

      if (face == 1) {
         return block.getRelative(BlockFace.NORTH);
      }
      if (face == 2) {
         return block.getRelative(BlockFace.EAST);
      }
      if (face == 0) {
         return block.getRelative(BlockFace.SOUTH);
      }
      if (face == 3) {
         return block.getRelative(BlockFace.WEST);
      }

      return null;
   }

   public static boolean isInList( Object target, List<Object> list ) {
      if (list == null)
         return (false);
      for (int x = 0; x < list.size(); ++x)
         if (list.get(x).equals(target))
            return (true);
      return (false);
   }

   // Toggle all doors.  (Used by rightclick action to get door list.)
   public static List<Block> toggleDoors( Block block, Block keyBlock, boolean wooden, boolean trap ) {
      List<Block> list = new ArrayList<Block>();

      toggleDoorBase(block, keyBlock, !trap, wooden, list);
      try {
         if (!wooden)
            block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
      }
      catch (NoSuchFieldError ex) {}
      catch (NoSuchMethodError ex) {}
      catch (NoClassDefFoundError ex) {}

      return (list);
   }

   // Main recursive function for toggling a door pair.  (No good for trap doors.)
   private static void toggleDoorBase( Block block, Block keyBlock, boolean iterateUpDown, boolean skipDoor, List<Block> list ) {
      Block checkBlock;

      // Toggle this door.

      if (list != null)
         list.add(block);
      if (!skipDoor)
         block.setData(( byte ) (block.getData() ^ 4));

      // Check up and down.

      if (iterateUpDown) {
         checkBlock = block.getRelative(BlockFace.UP);
         if (checkBlock.getTypeId() == block.getTypeId())
            toggleDoorBase(checkBlock, null, false, skipDoor, list);

         checkBlock = block.getRelative(BlockFace.DOWN);
         if (checkBlock.getTypeId() == block.getTypeId())
            toggleDoorBase(checkBlock, null, false, skipDoor, list);
      }

      // Check around the originating block, in the order NESW.

      if (keyBlock != null) {
         checkBlock = block.getRelative(BlockFace.NORTH);
         if (checkBlock.getTypeId() == block.getTypeId()) {
            if (((checkBlock.getX() == keyBlock.getX()) && (checkBlock.getZ() == keyBlock.getZ()))
                  || ((block.getX() == keyBlock.getX()) && (block.getZ() == keyBlock.getZ()))) {
               toggleDoorBase(checkBlock, null, true, false, list);
            }
         }

         checkBlock = block.getRelative(BlockFace.EAST);
         if (checkBlock.getTypeId() == block.getTypeId()) {
            if (((checkBlock.getX() == keyBlock.getX()) && (checkBlock.getZ() == keyBlock.getZ()))
                  || ((block.getX() == keyBlock.getX()) && (block.getZ() == keyBlock.getZ()))) {
               toggleDoorBase(checkBlock, null, true, false, list);
            }
         }

         checkBlock = block.getRelative(BlockFace.SOUTH);
         if (checkBlock.getTypeId() == block.getTypeId()) {
            if (((checkBlock.getX() == keyBlock.getX()) && (checkBlock.getZ() == keyBlock.getZ()))
                  || ((block.getX() == keyBlock.getX()) && (block.getZ() == keyBlock.getZ()))) {
               toggleDoorBase(checkBlock, null, true, false, list);
            }
         }

         checkBlock = block.getRelative(BlockFace.WEST);
         if (checkBlock.getTypeId() == block.getTypeId()) {
            if (((checkBlock.getX() == keyBlock.getX()) && (checkBlock.getZ() == keyBlock.getZ()))
                  || ((block.getX() == keyBlock.getX()) && (block.getZ() == keyBlock.getZ()))) {
               toggleDoorBase(checkBlock, null, true, false, list);
            }
         }
      }
   }

   public static int findChestCountNear( Block block ) {
      return (findChestCountNearBase(block, ( byte ) 0));
   }

   private static int findChestCountNearBase( Block block, byte face ) {
      int count = 0;
      Block checkBlock;

      if (face != 2) {
         checkBlock = block.getRelative(BlockFace.NORTH);
         if (((checkBlock.getTypeId() == Material.CHEST.getId()) || (checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()))
               && (checkBlock.getTypeId() == block.getTypeId())) {
            ++count;
            if (face == 0)
               count += findChestCountNearBase(checkBlock, ( byte ) 3);
         }
      }

      if (face != 5) {
         checkBlock = block.getRelative(BlockFace.EAST);
         if (((checkBlock.getTypeId() == Material.CHEST.getId()) || (checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()))
               && (checkBlock.getTypeId() == block.getTypeId())) {
            ++count;
            if (face == 0)
               count += findChestCountNearBase(checkBlock, ( byte ) 4);
         }
      }

      if (face != 3) {
         checkBlock = block.getRelative(BlockFace.SOUTH);
         if (((checkBlock.getTypeId() == Material.CHEST.getId()) || (checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()))
               && (checkBlock.getTypeId() == block.getTypeId())) {
            ++count;
            if (face == 0)
               count += findChestCountNearBase(checkBlock, ( byte ) 2);
         }
      }

      if (face != 4) {
         checkBlock = block.getRelative(BlockFace.WEST);
         if (((checkBlock.getTypeId() == Material.CHEST.getId()) || (checkBlock.getTypeId() == Material.TRAPPED_CHEST.getId()))
               && (checkBlock.getTypeId() == block.getTypeId())) {
            ++count;
            if (face == 0)
               count += findChestCountNearBase(checkBlock, ( byte ) 5);
         }
      }

      return (count);
   }

   public static int getSignOption( Block signBlock, String tag, String altTag, int defaultValue ) {
      Sign sign = ( Sign ) signBlock.getState();

      // Check main two users.

      String line;
      int x, y, end, index;

      for (y = 2; y <= 3; ++y)
         if (!sign.getLine(y).isEmpty()) {
            line = sign.getLine(y).replaceAll("(?i)\u00A7[0-F]", "");
            //if(line.isEmpty()) continue;

            end = line.length() - 1;

            if (end >= 2)
               if ((line.charAt(0) == '[') && (line.charAt(end) == ']')) {
                  index = line.indexOf(":");

                  if (index == -1) {
                     // No number.
                     if (line.substring(1, end).equalsIgnoreCase(tag) || line.substring(1, end).equalsIgnoreCase(altTag)) {
                        return (defaultValue);
                     }
                  }
                  else {
                     // Number.
                     if (line.substring(1, index).equalsIgnoreCase(tag) || line.substring(1, index).equalsIgnoreCase(altTag)) {
                        // Trim junk around the number.

                        for (x = index; x < end; ++x) {
                           if (Character.isDigit(line.charAt(x))) {
                              index = x;
                              break;
                           }
                        }
                        for (x = index + 1; x < end; ++x) {
                           if (!Character.isDigit(line.charAt(x))) {
                              end = x;
                              break;
                           }
                        }

                        // Try to parse the number, and return the result.
                        try {
                           int value = Integer.parseInt(line.substring(index, end));
                           return (value);
                        }
                        catch (NumberFormatException ex) {
                           return (defaultValue);
                        }
                     }
                  }
               }
         }

      return (defaultValue);
   }
}