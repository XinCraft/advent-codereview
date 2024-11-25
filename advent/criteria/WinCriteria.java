package net.xincraft.systems.advent.criteria;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import net.xincraft.database.data.PlayerData;
import org.bson.Document;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class WinCriteria extends Criteria {
    private int teamSize = 0;
    private WinCriteriaInfo winInfo =
            new WinCriteriaInfo(false, false, false);
    private boolean friendedOpponent = false;
    private int differentPlayers = 0;
    private int maxDeathRequirement = -1;

    private MongoCollection<Document> collection;

    public WinCriteria(String name) {
        super(name);
    }

    public static class WinCriteriaInfo {
        public final boolean withoutHitBlocking;
        public final boolean withoutBow;
        public final boolean withoutGoldenApples;

        public WinCriteriaInfo(boolean withoutHitBlocking, boolean withoutBow, boolean withoutGoldenApples) {
            this.withoutHitBlocking = withoutHitBlocking;
            this.withoutBow = withoutBow;
            this.withoutGoldenApples = withoutGoldenApples;
        }
    }

    public WinCriteria withTeamSize(int teamSize) {
        this.teamSize = teamSize;
        return this;
    }

    public WinCriteria withWinInfo(WinCriteria.WinCriteriaInfo info) {
        this.winInfo = info;
        return this;
    }

    public WinCriteria withFriendedOpponent(boolean friendedOpponent) {
        this.friendedOpponent = friendedOpponent;
        return this;
    }

    public WinCriteria withDifferentPlayers(MongoClient client, int differentPlayers) {
        this.collection = client.getDatabase("advent-2024").getCollection(getName());
        this.differentPlayers = differentPlayers;
        return this;
    }

    public WinCriteria withMaxDeathRequirement(int maxDeathReq) {
        this.maxDeathRequirement = maxDeathReq;
        return this;
    }

    public boolean check(UUID playerUUID, int teamSize, Collection<UUID> opponents,
                         int deaths, boolean blockedHits, boolean usedBow, boolean usedGoldenApples) {
        // check if the team size of the match, matches the criteria
        if (teamSize != 0 && teamSize != this.teamSize) {
            return false;
        }

        // check if the player has friended their opponent
        // loop through opponents and check if their uuid is present in the player's friends list
        if (friendedOpponent) {
            boolean anyOpponentFriended = opponents.stream()
                    .anyMatch(opponent -> PlayerData.get(playerUUID).getFriends().contains(opponent));

            // if no opponents are friends, return false
            if (!anyOpponentFriended) {
                return false;
            }
        }

        if (checkDifferentOpponents(playerUUID, opponents)) {
            return false;
        }

        if (checkWinInfo(blockedHits, usedBow, usedGoldenApples)) {
            return false;
        }

        if (maxDeathRequirement > -1) {
            if (deaths > maxDeathRequirement) {
                return false;
            }
        }

        return true;
    }

    private boolean checkWinInfo(boolean blockedHits, boolean usedBow, boolean usedGoldenApples) {
        if (winInfo.withoutHitBlocking) {
            return blockedHits;
        }

        if (winInfo.withoutBow) {
            return usedBow;
        }

        if (winInfo.withoutGoldenApples) {
            return usedGoldenApples;
        }

        return false;
    }

    private boolean checkDifferentOpponents(UUID playerUUID, Collection<UUID> opponents) {
        if (differentPlayers == 0) {
            return false;
        }

        // check if the player has played against different players by checking by the database
        // check if the player has played enough matches
        // get the document for the player within the collection
        Document document = collection.find(new Document("uuid", playerUUID.toString())).first();
        if (document == null) {
            // if it doesn't exist, create it
            document = new Document("uuid", playerUUID.toString());
            document.put("opponents", opponents);
            collection.insertOne(document);
        }

        List<UUID> opponentsList = document.getList("opponents", UUID.class);
        if (opponentsList.size() < differentPlayers) {
            return true;
        } else {
            // add opponents to database for next lookup if they don't already exist in the list
            opponents.forEach(opponent -> {
                if (!opponentsList.contains(opponent)) {
                    opponentsList.add(opponent);
                }
            });

            // update the opponents list in the database
            document.put("opponents", opponentsList);
            collection.replaceOne(new Document("uuid", playerUUID.toString()), document);
        }

        return false;
    }
}
