package com.vikson.projects.model.values;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class RevisionLocationDifference {

    @Column(name = "move_x")
    private int moveX;
    @Column(name = "move_y")
    private int moveY;
    @Column(name = "factor")
    private double factor;

    public int getMoveX() {
        return moveX;
    }

    public void setMoveX(int moveX) {
        this.moveX = moveX;
    }

    public RevisionLocationDifference withMoveX(int moveX) {
        this.moveX = moveX;
        return this;
    }

    public int getMoveY() {
        return moveY;
    }

    public void setMoveY(int moveY) {
        this.moveY = moveY;
    }

    public RevisionLocationDifference withMoveY(int moveY) {
        this.moveY = moveY;
        return this;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public RevisionLocationDifference withFactor(double factor) {
        this.factor = factor;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevisionLocationDifference that = (RevisionLocationDifference) o;
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
        return "RevisionLocationDifference{" +
                "moveX=" + moveX +
                ", moveY=" + moveY +
                ", factor=" + factor +
                '}';
    }
}
