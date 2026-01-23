package com.example.sosbaton;

public class FriendRequestResult {
    //データ通信用のクラス

    public enum Status { SUCCESS, ERROR, ALREADY_FRIEND, ALREADY_REQUESTED, NOT_FOUND }

    private final Status status;
    private final String message;

    public FriendRequestResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }
}
