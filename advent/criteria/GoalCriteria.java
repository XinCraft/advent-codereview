package net.xincraft.systems.advent.criteria;

public class GoalCriteria extends Criteria {
    private int goals = 1;
    private int teamSize = 0;
    private int timeRequirement = -1; // in seconds
    private boolean withoutPlacingBlocks = false;

    public GoalCriteria(String name) {
        super(name);
    }

    public GoalCriteria withGoals(int goals) {
        this.goals = goals;
        return this;
    }

    public GoalCriteria withTeamSize(int teamSize) {
        this.teamSize = teamSize;
        return this;
    }

    public GoalCriteria withTimeRequirement(int timeRequirement) {
        this.timeRequirement = timeRequirement;
        return this;
    }

    public GoalCriteria withoutPlacingBlocks(boolean withoutPlacingBlocks) {
        this.withoutPlacingBlocks = withoutPlacingBlocks;
        return this;
    }

    public boolean check(int goals, int teamSize, int timeInSeconds, boolean placedBlocks) {
        if (teamSize != 0 && teamSize != this.teamSize) {
            return false;
        }

        if (timeRequirement > -1) {
            if (timeInSeconds > timeRequirement) {
                return false;
            }
        }

        if (withoutPlacingBlocks && placedBlocks) {
            return false;
        }

        return goals >= this.goals;
    }
}
