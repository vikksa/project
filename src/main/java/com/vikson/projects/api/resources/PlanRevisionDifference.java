package com.vikson.projects.api.resources;

import java.util.Objects;

public class PlanRevisionDifference {
    private int moveX;
    private int moveY;
    private double factor;

    public PlanRevisionDifference() {}

    public PlanRevisionDifference(int moveX, int moveY, double factor) {
        this.moveX = moveX;
        this.moveY = moveY;
        this.factor = factor;
    }

    public int getMoveX() {
        return moveX;
    }

    public void setMoveX(int moveX) {
        this.moveX = moveX;
    }

    public PlanRevisionDifference withMoveX(int moveX) {
        this.moveX = moveX;
        return this;
    }

    public int getMoveY() {
        return moveY;
    }

    public void setMoveY(int moveY) {
        this.moveY = moveY;
    }

    public PlanRevisionDifference withMoveY(int moveY) {
        this.moveY = moveY;
        return this;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public PlanRevisionDifference withFactor(double factor) {
        this.factor = factor;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanRevisionDifference that = (PlanRevisionDifference) o;
        return moveX == that.moveX &&
                moveY == that.moveY &&
                Double.compare(that.factor, factor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(moveX, moveY, factor);
    }

    @Override
    public String toString() {
        return "PlanRevisionDifference{" +
                "moveX=" + moveX +
                ", moveY=" + moveY +
                ", factor=" + factor +
                '}';
    }
}
