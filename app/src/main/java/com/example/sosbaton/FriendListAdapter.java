package com.example.sosbaton;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendViewHolder> {

    private List<FriendModel> friendList = new ArrayList<>();

    // リストの更新（nullチェック付き）
    public void submitList(List<FriendModel> newList) {
        if (newList == null) {
            this.friendList = new ArrayList<>();
        } else {
            this.friendList = newList;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendModel friend = friendList.get(position);
        if (friend == null) return;

        // --- ユーザー名のセット（nullなら「不明なユーザー」） ---
        String name = (friend.getUserName() != null) ? friend.getUserName() : "不明なユーザー";
        holder.tvUsername.setText(name);

        // --- IDのセット（nullなら「---」） ---
        String id = (friend.getUserId() != null) ? friend.getUserId() : "---";
        holder.tvUserId.setText("ID: " + id);

        // --- 未実装情報のガード（データがない場合は非表示にする） ---
        // ※FriendModelにメソッドがない場合は、とりあえず仮でチェック
        // friend.getCurrentBoard() などのメソッドを追加した時に有効化してください

        /* 例：避難場所データのガード
        if (friend.getCurrentBoard() != null && !friend.getCurrentBoard().isEmpty()) {
            holder.tvCurrentBoard.setText("現在の避難場所: " + friend.getCurrentBoard());
            holder.tvCurrentBoard.setVisibility(View.VISIBLE);
        } else {
            holder.tvCurrentBoard.setVisibility(View.GONE); // データがないなら行ごと消す
        }
        */

        // 現在はまだデータが揃っていないとのことなので、固定で「情報なし」にするか非表示に
        holder.tvCurrentBoard.setText("避難場所情報: 未取得");
        holder.tvAddedAt.setVisibility(View.GONE); // 日時は一旦隠す

        // --- ボタン類のクリックリスナー（空で準備だけしておく） ---
        holder.btnDelete.setOnClickListener(v -> {
            // TODO: 削除処理（ViewModel経由で呼び出す）
        });

        holder.btnCopy.setOnClickListener(v -> {
            // TODO: クリップボードへのコピー処理
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvUserId, tvCurrentBoard, tvAddedAt;
        ImageButton btnDelete, btnEye, btnCopy;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvCurrentBoard = itemView.findViewById(R.id.tvCurrentBoard);
            tvAddedAt = itemView.findViewById(R.id.tvAddedAt);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEye = itemView.findViewById(R.id.btnEye);
            btnCopy = itemView.findViewById(R.id.btnCopy);
        }
    }
}