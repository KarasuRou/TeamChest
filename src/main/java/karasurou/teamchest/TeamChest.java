package karasurou.teamchest;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * <h3>MINECRAFT SERVER PLUGIN</h3>
 * This plugin will add a new feature where a team-lead can designate a chest and every team-member can use it.<br/>
 * <br/>
 * <em><u>Commands (Plugins):</u></em><br/>
 * teamchest.debug:<br/>
 * - debug<br/>
 * - getAllTeams<br/>
 * teamchest.command:<br/>
 * - createTeam [teamName]<br/>
 * - openChest [teamName]<br/>
 * - deleteTeam [teamName]<br/>
 * - getTeams<br/>
 * - inviteToTeam [teamName] [player]<br/>
 * - getTeamInvitations [teamName]<br/>
 * - cancelTeamInvitation [teamName] [player]<br/>
 * - acceptTeamInvitation [teamName]<br/>
 * - denyTeamInvitation [teamName]<br/>
 * - leaveTeam [teamName]<br/>
 * - kickFromTeam [teamName] [player]<br/>
 * <br/>
 * <em><u>Permissions if needed:</u></em><br/>
 * teamchest.debug - Admin commands<br/>
 * teamchest.command - Team creation and chest usage<br/>
 * @author Rouven Tjalf Rosploch (KarasuRou)
 * @version 1.0.0
 */
public class TeamChest extends JavaPlugin {

    private static Plugin plugin;
    private final static List<String> debugCommands = new ArrayList<>();
    private final static List<String> commands = new ArrayList<>();

