package karasurou.teamchest;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.*;

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
                return true;
            } else {
                player.sendMessage(Config.getLanguage("NOT OWNER"));// TODO: 03.03.2022
            }
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static boolean acceptInvitation(String teamName, Player player) {// TODO: 03.03.2022
        try {
//            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static boolean denyInvitation(String teamName, Player player) {// TODO: 03.03.2022
        try {
//            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static boolean leaveTeam(String teamName, Player player) {
        try {
            removePlayerFromTeam(teamName, player.getName());
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static boolean inviteToTeam(String teamName, String player, Player sendPlayer) {// TODO: 03.03.2022
        try {
//            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    public static boolean kickFromTeam(String teamName, String player, Player sendPlayer) {
        try {
            if (getMembersFromTeam(teamName)[0].equals(sendPlayer.getName())) {
                removePlayerFromTeam(teamName, player);
                return true;
            } else {
                sendPlayer.sendMessage(Config.getLanguage("NOT OWNER"));// TODO: 03.03.2022
            }
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
        return false;
    }

    private static String[] getTeams() throws IOException {
        return EditTeamFile.getTeams();
    }

    private static String[] getMembersFromTeam(String teamName) throws IOException {
        return EditTeamFile.getMembersFromTeam(teamName);
    }

    private static void addTeam(String teamName, String owner) throws IOException {
        EditTeamFile.addTeam(teamName,owner);
    }

    private static void removeTeam(String teamName) throws IOException {
        EditTeamFile.removeTeam(teamName);
    }

    private static void addPlayerFromTeam(String teamName, String member) throws IOException {
        String[] beforeMember = EditTeamFile.getMembersFromTeam(teamName);
        String[] afterMember = new String[beforeMember.length + 1];
        System.arraycopy(beforeMember, 0, afterMember, 0, afterMember.length);
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

    private static class EditTeamFile{

        private final static File file = new File(plugin.getDataFolder(),"teams.json");
        private final static HashMap<String, String[]> teamAndMemberCombination = new HashMap<>();

        public static void init() {
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().severe(e.getMessage());
                }
            }
        }

        private EditTeamFile(){}

        private static String[] getTeams() throws IOException {
            new EditTeamFile().reloadFile();
            return teamAndMemberCombination.keySet().toArray(new String[0]);
        }

        private static void addTeam(String teamName, String owner) throws IOException {
            teamAndMemberCombination.put(teamName, new String[]{owner});
            new EditTeamFile().writeFile();
        }

        private static void removeTeam(String team) throws IOException {
            teamAndMemberCombination.remove(team);
            new EditTeamFile().writeFile();
        }

        private static String[] getMembersFromTeam(String teamName) throws IOException {
            new EditTeamFile().reloadFile();
            return teamAndMemberCombination.get(teamName);
        }

        private static void setMembersFromTeam(String team, String[] members) throws IOException {
            teamAndMemberCombination.replace(team, members);
            new EditTeamFile().writeFile();
        }

        private void reloadFile() throws IOException {
            teamAndMemberCombination.clear();
            org.json.JSONObject teams = new org.json.JSONObject(new EditTeamFile().readFile());
            JSONArray jsonArray = (JSONArray) teams.get("teams");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String team = (String) jsonObject.keySet().toArray()[0];
                String[] members = ((JSONArray)jsonObject.get(team)).toList().toArray(new String[0]);
                teamAndMemberCombination.put(team, members);
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

        private void writeFile() throws IOException {
            String[] teams = teamAndMemberCombination.keySet().toArray(new String[0]);
            JSONArray jsonArray = new JSONArray();
            for (String team : teams) {
                String[] member = teamAndMemberCombination.get(team);
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
