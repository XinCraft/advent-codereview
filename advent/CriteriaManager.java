package net.xincraft.systems.advent;

import com.mongodb.client.MongoClient;
import net.xincraft.XinCraftPlugin;
import net.xincraft.systems.advent.criteria.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CriteriaManager {
    // day of the month to criteria
    private final Map<Integer, Criteria> criteriaMap = new HashMap<>();

    public CriteriaManager(MongoClient mongoClient) {
        // Initialize criteria
        criteriaMap.put(1, new PlayCriteria("play-one-match"));
        criteriaMap.put(2, new KillCriteria("15-kill-match").withKills(15));
        criteriaMap.put(3, new GoalCriteria("score-3-times-in-match"));
        criteriaMap.put(4, new WinCriteria("win-match"));
        criteriaMap.put(5, new PlayCriteria("play-2v2-match").withTeamSize(2));
        criteriaMap.put(6, new GoalCriteria("3-goal-2v2-match").withGoals(3).withTeamSize(2));
        criteriaMap.put(7, new WinCriteria("win-with-less-than-10-deaths").withMaxDeathRequirement(10));
        criteriaMap.put(8, new GoalCriteria("goal-in-2-minutes").withTimeRequirement(60 * 2));
        criteriaMap.put(9, new ShootCriteria("shoot-from-40-blocks").withDistance(40));
        criteriaMap.put(10, new GoalCriteria("goal-without-blocks").withoutPlacingBlocks(true));
        criteriaMap.put(11, new KillCriteria("kill-with-bow").withWeapon(Material.BOW));
        criteriaMap.put(12, new DrawCriteria("draw-match"));
        criteriaMap.put(13, new KillCriteria("kill-without-taking-damage").withDamageTakenRequirement(0));
        criteriaMap.put(14, new WinCriteria("win-without-golden-apples").withWinInfo(
                new WinCriteria.WinCriteriaInfo(false, false, true)));
        criteriaMap.put(15, new WinCriteria("win-against-friend").withFriendedOpponent(true));
        criteriaMap.put(16, new PlayCriteria("play-2v2-with-friend").withTeamSize(2)
                .withFriendedTeammate(true));
        criteriaMap.put(17, new OpponentDeathCriteria("opponent-goal-death").byGoal(true));
        criteriaMap.put(18, new WinCriteria("win-without-blocking-hit").withWinInfo(
                new WinCriteria.WinCriteriaInfo(true, false, false)));
        criteriaMap.put(19, new WinCriteria("win-3v3-match").withTeamSize(3));
        criteriaMap.put(20, new PlayCriteria("play-4v4-match").withTeamSize(4));
        criteriaMap.put(21, new WinCriteria("win-against-3-different-opponents")
                .withDifferentPlayers(mongoClient, 3));
        criteriaMap.put(22, new PlayCriteria("play-5-matches").withMatchesToPlay(mongoClient, 5));
        criteriaMap.put(23, new WinCriteria("win-without-using-bow").withWinInfo(
                new WinCriteria.WinCriteriaInfo(false, true, false)));
        criteriaMap.put(24, new EatCriteria("eat-10-golden-apples").withAmount(mongoClient, 10));
    }

    private @NotNull Criteria getCriteria() {
        int dayOfMonth = LocalDate.now().getDayOfMonth();
        return criteriaMap.get(dayOfMonth);
    }

    public void onPlay(Player player, int teamSize, Collection<UUID> teammates) {
        CompletableFuture.runAsync(() -> {
            Criteria criteria = getCriteria();
            if (criteria instanceof PlayCriteria) {
                PlayCriteria playCriteria = (PlayCriteria) criteria;
                if (playCriteria.check(player.getUniqueId(), teamSize, teammates)) {
                    XinCraftPlugin.get().getAdventManager().completePlayerChallenge(player, playCriteria.getName());
                }
            }
        });
    }

    public void onDraw(Player player) {
        CompletableFuture.runAsync(() -> {
            Criteria criteria = getCriteria();
            if (criteria instanceof DrawCriteria) {
                DrawCriteria drawCriteria = (DrawCriteria) criteria;
                XinCraftPlugin.get().getAdventManager().completePlayerChallenge(player, drawCriteria.getName());
            }
        });
    }

    public void onWin(Player player, int teamSize, Collection<UUID> opponents, int deaths,
                      boolean blockedHits, boolean usedBow, boolean usedGoldenApples) {
        CompletableFuture.runAsync(() -> {
            Criteria criteria = getCriteria();
            if (criteria instanceof WinCriteria) {
                WinCriteria winCriteria = (WinCriteria) criteria;
                if (winCriteria.check(player.getUniqueId(), teamSize, opponents, deaths, blockedHits, usedBow, usedGoldenApples)) {
                    XinCraftPlugin.get().getAdventManager().completePlayerChallenge(player, winCriteria.getName());
                }
            }
        });
    }

    public void onKill(Player player, int kills, Material weapon, int damageTaken) {
        CompletableFuture.runAsync(() -> {
            Criteria criteria = getCriteria();
            if (criteria instanceof KillCriteria) {
                KillCriteria killCriteria = (KillCriteria) criteria;
                if (killCriteria.check(kills, weapon, damageTaken)) {
                    XinCraftPlugin.get().getAdventManager().completePlayerChallenge(player, killCriteria.getName());
                }
            }
        });
    }

    public void onShoot(Player player, int distance) {
        CompletableFuture.runAsync(() -> {
            Criteria criteria = getCriteria();
            if (criteria instanceof ShootCriteria) {
                ShootCriteria shootCriteria = (ShootCriteria) criteria;
                if (shootCriteria.check(distance)) {
                    XinCraftPlugin.get().getAdventManager().completePlayerChallenge(player, shootCriteria.getName());
                }
            }
        });
    }

    public void onOpponentDeath(Player player, boolean byGoal) {
        CompletableFuture.runAsync(() -> {
            Criteria criteria = getCriteria();
            if (criteria instanceof OpponentDeathCriteria) {
                OpponentDeathCriteria opponentDeathCriteria = (OpponentDeathCriteria) criteria;
                if (opponentDeathCriteria.check(byGoal)) {
                    XinCraftPlugin.get().getAdventManager().completePlayerChallenge(player, opponentDeathCriteria.getName());
                }
            }
        });
    }

    public void onGoal(Player player , int goals, int teamSize, int ongoingTimeInSeconds, boolean placedBlocks) {
        CompletableFuture.runAsync(() -> {
            Criteria criteria = getCriteria();
            if (criteria instanceof GoalCriteria) {
                GoalCriteria goalCriteria = (GoalCriteria) criteria;
                if (goalCriteria.check(goals, teamSize, ongoingTimeInSeconds, placedBlocks)) {
                    XinCraftPlugin.get().getAdventManager().completePlayerChallenge(player, goalCriteria.getName());
                }
            }
        });
    }

    public void onEat(Player player, Material material) {
        CompletableFuture.runAsync(() -> {
            Criteria criteria = getCriteria();
            if (criteria instanceof EatCriteria) {
                EatCriteria eatCriteria = (EatCriteria) criteria;
                if (eatCriteria.check(player.getUniqueId(), material)) {
                    XinCraftPlugin.get().getAdventManager().completePlayerChallenge(player, eatCriteria.getName());
                }
            }
        });
    }
}