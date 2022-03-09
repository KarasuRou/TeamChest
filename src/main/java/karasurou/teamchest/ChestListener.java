package karasurou.teamchest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ChestListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerOpenChest(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getClickedBlock() != null && TeamChestAPI.isChest(event.getClickedBlock()) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            sendMessage(p, "You opened a chest!");
//            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHopperThingsTryToMove(InventoryMoveItemEvent event) { //If chest protected -> NO! JUST NO! NO HOPPER!
        Player p = TeamChest.getPlugin().getServer().getPlayer("KarasuRou");
        if (TeamChestAPI.isChest(event.getSource().getHolder())) {
//            sendMessage(p, "Chest:" + event.getSource().getHolder());
        } else {
//            sendMessage(p, String.valueOf(event.getSource().getHolder()));
        }
//        event.setCancelled(true);
    }

    private void sendMessage(Player player, String s) {
        player.sendMessage(s);
    }
}
