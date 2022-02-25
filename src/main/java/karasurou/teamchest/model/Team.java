package karasurou.teamchest.model;

import org.bukkit.entity.Player;

public class Team {

    private Player[] players;

    public Team(Player[] players) {
        this.players = players;
    }

    public Player[] getPlayer(){
        return players;
    }

    public Player[] addPlayer(Player player) {
        Player[] newPlayer = new Player[players.length + 1];
        System.arraycopy(players, 0, newPlayer, 0, players.length);
        newPlayer[newPlayer.length - 1] = player;
        this.players = newPlayer;
        return newPlayer;
    }
}
