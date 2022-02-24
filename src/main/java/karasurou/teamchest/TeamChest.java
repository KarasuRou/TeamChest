package karasurou.teamchest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * <h3>MINECRAFT SERVER PLUGIN</h3>
 * This plugin will ...<br/>
 * <br/>
 * Permissions if needed:<br/>
 * teamchest.com - des ({@code command})<br/>
 * @author Rouven Tjalf Rosploch (KarasuRou)
 * @version 1.0.0
 */
public class TeamChest extends JavaPlugin {

    @Override
    public void onEnable() {
        Config.loadConfig();
    }

    @Override
    public void onDisable() {
        Config.unloadConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return super.onCommand(sender, command, label, args);
    }
}
