package karasurou.teamchest;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class ChestListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignWritten(SignChangeEvent event) {
        String[] lines = event.getLines();
        Player player = event.getPlayer();
        if (Config.isSignLine(lines[0])) {
            String teamName = lines[1];
            Block sign = event.getBlock();
            if (!TeamChestAPI.teamDontExists(teamName)) {
                if (TeamChestAPI.isOwner(teamName, player.getName())) {
                    Block chest = searchChestAtSign(sign);
                    if (chest == null) {
                        return;
                    }
                    TeamChestAPI.setSignAndChestForTeam(teamName, sign, chest);
                    sendMessage(player, Config.getLanguage("chest-created")
                            .replace("[TEAM]", teamName));
                } else {
                    sendMessage(player, Config.getLanguage("no-teamowner"));
                    for (int i = 0; i <= 3; i++) {
                        event.setLine(i, Config.getLanguage("sign-error"));
                    }
                }
            } else {
                sendMessage(player, Config.getLanguage("no-team"));
                for (int i = 0; i <= 3; i++) {
                    event.setLine(i, Config.getLanguage("sign-error"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProtectedBlockRemove(BlockBreakEvent event) {
        if (TeamChestAPI.isProtectedChest(event.getBlock())) {
            event.setCancelled(true);
        } else if (TeamChestAPI.isProtectionSign(event.getBlock())) {
            String[] lines = ((Sign) event.getBlock().getState()).getLines();
            Player player = event.getPlayer();
            if (Config.isSignLine(lines[0])) {
                String teamName = lines[1];
                if (TeamChestAPI.getAllTeamsAndMembers().get(teamName)[0].equals(player.getName())) {
                    TeamChestAPI.removeChestSignETC(teamName);
                    sendMessage(player, Config.getLanguage("chest-removed")
                            .replace("[TEAM]", teamName));
                } else {
                    sendMessage(player, Config.getLanguage("no-teamowner"));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProtectedBlockBurn(BlockBurnEvent event) {
        if (TeamChestAPI.isProtectedChest(event.getBlock()) || TeamChestAPI.isProtectionSign(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTNTExplosion(EntityExplodeEvent event){
        event.blockList().removeIf(block -> TeamChestAPI.isProtectedChest(block) || TeamChestAPI.isProtectionSign(block));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplosion(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> TeamChestAPI.isProtectedChest(block) || TeamChestAPI.isProtectionSign(block));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChestOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType().getDefaultTitle().equalsIgnoreCase("chest")) {
            Location location = event.getInventory().getLocation();
            if (location == null)
                return;

            Block block = location.getBlock();
            Player player = (Player) event.getPlayer();
            if (TeamChestAPI.isProtectedChest(block)) {
                if (!TeamChestAPI.allowedToOpenChest(block, player)) {
                    event.setCancelled(true);
                    sendMessage(player, Config.getLanguage("no-chest-opening-permission"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHopperTrysToMoveThings(InventoryMoveItemEvent event) {
        if (TeamChestAPI.isChest(event.getSource().getHolder())) {
            Location location = event.getSource().getLocation();
            if (location == null)
                return;

            Block block = location.getBlock();
            if (TeamChestAPI.isProtectedChest(block)) {
                event.setCancelled(true);
            }
        }
    }

    private Block searchChestAtSign(Block sign) {
        Block[] blocks = getBlocksAroundIt(sign);
        for (Block block : blocks) {
            if (TeamChestAPI.isChest(block)) {
                return block;
            }
        }
        return null;
    }

    private Block[] getBlocksAroundIt(Block block) {
        Block[] blocks = new Block[4];
        blocks[0] = block.getRelative(BlockFace.NORTH);
        blocks[1] = block.getRelative(BlockFace.EAST);
        blocks[2] = block.getRelative(BlockFace.SOUTH);
        blocks[3] = block.getRelative(BlockFace.WEST);
        return blocks;
    }

    private void sendMessage(Player player, String s) {
        player.sendMessage(s);
    }
}
