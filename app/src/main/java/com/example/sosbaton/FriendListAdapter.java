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

    //リストに表示する用のフレンドリスト
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

    //自作XML読み込み
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

        String boardId = friend.getCurrentBoardId();
        if (boardId != null && !boardId.isEmpty()) {
            holder.tvCurrentBoard.setText("現在の避難場所: " + boardId);
            holder.tvCurrentBoard.setVisibility(View.VISIBLE);
        } else {
            // まだ避難していない、または情報がない場合
            holder.tvCurrentBoard.setText("避難情報: 未設定");
            holder.tvCurrentBoard.setVisibility(View.VISIBLE);
        }

        String at = (friend.getEvacuatedAt() != null) ? friend.getEvacuatedAt() : "不明";
        holder.tvAddedAt.setText("避難時間: " + at);


        // --- ボタン類のクリックリスナー（空で準備だけしておく） ---
        holder.btnDelete.setOnClickListener(v -> {
            // TODO: 削除処理（ViewModel経由で呼び出す）
        });

        holder.btnCopy.setOnClickListener(v -> {
            // 1. システムのコピー機能（ClipboardManager）を取得
            android.content.Context context = v.getContext();
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    context.getSystemService(android.content.Context.CLIPBOARD_SERVICE);

            // 2. コピーするデータを作成
            android.content.ClipData clip = android.content.ClipData.newPlainText("UserID", friend.getUserId());

            // 3. クリップボードにセット
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);


                // 1. Snackbarを作成
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
            }
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
            btnCopy = itemView.findViewById(R.id.btnCopy);
        }
    }
}