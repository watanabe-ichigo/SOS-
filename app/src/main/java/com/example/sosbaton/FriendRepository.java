package com.example.sosbaton;

import java.util.List;
import java.util.ArrayList;
import com.google.firebase.firestore.FirebaseFirestore;

public class FriendRepository {
    //フレンドリストのFirestoreとの通信を担うクラス

    // 検索結果を返すためのインターフェース（コールバック）
    public interface SearchCallback {
        void onComplete(List<FriendModel> friends);
    }

    public void searchUserByName(String id, SearchCallback callback) {

        //ファイアストアのインスタンス
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Firestoreに「データちょうだい」とリクエストを送る（非同期）
        db.collection("users")
                .whereEqualTo(com.google.firebase.firestore.FieldPath.documentId(), id)
                .get()
                .addOnCompleteListener(task -> {
                    List<FriendModel> friends = new ArrayList<>();

                    if (task.isSuccessful() && task.getResult() != null) {
                        // 成功したらリストを作る
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            friends.add(document.toObject(FriendModel.class));
                        }
                    }

                    // ★ここで引数の callback に結果をセットして実行する！
                    // これにより、このメソッドを呼んだ側（ViewModel）にデータが届く
                    callback.onComplete(friends);
                });

    }



}
