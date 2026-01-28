package com.example.sosbaton;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // ログレベルを "ERROR" にして目立たせる
        android.util.Log.e("FCM_DEBUG", "★★ Serviceが呼ばれました！！ ★★");

        // デバッグ用トースト（低スペック端末ではこれが出るかが勝負）
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getApplicationContext(), "受信！", Toast.LENGTH_SHORT).show());

        String title = "避難指示";
        String message = "直ちに避難してください";

        // データが含まれていればそれを使う
        if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            message = remoteMessage.getData().get("body");
        }

        sendNotification(title, message);
    }

    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round) // ※必ず「白一色の透過アイコン」を用意すること！
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX) // 最高優先度
                .setCategory(NotificationCompat.CATEGORY_ALARM) // アラーム扱いにする
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
