package net.xincraft.systems.advent.criteria;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bukkit.Material;

import java.util.UUID;

public class EatCriteria extends Criteria {
    private Material material = Material.GOLDEN_APPLE;
    private int amount = 1;

    private MongoCollection<Document> collection;

    public EatCriteria(String name) {
        super(name);
    }

    public EatCriteria withMaterial(Material material) {
        this.material = material;
        return this;
    }

    public EatCriteria withAmount(MongoClient client, int amount) {
        this.amount = amount;
        this.collection = client.getDatabase("advent-2024").getCollection(getName());
        return this;
    }

    public boolean check(UUID playerUUID, Material material) {
        // Logic to check if the player has eaten the required number of golden apples
        if (this.material != material) {
            return false;
        }

        if (amount > 1) {
            Document document = collection.find(new Document("uuid", playerUUID.toString())).first();
            if (document == null) {
                // if it doesn't exist, create it
                document = new Document("uuid", playerUUID.toString());
                document.put("amount", 1);
                collection.insertOne(document);
            }

            int playerAmount = document.getInteger("amount");
            if (playerAmount >= amount) {
                return true;
            } else {
                document.put("amount", playerAmount + 1);
                collection.replaceOne(new Document("uuid", playerUUID.toString()), document);
                return false;
            }
        }

        return true;
    }
}
