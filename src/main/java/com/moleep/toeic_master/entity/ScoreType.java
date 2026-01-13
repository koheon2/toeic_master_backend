package com.moleep.toeic_master.entity;

public enum ScoreType {
    JOIN_STUDY(50),
    WRITE_REVIEW(20);

    private final int points;

    ScoreType(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
