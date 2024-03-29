package karasurou.teamchest;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private static Plugin plugin;
    private static FileConfiguration configuration;
    private final static String configurationFile = "config.yml";
    private static YamlConfiguration language;
    private static List<String> teamSignLine = new ArrayList<>();

    private Config() {}
    
    public static void loadConfig(Plugin plugin) {
        Config.plugin = plugin;

        initDefaultConfiguration();
        initLanguageFiles();

        configuration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), configurationFile));
        String languageFile = configuration.getString("language-file-" + System.getProperty("user.language"), "language.yml");
        language = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), languageFile));

        teamSignLine = configuration.getStringList("team-sign-line");
    }

    private static void initDefaultConfiguration() {
        plugin.saveDefaultConfig();
        configuration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), configurationFile));
        configuration.addDefault("language-file", "language.yml");
        configuration.addDefault("language-file-de", "language_de.yml");
        configuration.addDefault("team-sign-line", new String[]{"[Team-Sign]", "[team-sign]", "[Team Sign]", "[team sign]", "[Team_Sign]", "[team_sign]"});
        configuration.addDefault("sing-items", new String[]{"SPRUCE_WALL_SIGN", "DARK_OAK_WALL_SIGN", "ACACIA_WALL_SIGN", "BIRCH_WALL_SIGN", "OAK_WALL_SIGN", "JUNGLE_WALL_SIGN", "WARPED_WALL_SIGN", "CRIMSON_WALL_SIGN"});
        configuration.addDefault("team-items", new String[]{"CHEST", "TRAPPED_CHEST"});
        try {
            configuration.save(new File(plugin.getDataFolder(), configurationFile));
        } catch (IOException e) {
            outputError(e);
        }
    }

    private static void initLanguageFiles() {
        String[] files = {"language.yml", "language_de.yml"};
        for (String filename : files){
            File file = new File(plugin.getDataFolder(), filename);
            if (file.exists()) {
                if (file.delete()) {
                    plugin.saveResource(filename, false);
                }
            } else {
                plugin.saveResource(filename, false);
            }
        }
    }

    public static String getLanguage(String path) {
        return ChatColor.translateAlternateColorCodes('&', language.getString(path, "&b[TeamChest]&4 [ERROR]: " + path));
    }

    public static boolean isSignLine(String line) {
        return teamSignLine.contains(line);
    }

    private static void outputError(Exception exception) {
        plugin.getLogger().severe(exception.getMessage());
        exception.printStackTrace();
    }
}
