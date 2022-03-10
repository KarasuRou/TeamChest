package karasurou.teamchest;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.json.*;

import javax.annotation.Nullable;
import java.io.*;
import java.util.HashMap;

public class TeamChestAPI {

    private static Plugin plugin;

    private TeamChestAPI(){}

    public static void init(Plugin plugin) {
        TeamChestAPI.plugin = plugin;
        EditTeamFile.init();
    }

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

    public static boolean isProtectionSign(Block block) {
        if (isSign(block)) {
            return true; // TODO: 10.03.2022 CheckProtection
        }
        return false;
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

    public static boolean isChest(InventoryHolder inventoryHolder){
        if (inventoryHolder == null)
            return false;

        return inventoryHolder instanceof Chest || inventoryHolder instanceof DoubleChest;
    }

    public static boolean isProtectedChest(Block block) {
        if (isChest(block)) {
            return true; // TODO: 10.03.2022 CheckProtection
        }
        return false;
    }

    public static HashMap<String, String[]> getAllTeamsAndMembers() {
        try {
            HashMap<String, String[]> teamAndMembers = new HashMap<>();
            String[] teams = getTeams();
            for (String team:teams) {
                teamAndMembers.put(team, getMembersFromTeam(team));
            }
            return teamAndMembers;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return null;
    }

    public static boolean teamDontExists(String teamName) {
        try {
            for (String team : getTeams()) {
                if (team.equals(teamName)) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean createNewTeam(String teamName, Player owner) {
        try {
            addTeam(teamName, owner.getName());
            owner.sendMessage(Config.getLanguage("team_created").replace("[TEAM]", teamName));
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static boolean deleteTeam(String teamName, Player player) {
        try {
            if (getMembersFromTeam(teamName)[0].equals(player.getName())) {
                removeTeam(teamName);
                player.sendMessage(Config.getLanguage("team_deleted").replace("[TEAM]", teamName));
                return true;
            } else {
                player.sendMessage(Config.getLanguage("no-teamowner"));
            }
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static boolean acceptInvitation(String teamName, Player player) {
        try {
            removePlayerInvitation(teamName, player.getName());
            addPlayerToTeam(teamName, player.getName());
            player.sendMessage(Config.getLanguage("team_acceptinvite").replace("[TEAM]", teamName));
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static boolean denyInvitation(String teamName, Player player) {
        try {
            removePlayerInvitation(teamName, player.getName());
            player.sendMessage(Config.getLanguage("team_denyinvite").replace("[TEAM]", teamName));
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static boolean leaveTeam(String teamName, Player player) {
        try {
            removePlayerFromTeam(teamName, player.getName());
            player.sendMessage(Config.getLanguage("team_leftteam").replace("[TEAM]", teamName));
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static boolean inviteToTeam(String teamName, String player, Player sendPlayer) {
        try {
            if (getMembersFromTeam(teamName)[0].equals(sendPlayer.getName())) {
                addPlayerInvitation(teamName, player);
                sendPlayer.sendMessage(
                        Config.getLanguage("team_invite")
                                .replace("[TEAM]", teamName)
                                .replace("[PLAYER]", player)
                );
                return true;
            } else {
                sendPlayer.sendMessage(Config.getLanguage("no-teamowner"));
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static boolean kickFromTeam(String teamName, String player, Player sendPlayer) {
        try {
            if (getMembersFromTeam(teamName)[0].equals(sendPlayer.getName())) {
                removePlayerFromTeam(teamName, player);
                sendPlayer.sendMessage(
                        Config.getLanguage("team_kicked")
                                .replace("[TEAM]", teamName)
                                .replace("[PLAYER]", player)
                );
                return true;
            } else {
                sendPlayer.sendMessage(Config.getLanguage("no-teamowner"));
            }
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    private static String[] getTeams() throws IOException {
        return EditTeamFile.getTeams();
    }

    private static String[] getMembersFromTeam(String teamName) {
        return EditTeamFile.getMembersFromTeam(teamName);
    }

    private static void addTeam(String teamName, String owner) throws IOException {
        EditTeamFile.addTeam(teamName,owner);
    }

    private static void removeTeam(String teamName) throws IOException {
        EditTeamFile.removeTeam(teamName);
    }

    private static void addPlayerToTeam(String teamName, String member) throws IOException {
        String[] beforeMember = EditTeamFile.getMembersFromTeam(teamName);
        String[] afterMember = new String[beforeMember.length + 1];
        System.arraycopy(beforeMember, 0, afterMember, 0, beforeMember.length);
        afterMember[beforeMember.length] = member;
        EditTeamFile.setMembersFromTeam(teamName, afterMember);
    }

    private static void removePlayerFromTeam(String teamName, String player) throws IOException {
        String[] beforeMember = EditTeamFile.getMembersFromTeam(teamName);
        String[] afterMember = new String[beforeMember.length - 1];
        int i = 0;
        for (String member : beforeMember) {
            if (!member.equals(player)) {
                afterMember[i] = member;
                i++;
            }
        }
        EditTeamFile.setMembersFromTeam(teamName, afterMember);
    }

    private static void addPlayerInvitation(String teamName, String player) throws IOException {
        String[] beforeMember = EditTeamFile.getInvitationsForTeam(teamName);
        String[] afterMember;
        if (beforeMember != null) {
            afterMember = new String[beforeMember.length + 1];
            System.arraycopy(beforeMember, 0, afterMember, 0, beforeMember.length);
            afterMember[beforeMember.length] = player;
        } else {
            afterMember = new String[]{player};
        }
        EditTeamFile.setInvitationsForTeam(teamName, afterMember);
    }

    private static void removePlayerInvitation(String teamName, String player) throws IOException {
        String[] beforeMember = EditTeamFile.getInvitationsForTeam(teamName);
        String[] afterMember = new String[beforeMember.length - 1];
        int i = 0;
        for (String member : beforeMember) {
            if (!member.equals(player)) {
                afterMember[i] = member;
                i++;
            }
        }
        EditTeamFile.setInvitationsForTeam(teamName, afterMember);
    }

    public static boolean allowedToOpenChest(Block block, Player player) {
        return true; // TODO: 10.03.2022 Check if user is allowed to open the chest
    }

    private static class EditTeamFile{

        private final static File file = new File(plugin.getDataFolder(),"teams.json");
        private final static HashMap<String, String[]> teamAndMemberCombination = new HashMap<>();
        private final static HashMap<String, String[]> teamAndInvitationCombination = new HashMap<>();
        private final static HashMap<String, String> teamAndWorldCombination = new HashMap<>();
        private final static HashMap<String, Location> teamAndChestCombination = new HashMap<>();
        private final static HashMap<String, Location> teamAndSignCombination = new HashMap<>();

        public static void init() {
                try {
                    if (!file.exists()) {
                        if (file.createNewFile()) {
                            plugin.getLogger().info(Config.getLanguage("teamfilecreated"));
                        } else {
                            plugin.getLogger().severe(Config.getLanguage("teamfilecreated-error"));
                        }
                    }
                } catch (IOException e) {
                    plugin.getLogger().severe(e.getMessage());
                }
        }

        private EditTeamFile(){}

        private static String[] getTeams() {
            new EditTeamFile().loadTeams();
            return teamAndMemberCombination.keySet().toArray(new String[0]);
        }

        private static void addTeam(String teamName, String owner) throws IOException {
            teamAndMemberCombination.put(teamName, new String[]{owner});
            new EditTeamFile().writeTeamFile();
        }

        private static void removeTeam(String team) throws IOException {
            teamAndMemberCombination.remove(team);
            teamAndInvitationCombination.remove(team);
            new EditTeamFile().writeTeamFile();
        }

        private static String[] getMembersFromTeam(String teamName) {
            new EditTeamFile().loadTeams();
            return teamAndMemberCombination.get(teamName);
        }

        private static void setMembersFromTeam(String team, String[] members) throws IOException {
            teamAndMemberCombination.replace(team, members);
            new EditTeamFile().writeTeamFile();
        }

        private static String[] getInvitationsForTeam(String teamName) {
            new EditTeamFile().loadTeams();
            return teamAndInvitationCombination.get(teamName);
        }

        private static void setInvitationsForTeam(String team, @Nullable String[] members) throws IOException {
            if (members == null) {
                teamAndInvitationCombination.remove(team);
            } else {
                if (teamAndInvitationCombination.containsKey(team)) {
                    teamAndInvitationCombination.replace(team, members);
                } else {
                    teamAndInvitationCombination.put(team, members);
                }
            }
            new EditTeamFile().writeTeamFile();
        }

        private static void setWorld(String team, String world) throws NullPointerException{
            if (world == null) {
                teamAndWorldCombination.remove(team);
            } else {
                if (teamAndWorldCombination.containsKey(team)) {
                    teamAndWorldCombination.replace(team, world);
                } else {
                    teamAndWorldCombination.put(team, world);
                }
            }
        }

        private static String getWorld(String team) { // TODO: 10.03.2022 Do I REALLY need this?
            new EditTeamFile().loadTeams();
            return teamAndWorldCombination.get(team);
        }

        private static void setChestLocation(String team, Location location) throws NullPointerException{
            if (location == null) {
                teamAndInvitationCombination.remove(team);
            } else {
                if (teamAndChestCombination.containsKey(team)) {
                    teamAndChestCombination.replace(team, location);
                } else {
                    teamAndChestCombination.put(team, location);
                }
            }
        }

        private static Location getChestLocation(String team) {
            new EditTeamFile().loadTeams();
            return teamAndChestCombination.get(team);
        }

        private static void setSignLocation(String team, Location location) throws NullPointerException{
            if (location == null) {
                teamAndSignCombination.remove(team);
            } else {
                if (teamAndSignCombination.containsKey(team)) {
                    teamAndSignCombination.replace(team, location);
                } else {
                    teamAndSignCombination.put(team, location);
                }
            }
        }

        private static Location getSignLocation(String team) {
            new EditTeamFile().loadTeams();
            return teamAndSignCombination.get(team);
        }

        private void loadTeams() {
            JSONArray allTeams;
            try {
                JSONObject teams = new JSONObject(new EditTeamFile().readFile());
                allTeams = (JSONArray) teams.get("teams");

                teamAndMemberCombination.clear();
                teamAndInvitationCombination.clear();
                teamAndChestCombination.clear();
                teamAndSignCombination.clear();
            } catch (Exception ignored) {
                allTeams = new JSONArray();
            }
            for (int i = 0; i < allTeams.length(); i++) {
                JSONObject jsonObject = allTeams.getJSONObject(i);
                String teamName = (String) jsonObject.get("Name");
                String[] members = ((JSONArray)jsonObject.get("Member")).toList().toArray(new String[0]);

                if (!jsonObject.isNull("Invitation")) {
                    String[] invitation = ((JSONArray)jsonObject.get("Invitation")).toList().toArray(new String[0]);

                    teamAndInvitationCombination.put(teamName, invitation);
                }

                if (!jsonObject.isNull("World")) {
                    String worldName = jsonObject.getString("World");
                    int[] chestInts = ArrayUtils.toPrimitive(jsonObject.getJSONArray("ChestLocation").toList().toArray(new Integer[0]));
                    int[] signInts = ArrayUtils.toPrimitive(jsonObject.getJSONArray("SignLocation").toList().toArray(new Integer[0]));

                    teamAndWorldCombination.put(teamName, worldName);
                    teamAndChestCombination.put(teamName, getIntsLocation(worldName, chestInts));
                    teamAndSignCombination.put(teamName, getIntsLocation(worldName, signInts));
                }
                teamAndMemberCombination.put(teamName, members);
            }
        }

        private String readFile() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuilder raw = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                raw.append(line).append("\n");
            }
            return raw.toString();
        }

        private void writeTeamFile() throws IOException {
            String[] teams = teamAndMemberCombination.keySet().toArray(new String[0]);
            JSONArray teamArray = new JSONArray();
            for (String team : teams) {

                String[] member = teamAndMemberCombination.get(team);
                String[] memberInvitation = teamAndInvitationCombination.get(team);
                JSONObject object = new JSONObject();
                object.put("Name", team);
                object.put("Member", member);
                if (memberInvitation != null) {
                    object.put("Invitation", memberInvitation);
                }
                if (teamAndWorldCombination.get(team) != null) {
                    object.put("World", teamAndWorldCombination.get(team));
                    object.put("ChestLocation", getLocationInts(teamAndChestCombination.get(team)));
                    object.put("SignLocation", getLocationInts(teamAndChestCombination.get(team)));
                }
                teamArray.put(object);
            }
            JSONObject allTeamsObject = new JSONObject();
            allTeamsObject.put("teams", teamArray);
            Writer writer = new FileWriter(file);
            writer.write(allTeamsObject.toString());
            writer.close();
        }

        private int[] getLocationInts(Location location) {
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            return new int[]{x, y, z};
        }

        private Location getIntsLocation(String worldName, int[] coordination) {
            // coordination array = 0:x , 1:y , 2:z
            return plugin.getServer().getWorld(worldName).getBlockAt(coordination[0], coordination[1], coordination[2]).getLocation();
        }
    }
}
