package karasurou.teamchest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

// TODO: 25.02.2022  
/**
 * <h3>MINECRAFT SERVER PLUGIN</h3>
 * This plugin will add a new feature ...<br/>
 * <br/>
 * Permissions if needed:<br/>
 * teamchest.com - des ({@code command})<br/>
 * @author Rouven Tjalf Rosploch (KarasuRou)
 * @version 1.0.0
 */
public class TeamChest extends JavaPlugin {

    private static Plugin plugin;

    @Override
    public void onEnable() {// TODO: 25.02.2022
        plugin = this;
        Config.loadConfig(this);
    }

    public static Plugin getPlugin(){
        return plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return super.onCommand(sender, command, label, args);
    }
}
