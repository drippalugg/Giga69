package com.example.giga67.model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Review {
    private String id;
    private int partId;
    private String userId;
    private String userName;
    private int rating;
    private String comment;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private boolean isEdited;

    public Review(String id, int partId, String userId, String userName, int rating, String comment, ZonedDateTime createdAt, ZonedDateTime updatedAt, boolean isEdited) {
        this.id = id;
        this.partId = partId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isEdited = isEdited;
    }

    public String getId() { return id; }
    public int getPartId() { return partId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName != null ? userName : "Пользователь"; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public boolean isEdited() { return isEdited; }

    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setEdited(boolean edited) { isEdited = edited; }

    public String getFormattedDate() {
        if (createdAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            return createdAt.format(formatter);
        }
        return "";
    }

    public String getStars() {
        return "\u2605".repeat(rating) + "\u2606".repeat(5 - rating);
    }

    @Override
    public String toString() {
        return "Review{id='" + id + "', partId=" + partId + ", rating=" + rating + ", comment='" + comment + "'}";
    }
}
