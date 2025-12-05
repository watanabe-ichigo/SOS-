package com.example.sosbaton;

public class Shelter {
    public String name;
    public String address;
    public String type;
    public double lat;
    public double lng;
    public String docId;
    //ピンのコンストラクタ（・オブジェクト生成時に呼び出される・引数はメイン）
    public Shelter(String docId,String name, String address, String type, double lat, double lng) {
        this.docId = docId;
        this.name = name;
        this.address = address;
        this.type = type;
        this.lat = lat;
        this.lng = lng;
    }

}
