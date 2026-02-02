package com.example.sosbaton;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;
import android.view.LayoutInflater;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Toast;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import android.widget.ImageView;

public class friendActivity extends AppCompatActivity {

    private FriendListAdapter adapter; // ★追加：アダプターの宣言


    protected void onCreate(Bundle savedInstanceState) {
        //layout読み込み
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_friend);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // ステータスバーのアイコンを暗い色（黒など）に設定する
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }

        // 親のルートレイアウトを取得するわよ！
        View rootLayout = findViewById(R.id.root_layout);
        if (rootLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(
                        androidx.core.view.WindowInsetsCompat.Type.systemBars()
                );

                // XMLで設定した元のpadding (16dp) を取得して加算するのだ
                float density = getResources().getDisplayMetrics().density;
                int basePaddingPx = (int) (16 * density);

                // 四方のセーフゾーンを考慮してパディングを設定するわよ
                v.setPadding(
                        systemBars.left + basePaddingPx,
                        systemBars.top + basePaddingPx,
                        systemBars.right + basePaddingPx,
                        systemBars.bottom + basePaddingPx
                );

                return insets;
            });
        }



        // ViewModelのインスタンス
        FriendViewModel friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);
        //

        if (MainActivity.getInstance() != null) {
            MainActivity.getInstance().loadSospin();
        }



        //閉じる
        ImageButton close =findViewById(R.id.btnClose);
        close.setOnClickListener(v -> {

            finish();
        });


        // 2. RecyclerViewの設定（★追加）
        setupRecyclerView();

        adapter.setOnFriendClickListener((lat, lng,userId) -> {
            android.content.Intent resultIntent = new android.content.Intent();
            resultIntent.putExtra("zoom_lat", lat);
            resultIntent.putExtra("zoom_lng", lng);
            resultIntent.putExtra("zoom_uid", userId);
            setResult(RESULT_OK, resultIntent); // 結果をセット
            finish(); // 画面を閉じてMainActivityに戻る
        });

        // 3. フレンドリストの監視設定（★追加）
        // Firestoreからデータが届くたびに、ここが自動で実行されます
        friendViewModel.friendList.observe(this, list -> {
            if (list != null) {
                adapter.submitList(list); // アダプターに最新リストを渡す
            }
        });

        // 4. 新規追加時のトースト通知（★追加）
        friendViewModel.newFriendAddedEvent.observe(this, friend -> {
            if (friend != null) {
                Toast.makeText(this, friend.getUsername() + "さんとフレンドになりました！", Toast.LENGTH_SHORT).show();
                friendViewModel.consumeNewFriendEvent(); // 通知済みとしてリセット
            }
        });

        // 5. 監視の開始スイッチ（★追加：これがないとデータが流れてきません）
        friendViewModel.init();




        EditText editText = findViewById(R.id.etFriendUserId);
        //検索ボタン
        Button add = findViewById(R.id.btnAddFriend);

        LayoutInflater inflater = LayoutInflater.from(this);





            // 検索結果待ちobserveで結果監視
            friendViewModel.getSearchResults().observe(this, friends -> {
                //通信がまだ終わっていない
                if (friends == null) return;

                // ヒットしなかった場合
                if (friends.isEmpty()) {
                    //no_hitダイアログを表示
                    new AlertDialog.Builder(this)
                            .setTitle("検索結果")
                            .setMessage("入力されたIDのユーザーは見つかりませんでした。\nIDが正しいか確認してください。")
                            .setPositiveButton("OK", null) // 閉じるだけのボタン
                            .show();
                }else{
                    //ヒットした処理

                    //ダイアログのリセット
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search_user, null);

                    //リストの0を取得だが結果は1件になるのでどっちにしろ
                    FriendModel foundUser = friends.get(0);

                    //usernameのテキストボックス
                    TextView tx_name = dialogView.findViewById(R.id.tvUserName);

                    //useridのテキストボックス
                    TextView tx_id = dialogView.findViewById(R.id.tvUserId);

                    ImageView iv_icon = dialogView.findViewById(R.id.imgIcon);

                    // 取得した情報をテキストボックスにセット
                    if (tx_name != null) {
                        tx_name.setText("名前：" + foundUser.getUsername());
                    }

                    if (tx_id != null) {
                        // 「固定テキスト」 + 「取得した値」
                        tx_id.setText("ID：" + foundUser.getUserId());
                    }

                    if (iv_icon != null) {
                        String iconUrl = foundUser.getIconUrl(); // FriendModelにiconUrlがある前提よ！
                        if (iconUrl != null && !iconUrl.isEmpty()) {
                            com.bumptech.glide.Glide.with(this)
                                    .load(iconUrl)
                                    .circleCrop()
                                    .into(iv_icon);
                        } else {
                            iv_icon.setImageResource(R.drawable.initial_icon_user_);
                        }
                    }




                    //カスタムダイアログ表示
                    new AlertDialog.Builder(this)
                            .setTitle("フレンド申請")
                            .setView(dialogView)
                            .setPositiveButton("追加", (dialog, which) -> {

                                friendViewModel.sendFriendRequest( foundUser.getUserId());


                            })
                            .setNegativeButton("キャンセル", null)
                            .show();
                }

                //結果をリセット
                friendViewModel.clearSearchResult();
            });


        // リクエスト送信の結果を監視
        friendViewModel.getFriendRequestResult().observe(this, result -> {
            if (result == null) return;

            // 1. ステータスに応じてタイトルを切り替える
            String title = (result.getStatus() == FriendRequestResult.Status.SUCCESS)
                    ? "送信完了"
                    : "リクエスト失敗";

            // 2. 標準のAlertDialogを構築
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(result.getMessage()) // Repositoryで設定した詳細メッセージ
                    .setPositiveButton("OK", (dialog, which) -> {
                        // ボタンを押したときにViewModelのフラグをリセット
                        friendViewModel.clearFriendRequestResult();

                        // 成功した時だけ画面を閉じる場合はここに追記
                        if (result.getStatus() == FriendRequestResult.Status.SUCCESS) {
                            // finish();
                        }
                    })
                    .setCancelable(false) // 戻るボタンなどで勝手に閉じないようにする
                    .show();
        });






        // 3. ボタンの中身は「依頼を出すだけ」にする
        add.setOnClickListener(v -> {

            String inputid = editText.getText().toString();
            if (!inputid.isEmpty()) {
                friendViewModel.findFriend(inputid); // 依頼を投げるだけ！
            }
        });


    }
    // ★追加：RecyclerViewの初期化メソッド
    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rvFriendList); // XMLのRecyclerViewのIDに合わせてください
        adapter = new FriendListAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
