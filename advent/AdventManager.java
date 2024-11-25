package net.xincraft.systems.advent;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import net.xincraft.XinCraftPlugin;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AdventManager {
    private static final String DATABASE_NAME = "advent-2024";
    private static final String PLAYER_COLLECTION_NAME = "players";
    private final MongoCollection<Document> playerCollection;

    public AdventManager(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        playerCollection = database.getCollection(PLAYER_COLLECTION_NAME);
    }

    private static @NotNull Bson uuidFilter(UUID playerUUID) {
        return Filters.eq("uuid", playerUUID.toString());
    }

    public CompletableFuture<Void> setupPlayer(UUID playerUUID) {
        return CompletableFuture.runAsync(() -> {
            if (playerCollection.find(uuidFilter(playerUUID)).first() == null) {
                Document playerDocument = new Document("uuid", playerUUID.toString())
                        .append("completedQuests", new ArrayList<String>());
                playerCollection.insertOne(playerDocument);
            }
        });
    }

    public void completePlayerChallenge(Player player, String completedChallenge) {
        // notify the player, making sure its on the main thread
        Bukkit.getScheduler().runTask(XinCraftPlugin.get(), () ->
                player.sendMessage(ChatColor.GREEN + "You have completed the advent challenge: " + completedChallenge));

        // update the database
        updateDatabase(player.getUniqueId(), completedChallenge).thenRun(() ->
                XinCraftPlugin.get().getLogger().info("Player " + player.getName() + " completed challenge " +
                        completedChallenge + " and the database has been updated."));
    }

    private @NotNull CompletableFuture<Void> updateDatabase(UUID playerUUID, String completedChallenge) {
        return CompletableFuture.runAsync(() -> {
            Document playerDocument = playerCollection.find(Filters.eq("uuid", playerUUID.toString())).first();
            if (playerDocument != null) {
                List<String> completedQuests = playerDocument.getList("completedQuests", String.class);
                if (!completedQuests.contains(completedChallenge)) {
                    completedQuests.add(completedChallenge);
                    playerDocument.put("completedQuests", completedQuests);
                    playerCollection.replaceOne(uuidFilter(playerUUID), playerDocument);
                }
            }
        });
    }
}