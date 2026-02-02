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
}
