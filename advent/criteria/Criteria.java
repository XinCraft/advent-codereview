package net.xincraft.systems.advent.criteria;

import lombok.Getter;

@Getter
public abstract class Criteria {
    private final String name;

    public Criteria(String name) {
        this.name = name;
    }

}