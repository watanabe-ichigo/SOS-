package com.example.sosbaton;

public class FriendModel {
      //フレンドリストのデータ保持クラス

    //検索結果表示用のuserid
    private String userId;

    //検索結果表示用のusername
    private String username;

    // Firestoreのために空のコンストラクタが必要
    public FriendModel() {}

    //userIdに値が代入されていない場合
    public String getUserId() {
        return userId;
    }

    //userNameに値が代入されていない場合
    public String getUserName() {
        return username;
    }

    // 必要に応じてSetter（値をセットする用）も追加
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }
}
