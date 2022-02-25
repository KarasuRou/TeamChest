package karasurou.teamchest;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class Config {

    private static Plugin plugin;
    private static FileConfiguration configuration;
    private final static String configurationFile = "config.yml";
    private static YamlConfiguration language;
    private static String languageFile = "";

    private Config() {}
    
    public static void loadConfig(Plugin plugin) {// TODO: 25.02.2022  
        Config.plugin = plugin;
        configuration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), configurationFile));
        languageFile = configuration.getString("language-file-"+System.getProperty("user.language"),"language.yml");
        language = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), configurationFile));
    }

    public static void unloadConfig() {// TODO: 25.02.2022  
    }

}
