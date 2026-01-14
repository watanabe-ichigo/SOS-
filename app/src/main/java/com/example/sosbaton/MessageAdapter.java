package com.example.sosbaton;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import com.google.firebase.Timestamp;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    //BulletinboardActivityからの全メッセージ管理リスト参照
    private List<Message> messageList;

    //コンストラクタで受け取ったdocIdの保存
    private String pinDocId;

    public MessageAdapter(List<Message> messageList, String pinDocId) {
        this.messageList = messageList;
        this.pinDocId = pinDocId;
    }

    //メソッドがnullにならない保障
    @NonNull
    @Override
    //自作XMLをRecyclerViewに渡す＆viewholderにXMLの保存関数
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);

        return new MessageViewHolder(v);
    }

    //リストから情報を取り出してviewに反映する関数＆削除ボタン
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        //messageクラスのオブジェクトが入っているためusernameなど参照可
        Message msg = messageList.get(position);

        holder.tvMessage.setText(msg.userName + " : " + msg.text);
        holder.tvTime.setText(getRelativeTime(msg.createdAt));



        String myId = FirebaseAuth.getInstance().getUid();

        //削除ボタン(if)自分ではない投稿ボタン非表示(else)my投稿削除ロジック
        if (!msg.userId.equals(myId)) {
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnDelete.setOnClickListener(v -> {
                FirebaseFirestore.getInstance()
                        .collection("boards")
                        .document(pinDocId)
                        .collection("messages")
                        .document(msg.messageId)
                        .delete();
            });
        }
    }

    //リストのアイテム数を返す関数
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    //投稿日時計算関数
    private String getRelativeTime(Timestamp createdAt) {
        if (createdAt == null) return "";

        long now = System.currentTimeMillis();
        long then = createdAt.toDate().getTime();
        long diff = now - then; // ミリ秒差

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours   = minutes / 60;
        long days    = hours / 24;

        if (days > 0) return days + "日前";
        if (hours > 0) return hours + "時間前";
        if (minutes > 0) return minutes + "分前";
        return "たった今";
    }


    static class MessageViewHolder extends RecyclerView.ViewHolder {

        //避難完了テキストボックス
        TextView tvMessage;

        TextView tvTime;
        //削除ボタン（単体）
        ImageButton btnDelete;

        //インスタンスを一度に済ます関数（引数は上部のコンストラクタで代入したXML）
        //→findViewByIdを何度もやらずholder.tvMessageのように使用できる
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvTime    = itemView.findViewById(R.id.tvTime);

        }
    }
}
