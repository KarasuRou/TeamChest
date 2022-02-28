package karasurou.teamchest;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utilities {

    private Utilities(){}

    public static void sendCommandHelp(String command, CommandSender sender, TeamChest plugin) {
        String[] commands = plugin.getCommands(sender);
        if (Arrays.asList(commands).contains(command)) {
            sender.sendMessage(Config.getLanguage("invalid-command"));
            sender.sendMessage(Config.getLanguage("command-help"));
            sender.sendMessage(Config.getLanguage("command_" + command.toLowerCase()));
        } else {
            List<String> list = new ArrayList<>();
            for (String s : commands) {
                if (s.startsWith(command)) {
                    list.add(s);
                }
            }
            if (list.size() == 0) {
                sender.sendMessage(Config.getLanguage("no-command-found"));
                sendAllCommandHelp(sender, plugin);
            } else {
                String begin = "- ";
                if (list.size() == 1) {
                    sender.sendMessage(Config.getLanguage("invalid-command"));
                    begin = "";
                } else {
                    sender.sendMessage(Config.getLanguage("no-commands-detected"));
                }
                for (String com : list) {
                    if (!com.equalsIgnoreCase("debug")) {
                        sender.sendMessage(begin + Config.getLanguage("command_" + com.toLowerCase()));
                    }
                }
            }
        }

    }
    public static void sendAllCommandHelp(CommandSender sender, TeamChest plugin) {
        sender.sendMessage(Config.getLanguage("plugin-help"));
        String[] commands = plugin.getCommands(sender);
        String begin = "- ";
        if (commands.length <= 1)
            begin = "";
        for (String command : commands) {
            if (!command.equalsIgnoreCase("debug")) {
                sender.sendMessage(begin + Config.getLanguage("command_" + command.toLowerCase()));
            }
        }
    }
}