    @Override
    public void onEnable() {
        fillAllCommands();
        plugin = this;
        Config.loadConfig(this);
        TeamChestAPI.init(this);
        getServer().getPluginManager().registerEvents(new ChestListener(), this);

        if (errorDetected()) {
            plugin.getLogger().severe(Config.getLanguage("startup-error"));
            plugin.getPluginLoader().disablePlugin(this);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> senderCommands = getSenderCommands(sender);
        if (args != null) {
            if (args.length == 1) {
                List<String> list = new ArrayList<>();
                for (String s : senderCommands) {
                    if (s.toLowerCase().contains(args[0].toLowerCase())) {
                        list.add(s);
                    }
                }
                return list;
            } else {
                return new ArrayList<>();// TODO: 04.03.2022 Load Team-list for player (recommendation)?
            }
        }
        return senderCommands;
    }

    public static Plugin getPlugin(){
        return plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("teamchest")) {
            if (args.length != 0) {
                // These commands do not require a player
                if (debugCommands.contains(args[0])) {
                    if (!sender.hasPermission("teamchest.debug")) {
                        sender.sendMessage(Config.getLanguage("no-permission"));
                        return true;
                    }
                    switch (args[0]) {
                        case "debug":
                            sender.sendMessage("TeamChest Debug");
                            sender.sendMessage("TeamChest: v" + getDescription().getVersion());
                            sender.sendMessage("Plugin for Minecraft version: " + getDescription().getAPIVersion());
                            sender.sendMessage("Bukkit: v" + Bukkit.getServer().getClass().getPackage().getName().split("v")[1]);
                            sender.sendMessage("Server version: " + Bukkit.getVersion());
                            sender.sendMessage("Detected language: " + System.getProperty("user.language"));
                            break;
                        case "getAllTeams":
                            HashMap<String, String[]> teamPlayerMap = TeamChestAPI.getAllTeamsAndMembers();
                            String[] teams = teamPlayerMap.keySet().toArray(new String[0]);
                            for (String team : teams) {
                                String[] player = teamPlayerMap.get(team);
                                StringBuilder output = new StringBuilder(Config.getLanguage("team_1")
                                        .replace("[TEAM]", team)
                                        .replace("[OWNER]", player[0]));
                                if (player.length == 1) {
                                    output.append(Config.getLanguage("team_2_nomembers"));
                                } else {
                                    output.append(Config.getLanguage("team_2_members"));
                                    for (int i = 1; i < player.length; i++) {
                                        output.append(player[i]);
                                        if (i != player.length - 1) {
                                            output.append(", ");
                                        }
                                    }
                                }
                                sender.sendMessage(output.toString());
                            }
                            break;
                        default:
                            break;

                    }
                    return true;
                } else if (commands.contains(args[0])) {
                    if (!sender.hasPermission("teamchest.command")) {
                        sender.sendMessage(Config.getLanguage("no-permission"));
                        return true;
                    }
                    boolean success;
                    // These commands require a player
                    if (sender instanceof ConsoleCommandSender) {
                        sender.sendMessage(Config.getLanguage("no-player"));
                        return true;
                    }
                    if(args.length == 1){
                        switch (args[0]) {
                            case "getTeams":
                                success = TeamChestAPI.getPlayerTeams((Player) sender);
                                break;
                            default:
                                Utilities.sendCommandHelp(args[0], sender, this);
                                success = true;
                                break;
                        }
                    } else if (args.length == 2) {
                        if (TeamChestAPI.teamDontExists(args[1]) && !args[0].equals("createTeam")) {
                            sender.sendMessage(Config.getLanguage("no-team"));
                            return true;
                        }
                        switch (args[0]) {
                            case "createTeam":
                                if (!TeamChestAPI.teamDontExists(args[1])) {
                                    sender.sendMessage(Config.getLanguage("team-exists"));
                                    success = true;
                                } else {
                                    success = TeamChestAPI.createNewTeam(args[1], (Player) sender);
                                }
                                break;
                            case "deleteTeam":
                                success = TeamChestAPI.deleteTeam(args[1], (Player) sender);
                                break;
                            case "getTeamInvitations":
                                success = TeamChestAPI.getTeamInvitations(args[1], (Player) sender);
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
                            case "openChest":
                                success = TeamChestAPI.openChest(args[1], (Player) sender);
                                break;
                            default:
                                Utilities.sendCommandHelp(args[0], sender, this);
                                success = true;
                                break;
                        }
                    } else if (args.length == 3) {
                        if (TeamChestAPI.teamDontExists(args[1])) {
                            sender.sendMessage(Config.getLanguage("no-team"));
                            return true;
                        }
                        switch (args[0]) {
                            case "inviteToTeam":
                                success = TeamChestAPI.inviteToTeam(args[1], args[2], (Player) sender);
                                break;
                            case "cancelTeamInvitation":
                                success = TeamChestAPI.cancelTeamInvitation(args[1], args[2], (Player) sender);
                                break;
                            case "kickFromTeam":
                                success = TeamChestAPI.kickFromTeam(args[1], args[2], (Player) sender);
                                break;
                            default:
                                Utilities.sendCommandHelp(args[0], sender, this);
                                success = true;
                                break;
                        }
                    } else {
                        Utilities.sendCommandHelp(args[0], sender, this);
                        success = true;
                    }
                    if (!success) {
                        // If command was not successful
                        sender.sendMessage(Config.getLanguage("no-success-error"));
                        return true;
                    }
                } else {
                    // If no command was detected, print all
                    Utilities.sendAllCommandHelp(sender, this);
                    return true;
                }
            } else {
                // If no command was detected, print all
                Utilities.sendAllCommandHelp(sender, this);
                return true;
            }
        }
        return true;
    }

    private void fillAllCommands() {
        debugCommands.add("debug");
        debugCommands.add("getAllTeams");
        commands.add("createTeam");
        commands.add("openChest");
        commands.add("deleteTeam");
        commands.add("getTeams");
        commands.add("inviteToTeam");
        commands.add("getTeamInvitations");
        commands.add("cancelTeamInvitation");
        commands.add("acceptTeamInvitation");
        commands.add("denyTeamInvitation");
        commands.add("leaveTeam");
        commands.add("kickFromTeam");
    }

    public String[] getCommands(CommandSender sender) {
        return getSenderCommands(sender).toArray(new String[0]);
    }

    private List<String> getSenderCommands(CommandSender sender) {
        List<String> senderCommands = new ArrayList<>();
        if (sender.hasPermission("teamchest.debug")) {
            senderCommands.addAll(debugCommands.subList(0, debugCommands.size()));
        }
        if (sender.hasPermission("teamchest.command")) {
            senderCommands.addAll(commands.subList(0, commands.size()));
        }
        return senderCommands;
    }

    private boolean errorDetected() {
        return !new File(plugin.getDataFolder(), "teams.json").exists();
    }
}
