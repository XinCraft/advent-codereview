package net.xincraft.systems.advent.criteria;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import net.xincraft.database.data.PlayerData;
import org.bson.Document;

import java.util.Collection;
import java.util.UUID;

public class PlayCriteria extends Criteria {
    // store the number of matches a player plays in mongo
    private MongoCollection<Document> collection;

    private int matchesToPlay = 1;
    private int teamSize = 0;
    private boolean friendedTeammate = false;

    public PlayCriteria(String name) {
        super(name);
    }

    public PlayCriteria withMatchesToPlay(MongoClient client, int matchesToPlay) {
        this.matchesToPlay = matchesToPlay;
        this.collection = client.getDatabase("advent-2024").getCollection(getName());
        return this;
    }

    public PlayCriteria withTeamSize(int teamSize) {
        this.teamSize = teamSize;
        return this;
    }

    public PlayCriteria withFriendedTeammate(boolean friendedTeammate) {
        this.friendedTeammate = friendedTeammate;
        return this;
    }

    public boolean check(UUID playerUUID, int teamSize, Collection<UUID> teammates) {
        // check if the team size of the match, matches the criteria
        if (teamSize != 0 && teamSize != this.teamSize) {
            return false;
        }

        // check if the player has friended their teammate
        // loop through teammates and check if their uuid is present in the player's friends list
        if (friendedTeammate) {
            boolean anyTeammateFriended = teammates.stream()
                    .anyMatch(teammate -> PlayerData.get(playerUUID).getFriends().contains(teammate));

            // if no teammates are friends, return false
            if (!anyTeammateFriended) {
                return false;
            }
        }

        if (matchesToPlay > 1) {
            // check if the player has played enough matches
            // get the document for the player within the collection
            Document document = collection.find(new Document("uuid", playerUUID.toString())).first();
            if (document == null) {
                // if it doesn't exist, create it
                document = new Document("uuid", playerUUID.toString())
                        .append("matchesPlayed", 0);
                collection.insertOne(document);
            }

            // get the number of matches played by the player
            int matchesPlayed = document.getInteger("matchesPlayed");
            if (matchesPlayed < matchesToPlay) {
                // if the player hasn't played enough matches, increment the number of matches played
                document.put("matchesPlayed", matchesPlayed + 1);
                collection.replaceOne(new Document("uuid", playerUUID.toString()), document);
                return false;
            } else {
                // if the player has played enough matches, return true
                return true;
            }
        }

        return true;
    }
}
