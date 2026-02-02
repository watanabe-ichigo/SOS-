package com.example.sosbaton;

public class FriendModel {
      //フレンドリストのデータ保持クラス

    //検索結果表示用のuserid
    private String userId;

    //検索結果表示用のusername
    private String username;

    //URLを保存する場所
    private String iconUrl;

    //現状の避難場所id
    private String currentBoardId;

    //避難時間
    private String evacuatedAt;
    //sos状態のフラグ
    private boolean isSos;

    //sos座標
    private double sos_latitude;
    private double sos_longitude;



    // Firestoreのために空のコンストラクタが必要
    public FriendModel() {}



    //userIdを渡していく
    public String getUserId() {
        return userId;
    }

    //userNameを渡していく
    public String getUsername() {
        return username;
    }

    //currentBoardIdを渡していく
    public String getCurrentBoardId() {
        return currentBoardId;
    }

    //evacuatedAtを渡していく
    public String getEvacuatedAt() {
        return evacuatedAt;
    }

    //isSosを渡していく
    public boolean getIsSos() { return isSos; }

    //Urlを渡していく
    public String getIconUrl() {
        return iconUrl;
    }

    //座標を渡していく
    public double getSos_latitude() { return sos_latitude; }
    public double getSos_longitude() { return sos_longitude; }



    // 必要に応じてSetter（値をセットする用）も追加
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public void setCurrentBoardId(String currentBoardId) {
        this.currentBoardId = currentBoardId;
    }

    public void setEvacuatedAt(String evacuatedAt) {
        this.evacuatedAt = evacuatedAt;
    }

    public void setIsSos(boolean isSos) { this.isSos = isSos; }

    public void setIconUrl(String iconUrl) {this.iconUrl = iconUrl;}
    public void setSos_latitude(double lat) { this.sos_latitude = lat; }
    public void setSos_longitude(double lng) { this.sos_longitude = lng; }
}
