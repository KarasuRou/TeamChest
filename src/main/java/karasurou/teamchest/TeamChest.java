package karasurou.teamchest;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
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
        if (command.getName().equals("teamchest")) {
            if (args.length == 0) {
                sender.sendMessage(Config.getLanguage("commando-usage"));
            } else {
                // These commands do not require a player
                if (args[0].equalsIgnoreCase("debug")) {
                    if (!sender.hasPermission("teamchest.debug")) {
                        sender.sendMessage(Config.getLanguage("no-permission"));
                    } else {
                        sender.sendMessage("TeamChest Debug");
                        sender.sendMessage("TeamChest: v" + getDescription().getVersion());
                        sender.sendMessage("Plugin for Minecraft version: " + getDescription().getAPIVersion());
                        sender.sendMessage("Bukkit: v" + Bukkit.getServer().getClass().getPackage().getName().split("v")[1]);
                        sender.sendMessage("Server version: " + Bukkit.getVersion());
                        sender.sendMessage("Detected language: " + System.getProperty("user.language"));
                    }
                    return true;
                } else if (sender.hasPermission("teamchest.command")) {
                    boolean success = false;
                    // These commands require a player
                    if (sender instanceof ConsoleCommandSender) {
                        sender.sendMessage(Config.getLanguage("no-player"));
                        return true;
                    }
                    if (args.length == 2) {
                        switch (args[0]) {
                            case "createTeam":
                                success = TeamChestAPI.createNewTeam(args[1], (Player) sender);
                                break;
                            case "deleteTeam":
                                success = TeamChestAPI.deleteTeam(args[1], (Player) sender);
                                break;
                            case "acceptTeamInvitation":
                                success = TeamChestAPI.acceptInvitation(args[1], (Player) sender);
                                break;
                            case "denyTeamInvitation":
                                success = TeamChestAPI.denyInvitation(args[1], (Player) sender);
                                break;
                            case "leaveTeam":
                                success = TeamChestAPI.leaveTeam(args[1], (Player) sender);
                                break;
                            default:
                                sender.sendMessage(Config.getLanguage("commando-usage"));
                                break;
                        }
                    } else if (args.length == 3) {
                        switch (args[0]) {
                            case "inviteToTeam":
                                success = TeamChestAPI.inviteToTeam(args[1], args[2], (Player) sender);
                                break;
                            case "kickFromTeam":
                                success = TeamChestAPI.kickFromTeam(args[1], args[2], (Player) sender);
                                break;
                            default:
                                sender.sendMessage(Config.getLanguage("commando-usage"));
                                break;
                        }
                    } else {
                        sender.sendMessage(Config.getLanguage("commando-usage"));
                    }
                    if (!success) {
                        // If command was not successful
                        sender.sendMessage(Config.getLanguage("no-success-error"));
                    }
                } else {
                    sender.sendMessage(Config.getLanguage("no-permission"));
                }
            }
        }
        return true;
    }
}
