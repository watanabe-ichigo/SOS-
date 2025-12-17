package com.example.sosbaton;

public class Sospin {

    // 必須
    public long type;
    public double lat;
    public double lng;
    public long createdAt;
    public String docId;

    // 任意
    public Integer sosCategory;
    public Integer urgency;

    //public Sospin() {}

    public Sospin(long type,double lat, double lng,long createdAt,String docId) {
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.createdAt = createdAt;
        this.docId = docId;
    }
}
