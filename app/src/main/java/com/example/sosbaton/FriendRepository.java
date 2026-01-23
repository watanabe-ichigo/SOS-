package com.example.sosbaton;

import java.util.List;
import java.util.ArrayList;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
// Firebase関連（Task, getResult()）
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

// FirestoreやRealtime Databaseの結果取得に使う場合
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

// Taskの結果ステータスなどを扱う場合（Statusが何かによる）
//import com.google.android.gms.common.api.Status;

// Optional: 非同期処理で使うことがある
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.TaskCompletionSource;

import com.google.firebase.auth.FirebaseAuth;

public class FriendRepository {
    //フレンドリストのFirestoreとの通信を担うクラス

    // 検索結果を返すためのインターフェース（コールバック）
    public interface SearchCallback {
        void onComplete(List<FriendModel> friends);
    }

    //userの検索ロジック
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


    // フレンドリクエスト送信ロジック
    public void processFriendRequest(String myUid, String targetUid, Consumer<FriendRequestResult> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("users").document(targetUid).get().addOnSuccessListener(userDoc -> {


            //相手が存在するかチェック
            if (!userDoc.exists()) {
                callback.accept(new FriendRequestResult(FriendRequestResult.Status.NOT_FOUND, "ユーザーが見つかりません"));
                return;
            }


            db.collection("users").document(myUid).collection("friend_list").document(targetUid).get()
                    .addOnSuccessListener(friendDoc -> {
                        //すでにフレンドかチェック
                        if (friendDoc.exists()) {
                            callback.accept(new FriendRequestResult(FriendRequestResult.Status.ALREADY_FRIEND, "すでにフレンドです"));
                            return;
                        }

                        // すでに「申請中（pending）」のデータがないか探す
                        db.collection("friend_requests")
                                .whereEqualTo("from", myUid)
                                .whereEqualTo("to", targetUid)
                                .whereEqualTo("status", "pending")
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        callback.accept(new FriendRequestResult(FriendRequestResult.Status.ALREADY_REQUESTED, "すでに申請済みです"));
                                    } else {

                                        // 自分のデータと相手のデータを同時に取りに行く
                                        Task<DocumentSnapshot> myTask = db.collection("users").document(myUid).get();
                                        Task<DocumentSnapshot> targetTask = db.collection("users").document(targetUid).get();

                                        Tasks.whenAll(myTask, targetTask).addOnSuccessListener(v -> {
                                            DocumentSnapshot myDoc = myTask.getResult();
                                            DocumentSnapshot targetDoc = targetTask.getResult();

                                            if (!targetDoc.exists()) {
                                                callback.accept(new FriendRequestResult(FriendRequestResult.Status.NOT_FOUND, "相手がいません"));
                                                return;
                                            }

                                            // ここで両方の名前が手に入る！
                                            String myName = myDoc.getString("username");
                                            String targetName = targetDoc.getString("username");

                                            // 3. 申請送信
                                            Map<String, Object> request = new HashMap<>();
                                            request.put("from_id", myUid);
                                            request.put("from_name", myName);
                                            request.put("to_id", targetUid);
                                            request.put("to_name", targetName);
                                            request.put("status", "pending");
                                            request.put("created_at", FieldValue.serverTimestamp());


                                            db.collection("friend_requests").add(request)
                                                    .addOnSuccessListener(doc -> callback.accept(new FriendRequestResult(FriendRequestResult.Status.SUCCESS, "申請を送信しました")))
                                                    .addOnFailureListener(e -> callback.accept(new FriendRequestResult(FriendRequestResult.Status.ERROR, "通信失敗")));
                                        });

                                    }
                                });


                    });
        });
    }


    // 結果を流し続けるメソッド（フレンドリストの監視）
    // 戻り値を ListenerRegistration に変更するだけ！
    public com.google.firebase.firestore.ListenerRegistration observeFriendList(String myUid, Consumer<List<FriendModel>> onUpdate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        return db.collection("users").document(myUid).collection("friend_list")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    List<FriendModel> updatedList = snapshots.toObjects(FriendModel.class);
                    onUpdate.accept(updatedList);
                });
    }



}
