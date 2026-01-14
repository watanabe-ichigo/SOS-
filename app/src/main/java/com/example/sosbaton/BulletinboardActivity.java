package com.example.sosbaton;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import java.util.Map;
import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.Timestamp;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.List;
import java.util.ArrayList;
import com.example.sosbaton.MessageAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.lifecycle.ViewModelProvider;





public class BulletinboardActivity extends AppCompatActivity {
   //選択ピンのdocIDをmainから取得用
    private String newPinDocId;

    //選択ピンのnameをmainから取得用
    private String pinname;
    //選択ピンの座標をmainから取得用
    private LatLng pinlatlng;

    //usernameをmainから取得用
    private String my_username;

    //ログのタグ付け
    private static final String TAG = "Firestore";

    //データベース取得
    private FirebaseFirestore db  = FirebaseFirestore.getInstance();

    //削除用
    public String messageId;

    //全メッセージ管理リスト(Adapterクラスも参照する)
    private List<Message> messageList = new ArrayList<>();

    //Adapterクラスのインスタンスを保持する変数のフィールド
    private MessageAdapter adapter;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletinboard);

        RecyclerView rv = findViewById(R.id.rvEvacuationList);

        //osに殺されない記憶するやつ
        BoardViewModel viewModel = new ViewModelProvider(this).get(BoardViewModel.class);
        viewModel.getRegistrationResult().observe(this, caseType -> {
            switch (caseType) {
                case FIRST_REGISTRATION:
                    Toast.makeText(this, "避難登録完了", Toast.LENGTH_SHORT).show();
                    break;
                case BOARD_UPDATED_Area:
                    Toast.makeText(this, "避難場所を更新しました", Toast.LENGTH_SHORT).show();
                    break;
                case FAILURE:
                    Toast.makeText(this, "登録に失敗しました", Toast.LENGTH_SHORT).show();
                    break;
                case BOARD_UPDATED_Info:
                    Toast.makeText(this,"避難情報を更新しました",Toast.LENGTH_SHORT).show();

            }
        });



        newPinDocId = getIntent().getStringExtra("PIN_DOC_ID");
        pinname  = getIntent().getStringExtra("PIN_NAME");
        my_username =getIntent().getStringExtra("my_user_name");
        LatLng pinLatLng = getIntent().getParcelableExtra("PIN_LAT_LNG");
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        adapter = new MessageAdapter(messageList, newPinDocId);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        View root = findViewById(R.id.root_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });


        if (newPinDocId == null) {
            finish();
            Log.d(TAG, "docIDなし");
            return;
        }

        if(newPinDocId!= null){
            Log.d(TAG, "ドキュメント入っているよ");
        }

        if (pinname != null && !pinname.isEmpty()) {
            createboardDocument(newPinDocId,pinname);
        }

        TextView tvBoardTitle = findViewById(R.id.tvShelterName);
        tvBoardTitle.setText(pinname);



        ImageButton btnClose = findViewById(R.id.btnClose);
        Button btn_evacuation= findViewById(R.id.btnConfirm);
        Button btn_delete    = findViewById(R.id.btnCancel);
        //Xボタン
        btnClose.setOnClickListener(v->{
            finish();
        });

        //避難完了ボタン
        btn_evacuation.setOnClickListener(v->{

            String userId = FirebaseAuth.getInstance().getUid();
            viewModel.registerEvacuationUser(userId, my_username, newPinDocId);

        });

        //取り消しボタン
        btn_delete.setOnClickListener(v->{
            deleteAllMyMessages(newPinDocId);


        });

        //投稿ロード関数呼び出し
        loadBoardMessages(newPinDocId);


    }

    //避難所のdocIDを掲示板のdocIDとして避難所掲示板ドキュメント作成関数
    private void createboardDocument(String newPinDocId,String pinname) {

        Map<String, Object> data = new HashMap<>();
        data.put("pinname", pinname);
        data.put("pinDocId",newPinDocId);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("shelter_board")
                .document(newPinDocId)   // ← ここが docId
                .set(data)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "ドキュメント作成成功: " + pinname)
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "作成失敗")
                );
    }

    //避難user情報登録関数
    private void registerEvacuationUser(String my_username, String newPinDocId) {

        String userId = FirebaseAuth.getInstance().getUid();
        Log.d("AUTH", "uid = " + userId);
        Log.d("EVAC", "newPinDocId = " + newPinDocId);

        //ログインチェック
        if (userId == null){
            Log.d(TAG, "ull user");
            return;
        }

        db = FirebaseFirestore.getInstance();

        //ログインuser保存
        DocumentReference userStateRef = db.collection("users").document(userId);
        userStateRef.get()
                .addOnSuccessListener(snapshot -> {
                    Log.d("FIRE", "できた");
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRE", "だめだった", e);
                });


        userStateRef.get().addOnSuccessListener(snapshot -> {


            //過去の避難場所ID保存
            String oldPinDocId = snapshot.getString("currentBoardId");

            WriteBatch batch = db.batch();

            Log.d(TAG, "通過確認 ");
            // 旧掲示板が存在していて、別の避難所なら削除
            if (oldPinDocId != null && !oldPinDocId.equals(newPinDocId)) {
                DocumentReference oldMsgRef = db.collection("boards")
                        .document(oldPinDocId)
                        .collection("messages")
                        .document(userId);
                batch.delete(oldMsgRef);
            }
            Log.d(TAG, "通過完了");

            // 新掲示板へ登録
            DocumentReference newMsgRef = db.collection("boards")
                    .document(newPinDocId)
                    .collection("messages")
                    .document(userId);

            Map<String, Object> msg = new HashMap<>();
            msg.put("userId", userId);
            msg.put("userName", my_username);
            msg.put("text", "避難完了しました");
            msg.put("updatedAt", Timestamp.now());

            batch.set(newMsgRef, msg);
            // user の現在避難所更新
            Map<String, Object> userState = new HashMap<>();
            userState.put("currentBoardId", newPinDocId);
            batch.set(userStateRef, userState, SetOptions.merge());

            batch.commit()
                    .addOnSuccessListener(v -> {
                        Log.e("BATCH", "できた");

                if (oldPinDocId == null) {
                    Toast.makeText(this, "避難登録完了", Toast.LENGTH_SHORT).show();
                } else if (!oldPinDocId.equals(newPinDocId)) {
                    Toast.makeText(this, "避難場所を更新しました", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "避難情報を更新しました", Toast.LENGTH_SHORT).show();
                }

            })
                    .addOnFailureListener(e -> {
                        Log.e("BATCH", "だめ", e);
                    });


        });


    }

    //投稿削除関数
    private void deleteAllMyMessages(String newPinDocId) {

        String myId = FirebaseAuth.getInstance().getUid();
        if (myId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference messagesRef = db
                .collection("boards")
                .document(newPinDocId)
                .collection("messages");

        messagesRef.whereEqualTo("userId", myId).get()
                .addOnSuccessListener(query -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this, "自分の投稿をすべて削除しました", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "削除に失敗しました", Toast.LENGTH_SHORT).show()
                            );
                });
    }


    //掲示板の投稿ロード関数
    private void loadBoardMessages(String newPinDocId) {

        if (newPinDocId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("boards")
                .document(newPinDocId)
                .collection("messages")
                .orderBy("createdAt")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "メッセージ取得エラー", e);
                        return;
                    }
                    if (snapshots == null) return;

                    // リストをクリアしてから追加
                    messageList.clear();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Message m = new Message();
                        m.messageId = doc.getId();
                        m.userId = doc.getString("userId");
                        m.userName = doc.getString("userName");
                        m.text = doc.getString("text");
                        m.createdAt =doc.getTimestamp("createdAt");

                        messageList.add(m);
                    }

                    adapter.notifyDataSetChanged();
                });

    }









}
