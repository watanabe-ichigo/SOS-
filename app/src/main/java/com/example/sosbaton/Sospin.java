package com.example.sosbaton;
import android.widget.TextView;


public class Sospin {

    // 必須
    public long type;
    public double lat;
    public double lng;
    public long createdAt;
    public String docId;
    public long sosCategory;
    public long urgency;
    public long supporttype;
    public String Uname;



    //public Sospin() {}

    public Sospin(long type,double lat, double lng,long createdAt,String docId,long sosCategory,long urgency,long supporttype,String Uname) {
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.createdAt = createdAt;
        this.docId = docId;
        this.sosCategory = sosCategory;
        this.urgency = urgency;
        this.supporttype = supporttype;
        this.Uname = Uname;
    }

    public Sospin(long type,double lat, double lng,long createdAt,String docId) {
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.createdAt = createdAt;
        this.docId = docId;
    }
}
