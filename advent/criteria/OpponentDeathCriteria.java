package net.xincraft.systems.advent.criteria;

public class OpponentDeathCriteria extends Criteria {
    private boolean byGoal = false;

    public OpponentDeathCriteria(String name) {
        super(name);
    }

    public OpponentDeathCriteria byGoal(boolean byGoal) {
        this.byGoal = byGoal;
        return this;
    }

    public boolean check(boolean byGoal) {
        // Logic to check if the opponent jumped through their own goal
        return this.byGoal == byGoal;
    }
}
