package com.example.sosbaton;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.ImageView;
import android.widget.EditText;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage; // storage = FirebaseStorage.getInstance(); のため
import com.google.firebase.storage.StorageReference; // fileRef の型定義のため
import com.google.firebase.firestore.FieldValue; // FieldValue.delete() のため
import com.bumptech.glide.Glide; // Glide.with(this).load(url).into(imageUserIcon); のため
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;




public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView tvUserNameTop, tvValueName, tvValueEmail, tvValuePassword,tvValueuserId;

    private FirebaseStorage storage;
    private ImageView imageUserIcon;
    private final ActivityResultLauncher<CropImageContractOptions> cropImageLauncher =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful()) {
                    Uri resultUri = result.getUriContent();
                    if (resultUri != null) {
                        uploadImageToStorage(resultUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // 戻る
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // TextView
        tvUserNameTop = findViewById(R.id.tvUserNameTop);
        tvValueName = findViewById(R.id.tvValueName);
        tvValueEmail = findViewById(R.id.tvValueEmail);
        tvValuePassword = findViewById(R.id.tvValuePassword);
         tvValueuserId = findViewById(R.id.tvValueUserId);

        // Edit ボタン
        ImageView btnEditName = findViewById(R.id.btnEditName);
        ImageView btnEditEmail = findViewById(R.id.btnEditEmail);
        ImageView btnEditPassword = findViewById(R.id.btnEditPassword);

        btnEditName.setOnClickListener(v -> showEditNameDialog());
        btnEditEmail.setOnClickListener(v -> showEditEmailDialog());
        btnEditPassword.setOnClickListener(v -> showEditPasswordDialog());

        imageUserIcon = findViewById(R.id.imageUserIcon);
        imageUserIcon.setOnClickListener(v -> showIconOptionsDialog());

        // ユーザー情報読み込み
        loadUserInfo();

        //共有ボタン
        Button btnShare = findViewById(R.id.btnShareId);

        btnShare.setOnClickListener(v->{

            shareText(this, getUid());


        });
    }

    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String email = user.getEmail();
        String uid = user.getUid();

        tvValueEmail.setText(email);
        tvValuePassword.setText("●●●●●●●●");

        if (tvValueuserId != null) {
            tvValueuserId.setText(uid);
        }

        if (tvValueuserId != null) {
            tvValueuserId.setOnClickListener(v -> {
                String idToCopy = tvValueuserId.getText().toString();

                android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                        getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("UserID", idToCopy);
                clipboard.setPrimaryClip(clip);

                com.google.android.material.snackbar.Snackbar snackbar =
                        com.google.android.material.snackbar.Snackbar.make(v, "IDをコピーしました", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT);

                // 2. Viewを取得
                View snackbarView = snackbar.getView();

                // 3. レイアウトパラメータを FrameLayout.LayoutParams として取得し、位置を上に設定
                // ※Snackbarの内部構造を利用したハック的な方法です
                android.view.ViewGroup.LayoutParams lp = snackbarView.getLayoutParams();
                if (lp instanceof android.widget.FrameLayout.LayoutParams) {
                    android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) lp;
                    params.gravity = android.view.Gravity.TOP; // ここで上部を指定
                    params.topMargin = 150;                   // 上からのマージン
                    snackbarView.setLayoutParams(params);
                }

                snackbar.show();
            });
        }

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("username")) {
                        String name = doc.getString("username");
                        tvUserNameTop.setText(name + " さん");
                        tvValueName.setText(name);
                    }
                    if (doc.contains("iconUrl")) {
                        String iconUrl = doc.getString("iconUrl");
                        if (iconUrl != null && !iconUrl.isEmpty()) {
                            // Glideなどのライブラリを使って画像をロードするのだ
                            // (Glideを使用している場合の例なのだ)
                            Glide.with(this).load(iconUrl).into(imageUserIcon);
                        } else {
                            // URLがない場合はデフォルトアイコンに戻すのだ
                            imageUserIcon.setImageResource(R.drawable.initial_icon_user_);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "ユーザー情報取得失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    // ① 名前変更ダイアログ
    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    private void showEditNameDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("名前を変更");

        final EditText input = new EditText(this);
        input.setText(tvValueName.getText().toString());
        builder.setView(input);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "名前を入力してね", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(uid)
                    .update("username", newName)
                    .addOnSuccessListener(aVoid -> {
                        tvValueName.setText(newName);
                        tvUserNameTop.setText(newName + " さん");
                        Toast.makeText(this, "更新しました", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "更新失敗 " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("キャンセル", (d, w) -> d.cancel());
        builder.show();
    }

    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    // ② メール変更ダイアログ（再認証 + verifyBeforeUpdateEmail）
    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    private void showEditEmailDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("メールを変更");

        View view = getLayoutInflater().inflate(R.layout.dialog_change_email, null);
        EditText etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        EditText etNewEmail = view.findViewById(R.id.etNewEmail);

        builder.setView(view);

        builder.setPositiveButton("変更", (dialog, which) -> {

            String currentPassword = etCurrentPassword.getText().toString();
            String newEmail = etNewEmail.getText().toString().trim();

            if (currentPassword.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "必要な項目を入力してね", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔐 再認証
            AuthCredential credential = EmailAuthProvider
                    .getCredential(user.getEmail(), currentPassword);

            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {

                        // ★ Firebase 正攻法：まず確認メールを送らせる
                        user.verifyBeforeUpdateEmail(newEmail)
                                .addOnSuccessListener(v -> {

                                    // UI 上はとりあえず新しいメール表示だけ更新しておく
                                    tvValueEmail.setText(newEmail);

                                    Toast.makeText(this,
                                            "新しいメールに確認リンク送ったのだ。"
                                                    + "リンク踏んだらメール変更が確定するのだぞ！",
                                            Toast.LENGTH_LONG).show();

                                })
                                .addOnFailureListener(e -> {
                                    Log.e("EMAIL_UPDATE", "verifyBeforeUpdateEmail失敗", e);
                                    Toast.makeText(this,
                                            "メール送信失敗: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "パスワードが違います", Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("キャンセル", (d, w) -> d.cancel());
        builder.show();
    }




    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    // ③ パスワード変更ダイアログ（再認証付き）
    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    private void showEditPasswordDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("パスワード変更");

        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = view.findViewById(R.id.etNewPassword);

        builder.setView(view);

        builder.setPositiveButton("変更", (dialog, which) -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();

            if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "必要な項目を入力してね", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider
                    .getCredential(user.getEmail(), currentPassword);

            // 再認証
            user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
                user.updatePassword(newPassword)
                        .addOnSuccessListener(v ->
                                Toast.makeText(this, "パスワードを変更しました", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "変更失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "現在のパスワードが違います", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("キャンセル", (d, w) -> d.cancel());
        builder.show();
    }
    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    // ❹ アイコン操作ダイアログ
    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    private void showIconOptionsDialog() {
        // 画像URLが設定されているか（FirestoreのユーザーデータにiconUrlフィールドがあるか）をチェック
        // loadUserInfoでiconUrlが読み込まれている前提で、Firestoreから改めて取得するのが確実なのだ
        db.collection("users").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(doc -> {
                    String currentIconUrl = doc.getString("iconUrl");

                    // アイコンが設定済みかどうかに応じて選択肢を変えるのだ
                    String[] options;
                    if (currentIconUrl != null && !currentIconUrl.isEmpty()) {
                        // 既にアイコンがある場合: 変更、削除、キャンセルの3択
                        options = new String[]{"新しいアイコンを選択", "アイコンを削除", "キャンセル"};
                    } else {
                        // アイコンがない場合: 設定、キャンセルの2択
                        options = new String[]{"アイコンを設定", "キャンセル"};
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("アイコンの操作");

                    builder.setItems(options, (dialog, which) -> {
                        if (options[which].equals("新しいアイコンを選択") || options[which].equals("アイコンを設定")) {
                            // ギャラリーから画像を選択するのだ
                            selectImage();
                        } else if (options[which].equals("アイコンを削除")) {
                            // 画像を削除するのだ
                            deleteIcon();
                        } else if (options[which].equals("キャンセル")) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                });
    }

    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    // ❺ ギャラリーから画像を選択 (修正後)
    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    private void selectImage() {
        // ライブラリの設定を作るのだ
        CropImageOptions cropOptions = new CropImageOptions();
        cropOptions.guidelines = CropImageView.Guidelines.ON; // ガイドラインを表示
        cropOptions.cropShape = CropImageView.CropShape.OVAL; // 丸型のガイド
        cropOptions.fixAspectRatio = true; // 正方形に固定
        cropOptions.aspectRatioX = 1;
        cropOptions.aspectRatioY = 1;

        // ボタンや背景の色をハッキリさせる設定なのだ（これで見えるようになるはず！）
        cropOptions.activityMenuIconColor = android.graphics.Color.WHITE; // 決定ボタン（✓）を白に
        cropOptions.toolbarColor = android.graphics.Color.BLACK;          // バーを黒に
        cropOptions.activityTitle = "トリミング";                // タイトルも一応つけておくわ
        // ------------------------------

        // 【重要】アクションバーがないテーマでも、強制的にツールバーを表示させるのだ！
        cropOptions.showProgressBar = true; // ついでにプログレスバーも出すわ
        // ------------------------------

        // さっき定義した cropImageLauncher を使うのだ！
        cropImageLauncher.launch(new CropImageContractOptions(null, cropOptions));
    }

    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    // ❼ Firebase Storageに画像をアップロード
    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    private void uploadImageToStorage(Uri imageUri) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        // Storageの参照を作成するのだ (例: users/UID/profile_icon.jpg)
        StorageReference fileRef = storage.getReference()
                .child("users/" + user.getUid() + "/profile_icon.jpg");

        // アップロードを実行するのだ
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // アップロード成功後、画像のダウンロードURLを取得するのだ
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // 取得したURLをFirestoreとFirebase Authのプロフィールに保存するのだ
                        saveIconUrl(imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "アップロード失敗: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("STORAGE_UPLOAD", "アップロード失敗", e);
                });
    }

    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    // ❽ アイコンURLをFirestoreに保存
    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    private void saveIconUrl(String url) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        // FirestoreのユーザーデータにURLを保存するのだ
        db.collection("users").document(user.getUid())
                .update("iconUrl", url)
                .addOnSuccessListener(aVoid -> {
                    // 成功したらImageViewを更新するのだ
                    // Glideなどのライブラリを使用
                    Glide.with(this).load(url).circleCrop().into(imageUserIcon);
                    Toast.makeText(this, "アイコンを更新しました", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "URL保存失敗: " + e.getMessage(), Toast.LENGTH_LONG).show());

        // ちなみに、Firebase Authのプロフィール（photoUrl）にも保存できるのだ
        // UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
        //         .setPhotoUri(Uri.parse(url))
        //         .build();
        // user.updateProfile(profileUpdates);
    }

    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    // ❾ アイコンの削除
    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    private void deleteIcon() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        // 1. Storageから画像を削除
        StorageReference fileRef = storage.getReference()
                .child("users/" + user.getUid() + "/profile_icon.jpg");

        fileRef.delete().addOnSuccessListener(aVoid -> {
            // 2. FirestoreからURLを削除
            db.collection("users").document(user.getUid())
                    .update("iconUrl", FieldValue.delete()) // フィールドを削除
                    .addOnSuccessListener(task -> {
                        // 3. UIをデフォルトに戻す
                        imageUserIcon.setImageResource(R.drawable.initial_icon_user_);
                        Toast.makeText(this, "アイコンを削除しました", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "FirestoreのURL削除失敗: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }).addOnFailureListener(e -> {
            // 画像が存在しなかった場合も成功とみなすことがあるので、エラー処理は控えめにするのだ
            Log.e("STORAGE_DELETE", "Storageからの削除失敗 (ファイルが存在しない可能性): " + e.getMessage());
            // Storageから削除できなくてもFirestoreのURLだけでも消しておくのだ
            db.collection("users").document(user.getUid())
                    .update("iconUrl", FieldValue.delete())
                    .addOnSuccessListener(task -> {
                        imageUserIcon.setImageResource(R.drawable.initial_icon_user_);
                        Toast.makeText(this, "アイコンを削除しました", Toast.LENGTH_SHORT).show();
                    });
        });
    }


    //共有メソッド
    private void shareText(Context context, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);

        context.startActivity(
                Intent.createChooser(intent, "共有先を選択")
        );
    }


    private String getUid() {
        return FirebaseAuth.getInstance().getUid();
    }


}
