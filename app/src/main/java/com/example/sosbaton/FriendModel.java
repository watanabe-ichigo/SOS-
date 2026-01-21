package com.example.sosbaton;

public class FriendModel {
      //フレンドリストのデータ保持クラス

    //検索結果表示用のuserid
    public String userId;

    //検索結果表示用のusername
    public String userName;

    // Firestoreのために空のコンストラクタが必要
    public FriendModel() {}

    //userIdに値が代入されていない場合
    public String getUserId() {
        return userId;
    }

    //userNameに値が代入されていない場合
    public String getUserName() {
        return userName;
    }

    // 必要に応じてSetter（値をセットする用）も追加
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
