package com.example.sosbaton.model;

import com.google.firebase.Timestamp;

public class FriendRequest {
    private String docId;
    private String from;
    private String to;
    private String status;
    private Timestamp created_at;

    public FriendRequest() {}

    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }
}
