package com.example.sosbaton.model;

import com.google.firebase.Timestamp;

public class FriendRequest {
    private String docId;

    private String from_id;
    private String from_name;

    private String to_id;
    private String to_name;

    private String status;
    private Timestamp created_at;

    public FriendRequest() {}

    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }

    public String getFrom_id() { return from_id; }
    public String getFrom_name() { return from_name; }

    public String getTo_id() { return to_id; }
    public String getTo_name() { return to_name; }

    public String getStatus() { return status; }
    public Timestamp getCreated_at() { return created_at; }
    /*private String docId;
    private String from_id;
    private String from_name;
    private String to;
    private String status;
    private Timestamp created_at;

    public FriendRequest() {}

    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }

    public String getFrom() { return from_id; }
    public void setFrom(String from_id) { this.from_id = from_id; }
    public String getFrom_name() { return from_name; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }*/
}
