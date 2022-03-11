package karasurou.teamchest;

import org.bukkit.Location;
import org.bukkit.block.Block;
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
//        Player p = event.getPlayer();
//        String message = "Lines: " + Arrays.toString(event.getLines())  + "\n" +
//                         "Block: " + event.getBlock() + "\n";
//        event.getBlock().getLocation(); // TODO: 10.03.2022 Location of Sign
//        sendMessage(p, message); // TODO: 10.03.2022 Check sign permission
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProtectedBlockRemove(BlockBreakEvent event) {
        if (TeamChestAPI.isProtectedChest(event.getBlock())) {
            event.setCancelled(true);
        } else if (TeamChestAPI.isProtectionSign(event.getBlock())) {
            event.setCancelled(true);
//            sendMessage(event.getPlayer(), Arrays.toString(((Sign) event.getBlock().getState()).getLines())); // TODO: 10.03.2022 Check sign permission
//            Player player = event.getPlayer();
//            if () // TODO: 10.03.2022 Check if user is allowed to remove the sign
//            sendMessage(player, Config.getLanguage("no-teamowner"));
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

    private void sendMessage(Player player, String s) {
        player.sendMessage(s);
    }
}
