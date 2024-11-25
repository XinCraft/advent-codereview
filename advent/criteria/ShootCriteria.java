package net.xincraft.systems.advent.criteria;

public class ShootCriteria extends Criteria {
    private int distance = -1;

    public ShootCriteria(String name) {
        super(name);
    }

    public ShootCriteria withDistance(int distance) {
        this.distance = distance;
        return this;
    }

    public boolean check(int distance) {
        return distance >= this.distance;
    }
}
