package com.example.sosbaton;

public class PinInfo {
    public String docId;

    public String name;
    public String typeName;
    public double lat;
    public double lng;
    public long type;



    public PinInfo(String docId, String typeName, String name,Long type, double lat, double lng ) {
        this.docId = docId;
        this.typeName = typeName;
        this.name = name;
        this.type = type;
        this.lat = lat;
        this.lng = lng;

    }

    public Long getType() {
        return type;
    }
}
