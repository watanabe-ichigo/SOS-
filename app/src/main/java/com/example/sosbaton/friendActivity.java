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
import android.widget.Toast;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;


public class friendActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        //layout読み込み
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_friend);

        // ViewModelのインスタンス
        FriendViewModel friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);




        //閉じる
        ImageButton close =findViewById(R.id.btnClose);
        close.setOnClickListener(v -> {
            finish();
        });



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

                    //取得した情報をテキストボックスにセット
                    if (tx_name != null) tx_name.setText(foundUser.getUserName());
                    if (tx_id != null) tx_id.setText(foundUser.getUserId());


                    //カスタムダイアログ表示
                    new AlertDialog.Builder(this)
                            .setTitle("フレンド申請")
                            .setView(dialogView)
                            .setPositiveButton("追加", (dialog, which) -> {
                            })
                            .setNegativeButton("キャンセル", null)
                            .show();
                }

                //結果をリセット
                friendViewModel.clearSearchResult();
            });






        // 3. ボタンの中身は「依頼を出すだけ」にする
        add.setOnClickListener(v -> {

            String inputid = editText.getText().toString();
            if (!inputid.isEmpty()) {
                friendViewModel.findFriend(inputid); // 依頼を投げるだけ！
            }
        });





    }
}
