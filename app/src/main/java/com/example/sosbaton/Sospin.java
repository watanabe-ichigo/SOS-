package com.example.sosbaton;
import android.widget.TextView;
import java.io.Serializable;
import com.google.android.gms.maps.model.Marker;

public class Sospin  implements Serializable {

    // 必須
    public long type;
    public double lat;
    public double lng;
    public long createdAt;
    public String docId;
    public long sosCategory;
    public long urgency;
    public long supporttype;

    public long q4;

    public long q5;
    public String Uname;

    // UI 用フィールド
    public Marker marker; // Firestoreには保存しない
    public String uid;    // Firestoreには保存しない


    //public Sospin() {} ファイアベース自動追加用"Sospin sos = doc.toObject(Sospin.class);"


    public Sospin(long type,double lat, double lng,long createdAt,long sosCategory,long urgency,long supporttype,String Uname,String uid,String docId,long q4,long q5) {
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.createdAt = createdAt;
        this.sosCategory = sosCategory;
        this.urgency = urgency;
        this.supporttype = supporttype;
        this.Uname = Uname;
        this.uid = uid;
        this.docId=docId;
        this.q4 =q4;
        this.q5 =q5;
    }



}
