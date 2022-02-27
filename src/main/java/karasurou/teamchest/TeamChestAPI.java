package karasurou.teamchest;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TeamChestAPI {

    private TeamChestAPI(){}// TODO: 25.02.2022

    public static boolean isSign(Block block){
        switch (block.getType()) {
            case SPRUCE_WALL_SIGN:
            case DARK_OAK_WALL_SIGN:
            case ACACIA_WALL_SIGN:
            case BIRCH_WALL_SIGN:
            case OAK_WALL_SIGN:
            case JUNGLE_WALL_SIGN:
            case WARPED_WALL_SIGN:
            case CRIMSON_WALL_SIGN:
                return true;
            default:
                return false;
        }
    }

    public static boolean isChest(Block block){
        switch (block.getType()) {
            case CHEST:
            case TRAPPED_CHEST:
                return true;
            default:
                return false;
        }
    }

    public static boolean createNewTeam(String teamName, Player owner) {
        return false;
    }

    public static boolean addPlayerToTeam(String teamName, Player player) {
        return false;
    }

    public static boolean removePlayerFromTeam(String teamName, Player player) {
        return false;
    }

    public static boolean deleteTeam(String teamName, Player player) {
        return false;
    }
}
