package karasurou.teamchest;

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

    private static class EditTeamFile{

        private final static File file = new File(plugin.getDataFolder(),"teams.json");
        private final static File invitationFile = new File(plugin.getDataFolder(),"teamINV.json");
        private final static HashMap<String, String[]> teamAndMemberCombination = new HashMap<>();
        private final static HashMap<String, String[]> teamAndInvitationCombination = new HashMap<>();

        public static void init() {
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                        plugin.getLogger().info(Config.getLanguage("teamfilecreated"));
                    }
                    if (!invitationFile.exists()) {
                        invitationFile.createNewFile();
                        plugin.getLogger().info(Config.getLanguage("teaminvitationfilecreated"));
                    }
                } catch (IOException e) {
                    plugin.getLogger().severe(e.getMessage());
                }
        }

        private EditTeamFile(){}

        private static String[] getTeams() {
            new EditTeamFile().reloadFiles();
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
            new EditTeamFile().reloadFiles();
            return teamAndMemberCombination.get(teamName);
        }

        private static void setMembersFromTeam(String team, String[] members) throws IOException {
            teamAndMemberCombination.replace(team, members);
            new EditTeamFile().writeTeamFile();
        }

        private static String[] getInvitationsForTeam(String teamName) {
            new EditTeamFile().reloadFiles();
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

        private void reloadFiles() {
            teamAndMemberCombination.clear();
            JSONObject teams;
            JSONArray memberArray;
            try {
                teams = new JSONObject(new EditTeamFile().readFile(file));
                memberArray = (JSONArray) teams.get("teams");
            } catch (Exception ignored) {
                memberArray = new JSONArray();
            }
            JSONArray invitationMemberArray;
            try {
                teams = new JSONObject(new EditTeamFile().readFile(invitationFile));
                invitationMemberArray = (JSONArray) teams.get("teams");
            } catch (Exception ignored) {
                invitationMemberArray = new JSONArray();
            }
            for (int i = 0; i < memberArray.length() || i < invitationMemberArray.length(); i++) {
                if (i <= memberArray.length() - 1) {
                    JSONObject jsonObject = memberArray.getJSONObject(i);
                    String team = (String) jsonObject.keySet().toArray()[0];
                    String[] members = ((JSONArray)jsonObject.get(team)).toList().toArray(new String[0]);
                    teamAndMemberCombination.put(team, members);
                }
                if (i <= invitationMemberArray.length() - 1) {
                    JSONObject jsonObject = invitationMemberArray.getJSONObject(i);
                    String team = (String) jsonObject.keySet().toArray()[0];
                    String[] members = ((JSONArray)jsonObject.get(team)).toList().toArray(new String[0]);
                    teamAndInvitationCombination.put(team, members);
                }
            }
        }

        private String readFile(File file) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuilder raw = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                raw.append(line).append("\n");
            }
            return raw.toString();
        }

        private void writeTeamFile() throws IOException {
            writeTeamFile(file, teamAndMemberCombination);
            writeTeamFile(invitationFile, teamAndInvitationCombination);
        }

        private void writeTeamFile(File file, HashMap<String, String[]> hashMap) throws IOException {
            String[] teams = hashMap.keySet().toArray(new String[0]);
            JSONArray jsonArray = new JSONArray();
            for (String team : teams) {
                String[] member = hashMap.get(team);
                JSONObject object = new JSONObject();
                object.put(team, member);
                jsonArray.put(object);
            }
            JSONObject allTeams = new JSONObject();
            allTeams.put("teams", jsonArray);
            Writer writer = new FileWriter(file);
            writer.write(allTeams.toString());
            writer.close();
        }
    }
}
