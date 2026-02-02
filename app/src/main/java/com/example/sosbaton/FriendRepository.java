package com.example.sosbaton;

import java.util.List;
import java.util.ArrayList;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
// Firebase関連（Task, getResult()）
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

// FirestoreやRealtime Databaseの結果取得に使う場合

// Taskの結果ステータスなどを扱う場合（Statusが何かによる）
//import com.google.android.gms.common.api.Status;

// Optional: 非同期処理で使うことがある


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
    public com.google.firebase.firestore.ListenerRegistration observeFriendListWithDetails(String myUid, OnFriendsReadyListener listener) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        return db.collection("users").document(myUid).collection("friend_list")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    List<DocumentSnapshot> friendDocs = snapshots.getDocuments();
                    if (friendDocs.isEmpty()) {
                        listener.onReady(new ArrayList<>());
                        return;
                    }

                    List<FriendModel> fullInfoList = new ArrayList<>();
                    // ✅ 修正ポイント：AtomicIntegerを使ってカウントを安全に行う
                    java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(0);
                    int totalFriends = friendDocs.size();

                    for (DocumentSnapshot doc : friendDocs) {
                        String friendId = doc.getId();

                        db.collection("users").document(friendId).get()
                                .addOnSuccessListener(userProfile -> {
                                    if (userProfile.exists()) {
                                        FriendModel friend = new FriendModel();
                                        friend.setUserId(friendId);
                                        friend.setUsername(userProfile.getString("username"));
                                        // ★ ここに isSos の読み取りを追加！
                                        Boolean isSos = userProfile.getBoolean("isSos");
                                        friend.setIsSos(isSos != null && isSos); // nullチェックしつつセット
                                        String iconUrl = userProfile.getString("iconUrl");
                                        friend.setIconUrl(iconUrl != null ? iconUrl : "");                                        String shelterId = userProfile.getString("currentBoardId");
                                        com.google.firebase.Timestamp ts = userProfile.getTimestamp("evacuationTime");
                                        if (ts != null) {
                                            long diffSeconds = com.google.firebase.Timestamp.now().getSeconds() - ts.getSeconds();
                                            String timeLabel;

                                            if (diffSeconds < 60) {
                                                timeLabel = "たった今";
                                            } else if (diffSeconds < 3600) {
                                                timeLabel = (diffSeconds / 60) + "分前";
                                            } else if (diffSeconds < 86400) {
                                                timeLabel = (diffSeconds / 3600) + "時間前";
                                            } else {
                                                // 1日以上経った場合は日付を表示
                                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.JAPAN);
                                                timeLabel = sdf.format(ts.toDate());
                                            }
                                            friend.setEvacuatedAt(timeLabel); // 計算済みの文字列をセット
                                        } else {
                                            friend.setEvacuatedAt("不明");
                                        }

                                        if (shelterId != null && !shelterId.isEmpty()) {
                                            db.collection("shelters").document(shelterId).get()
                                                    .addOnSuccessListener(shelterDoc -> {
                                                        // 避難所名を取得してセット
                                                        friend.setCurrentBoardId(shelterDoc.exists() ? shelterDoc.getString("name") : "不明な避難所");



                                                        // ✅ カウントを進めてチェック
                                                        fullInfoList.add(friend);
                                                        if (counter.incrementAndGet() == totalFriends) {
                                                            listener.onReady(fullInfoList);
                                                        }
                                                    })
                                                    .addOnFailureListener(err -> {
                                                        friend.setCurrentBoardId("情報取得エラー");
                                                        fullInfoList.add(friend);
                                                        if (counter.incrementAndGet() == totalFriends) listener.onReady(fullInfoList);
                                                    });
                                        } else {
                                            friend.setCurrentBoardId("未避難");
                                            fullInfoList.add(friend);
                                            if (counter.incrementAndGet() == totalFriends) listener.onReady(fullInfoList);
                                        }
                                    } else {
                                        // ユーザー自体がいなかった場合もカウントは進める
                                        if (counter.incrementAndGet() == totalFriends) {
                                            listener.onReady(fullInfoList);
                                        }
                                    }
                                });
                    }
                });
    }

    // 完了チェック用の補助メソッド（重複コードを避けるため）
    private void checkAndFinish(List<FriendModel> list, FriendModel friend, int total, OnFriendsReadyListener listener) {
        list.add(friend);
        if (list.size() == total) {
            listener.onReady(list);
        }
    }

    //インタフェース:フレンド一覧の準備が完了したら呼ばれる
    public interface OnFriendsReadyListener {
        void onReady(List<FriendModel> friends);
    }



}
