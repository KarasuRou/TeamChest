package karasurou.teamchest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> commands = new ArrayList<>();
        if (sender.hasPermission("teamchest.debug")) {
            commands.add("debug");
        }
        if (sender.hasPermission("teamchest.command")) {
            commands.add("createTeam");
            commands.add("deleteTeam");
            commands.add("inviteToTeam");
            commands.add("acceptTeamInvitation");
            commands.add("denyTeamInvitation");
            commands.add("leaveTeam");
            commands.add("kickFromTeam");
            commands.add("version");
        }
        if (args != null && args.length == 1) {
            List<String> list = new ArrayList<>();
            for (String s : commands) {
                if (s.startsWith(args[0])) {
                    list.add(s);
                }
            }
            return list;
        }
        return commands;
    }

    public static Plugin getPlugin(){
        return plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return super.onCommand(sender, command, label, args);
    }
}
