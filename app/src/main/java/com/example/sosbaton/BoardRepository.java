package com.example.sosbaton;
// 🔹 Firebase Firestore 関連
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;

// 🔹 Java コレクション
import java.util.Map;
import java.util.HashMap;

// 🔹 Firestore データ型
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.SetOptions;

//Firestore 操作を集約するクラス
public class BoardRepository {

    //非同期完了処理で通知
    public interface Callback {
        void onComplete(CaseType resultCase);
    }

    public enum CaseType {
        FIRST_REGISTRATION,   // 初投稿
        BOARD_UPDATED_Area,        // 避難場所変更
        BOARD_UPDATED_Info,//避難情報更新
        FAILURE               // 登録失敗

    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 避難登録・更新処理
    public void registerEvacuation(String userId, String userName, String newPinDocId, Callback callback
                                   ) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userStateRef = db.collection("users").document(userId);

        userStateRef.get().addOnSuccessListener(snapshot -> {
            String oldPinDocId = snapshot.getString("currentBoardId");
            WriteBatch batch = db.batch();

            // 避難場所が変更されていれば前の投稿を削除
            if (oldPinDocId != null && !oldPinDocId.equals(newPinDocId)) {
                DocumentReference oldMsgRef = db.collection("boards")
                        .document(oldPinDocId)
                        .collection("messages")
                        .document(userId);
                batch.delete(oldMsgRef);
            }

            // 新掲示板に投稿
            DocumentReference newMsgRef = db.collection("boards")
                    .document(newPinDocId)
                    .collection("messages")
                    .document(userId);

            Map<String, Object> msg = new HashMap<>();
            msg.put("userId", userId);
            msg.put("userName", userName);
            msg.put("text", "避難完了しました");
            msg.put("createdAt", Timestamp.now());

            batch.set(newMsgRef, msg);

            // ユーザー情報の更新
            Map<String, Object> userState = new HashMap<>();
            userState.put("currentBoardId", newPinDocId);
            batch.set(userStateRef, userState, SetOptions.merge());

            // コミット
            batch.commit()
                    .addOnSuccessListener(v -> {
                        if (oldPinDocId == null) {
                            callback.onComplete(CaseType.FIRST_REGISTRATION);
                        } else if (!oldPinDocId.equals(newPinDocId)) {
                            callback.onComplete(CaseType.BOARD_UPDATED_Area);
                        }
                        else{
                            callback.onComplete(CaseType.BOARD_UPDATED_Info);
                        }

                    })
                    .addOnFailureListener(e -> {
                        callback.onComplete(CaseType.FAILURE); // 失敗通知
                    });

        }).addOnFailureListener(e -> {
            callback.onComplete(CaseType.FAILURE); // ユーザー情報取得失敗も含む
        });
    }
}
