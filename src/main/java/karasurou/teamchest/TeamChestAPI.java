package karasurou.teamchest;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.json.*;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
            String team = getTeamFromSign(block);
            return team != null;
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
            String team = getTeamFromChest(block);
            return team != null;
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

    public static boolean getPlayerTeams(Player sender) {
        try {
            String[] teams = searchForTeamsFromMember(sender.getName());
            if (teams.length == 0) {
                sender.sendMessage(Config.getLanguage("player_no_team"));
            } else if (teams.length == 1) {
                sender.sendMessage(Config.getLanguage("player_one_team")
                        .replace("[TEAM]", teams[0])
                        .replace("[PLAYER]", getMembersFromTeam(teams[0])[0]));
            } else {
                StringBuilder output = new StringBuilder(Config.getLanguage("player_multiple_team")
                        .replace("[AMOUNT]", String.valueOf(teams.length)));
                for (String team : teams) {
                    output.append("\n").append(Config.getLanguage("player_team_1")
                            .replace("[TEAM]", team)
                            .replace("[PLAYER]", getMembersFromTeam(team)[0]));
                }
                sender.sendMessage(output.toString());
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static boolean getTeamInvitations(String teamName, Player sender) {
        try {
            if (getMembersFromTeam(teamName)[0].equals(sender.getName())) {
                String[] member = getInvitationsFromTeam(teamName);
                if (member == null) {
                    sender.sendMessage(Config.getLanguage("no_invitations")
                            .replace("[TEAM]", teamName));
                } else if (member.length == 1) {
                    sender.sendMessage(Config.getLanguage("one_invitation")
                            .replace("[TEAM]",teamName)
                            .replace("[PLAYER]",member[0]));
                } else {
                    StringBuilder output = new StringBuilder(Config.getLanguage("multiple_invitations")
                            .replace("[AMOUNT]", String.valueOf(member.length))
                            .replace("[TEAM]", teamName));
                    for (int i = 0; i < member.length; i++) {
                        if (i == 0) {
                            output.append(Config.getLanguage("invitation_1")
                                    .replace("[PLAYER]",member[i]));
                        } else {
                            output.append(", ").append(Config.getLanguage("invitation_1")
                                    .replace("[PLAYER]",member[i]));
                        }
                    }
                    sender.sendMessage(output.toString());
                }
            }
            return true;
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

    public static boolean cancelTeamInvitation(String teamName, String playerName, Player owner) {
        try {
            if (getMembersFromTeam(teamName)[0].equals(owner.getName())) {
                String[] invitations = getInvitationsFromTeam(teamName);
                if (invitations == null) {
                    owner.sendMessage(Config.getLanguage("no-invite")
                            .replace("[TEAM]", teamName));
                    return true;
                }
                if (Arrays.asList(invitations).contains(playerName)) {
                    removePlayerInvitation(teamName, playerName);
                    owner.sendMessage(Config.getLanguage("team_cancelinvite")
                            .replace("[TEAM]", teamName)
                            .replace("[PLAYER]", playerName));
                } else {
                    owner.sendMessage(Config.getLanguage("no-invite-member")
                            .replace("[TEAM]", teamName)
                            .replace("[PLAYER]", playerName));
                }
            }
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

    public static boolean allowedToOpenChest(Block block, Player player) {
        String team = getTeamFromChest(block);
        if (team == null)
            return false;

        String[] members = getMembersFromTeam(team);
        return Arrays.asList(members).contains(player.getName());
    }

    public static boolean openChest(String teamName, Player sender) {
        try {
            if (Arrays.asList(getMembersFromTeam(teamName)).contains(sender.getName())) {
                if (!teamHaveStorage(teamName)) {
                    sender.sendMessage(Config.getLanguage("no_team_storage")
                            .replace("[TEAM]", teamName));
                    return true;
                }
                Location location = getChestFromTeam(teamName);
                int x = location.getBlockX();
                int y = location.getBlockY();
                int z = location.getBlockZ();
                Inventory inventory = ((Chest) plugin.getServer().getWorld(getChestLocationFromTeam(teamName)).getBlockAt(x, y, z).getState()).getInventory();
                sender.openInventory(inventory);
            } else {
                sender.sendMessage(Config.getLanguage("no-team-member").replace("[TEAM]", teamName));
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static void setSignAndChestForTeam(String teamName, Block sign, Block chest) {
        try {
            setSignAndChestFromTeam(teamName, sign, chest);
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
    }

    public static boolean isOwner(String teamName, String player) {
        return EditTeamFile.getMembersFromTeam(teamName)[0].equals(player);
    }

    public static void removeChestSignETC(String teamName) {
        try {
            removeSignAndChestFromTeam(teamName);
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
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

    private static String[] searchForTeamsFromMember(String name) {
        List<String> foundTeams = new ArrayList<>();
        String[] teams = EditTeamFile.getTeams();
        for (String team : teams) {
            String[] members = EditTeamFile.getMembersFromTeam(team);
            if (Arrays.asList(members).contains(name)) {
                foundTeams.add(team);
            }
        }
        return foundTeams.toArray(new String[0]);
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
        if (Arrays.asList(beforeMember).contains(player)) {
            for (String member : beforeMember) {
                if (!member.equals(player)) {
                    afterMember[i] = member;
                    i++;
                }
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
        if (Arrays.asList(beforeMember).contains(player)) {
            for (String member : beforeMember) {
                if (!member.equals(player)) {
                    afterMember[i] = member;
                    i++;
                }
            }
            if (afterMember.length == 0) {
                afterMember = null;
            }
            EditTeamFile.setInvitationsForTeam(teamName, afterMember);
        }
    }

    private static String[] getInvitationsFromTeam(String teamName) {
        return EditTeamFile.getInvitationsForTeam(teamName);
    }

    private static String getTeamFromChest(Block chest) {
        String[] teams = EditTeamFile.getTeams();
        Location searchedLocation = chest.getLocation();
        for (String team : teams) {
            Location givenLocation = getChestFromTeam(team);
            if (givenLocation == null)
                continue;

            if (givenLocation.getBlockX() == searchedLocation.getBlockX() &&
                    givenLocation.getBlockY() == searchedLocation.getBlockY() &&
                    givenLocation.getBlockZ() == searchedLocation.getBlockZ()) {
                return team;
            }
        }
        return null;
    }

    private static boolean teamHaveStorage(String teamName) {
        return getChestFromTeam(teamName) != null;
    }

    private static String getTeamFromSign(Block sign) {
        String[] teams = EditTeamFile.getTeams();
        Location searchedLocation = sign.getLocation();
        for (String team : teams) {
            Location givenLocation = getSignFromTeam(team);
            if (givenLocation == null) {
                continue;
            }

            if (givenLocation.getBlockX() == searchedLocation.getBlockX() &&
                    givenLocation.getBlockY() == searchedLocation.getBlockY() &&
                    givenLocation.getBlockZ() == searchedLocation.getBlockZ()) {
                return team;
            }
        }
        return null;
    }

    private static Location getChestFromTeam(String teamName) {
        return EditTeamFile.getChestLocation(teamName);
    }

    private static String getChestLocationFromTeam(String teamName) {
        return EditTeamFile.getWorld(teamName);
    }

    private static Location getSignFromTeam(String teamName) {
        return EditTeamFile.getSignLocation(teamName);
    }

    private static void setSignAndChestFromTeam(String teamName, Block sign, Block chest) throws Exception {
        EditTeamFile.setSignLocation(teamName, sign.getLocation());
        EditTeamFile.setChestLocation(teamName, chest.getLocation());
        EditTeamFile.setWorld(teamName, chest.getWorld().getName());
    }

    private static void removeSignAndChestFromTeam(String teamName) throws Exception {
        EditTeamFile.setSignLocation(teamName, null);
        EditTeamFile.setChestLocation(teamName, null);
        EditTeamFile.setWorld(teamName, null);
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

        private static void setWorld(String team, String world) throws Exception{
            if (world == null) {
                teamAndWorldCombination.remove(team);
            } else {
                if (teamAndWorldCombination.containsKey(team)) {
                    teamAndWorldCombination.replace(team, world);
                } else {
                    teamAndWorldCombination.put(team, world);
                }
            }
            new EditTeamFile().writeTeamFile();
        }

        private static String getWorld(String team) {
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
                    object.put("SignLocation", getLocationInts(teamAndSignCombination.get(team)));
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
