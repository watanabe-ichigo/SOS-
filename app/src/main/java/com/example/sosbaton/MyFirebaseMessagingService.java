package com.example.sosbaton;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
                Toast.makeText(getApplicationContext(), "通知が届きました！通知を確認してください！", Toast.LENGTH_SHORT).show());

        String title = "SOSbaton通知";
        String message = "メッセージが届きました。";

        // データが含まれていればそれを使う
        if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            message = remoteMessage.getData().get("body");
        }

        sendNotification(title, message);
    }

    private void sendNotification(String title, String message) {

        Intent intent = new Intent(this, friendActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round) // ※必ず「白一色の透過アイコン」を用意すること！
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX) // 最高優先度
                .setDefaults(NotificationCompat.DEFAULT_ALL) // 音、振動、ライトすべてデフォルトを使う
                .setOnlyAlertOnce(false) // ← ここを false にすると、上書き時でも再度音が鳴る
                .setCategory(NotificationCompat.CATEGORY_ALARM) // アラーム扱いにする
                .setFullScreenIntent(pendingIntent, true) // ★これを入れると強制的にポップアップする
                .setWhen(System.currentTimeMillis())      // ★通知時間を「今」にする
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);



        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            // 【修正】マイナスにならないように下位31ビットだけ使う工夫
            int uniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
            manager.notify(uniqueId, builder.build());
        }
    }
}
