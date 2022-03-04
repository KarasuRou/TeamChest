package karasurou.teamchest;

import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
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
            event.setCancelled(true);
        }
//        else {
//            sendMessage(p, "Something else");
//        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChestClose(InventoryMoveItemEvent event) {
        Player p = TeamChest.getPlugin().getServer().getPlayer("KarasuRou");
        if (event.getSource().getHolder() instanceof Chest) {
            sendMessage(p,"CHEST");
        }
        if (event.getSource().getHolder() instanceof DoubleChest) {
            sendMessage(p, "DOUBLE_CHEST");
        }
        sendMessage(p, "" + event.getSource().getHolder());
        event.setCancelled(true);
    }

    private void sendMessage(Player player, String s) {
        player.sendMessage(s);
    }
}
