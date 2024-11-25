package net.xincraft.systems.advent.criteria;

import org.bukkit.Material;

public class KillCriteria extends Criteria {
    private int kills = 1;
    private Material weapon = null;
    private int damageTakenRequirement = -1;

    public KillCriteria(String name) {
        super(name);
    }

    public KillCriteria withKills(int kills) {
        this.kills = kills;
        return this;
    }

    public KillCriteria withWeapon(Material weapon) {
        this.weapon = weapon;
        return this;
    }

    public KillCriteria withDamageTakenRequirement(int damageTakenRequirement) {
        this.damageTakenRequirement = damageTakenRequirement;
        return this;
    }

    public boolean check(int kills, Material weapon, int damageTaken) {
        if (damageTakenRequirement > -1) {
            // if the player has taken more damage than the requirement, return false
            if (damageTaken > damageTakenRequirement) {
                return false;
            }
        }

        // check if the player has killed enough players
        if (kills < this.kills) {
            return false;
        }

        // check if the player has killed a player with the correct weapon
        return this.weapon == null || weapon == this.weapon;
    }
}
