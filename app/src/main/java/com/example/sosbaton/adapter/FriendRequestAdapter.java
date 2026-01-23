package com.example.sosbaton.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sosbaton.R;
import com.example.sosbaton.model.FriendRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;

public class FriendRequestAdapter
        extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private List<FriendRequest> list;
    private String myUid;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // コンストラクタ（Activityから呼ばれる）
    public FriendRequestAdapter(List<FriendRequest> list, String myUid) {
        this.list = list;
        this.myUid = myUid;
    }

    // --------------------
    // 1行Viewを作る
    // --------------------
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);

        return new ViewHolder(view);
    }

    // --------------------
    // データを流し込む
    // --------------------
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        FriendRequest req = list.get(position);

        holder.tvUser.setText(req.getFrom_name());

        // 承認
        holder.btnAccept.setOnClickListener(v -> {

            accept(req);
        });
        /*holder.btnAccept.setOnClickListener(v -> accept(req));*/

        // 拒否
        holder.btnReject.setOnClickListener(v -> {
            db.collection("friend_requests")
                    .document(req.getDocId())
                    .update("status", "rejected");
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // --------------------
    // 承認処理
    // --------------------
    private void accept(FriendRequest req) {

        WriteBatch batch = db.batch();
        // 自分の friend_list に追加するデータ
        HashMap<String, Object> friendForMe = new HashMap<>();
        friendForMe.put("uid", req.getFrom_id());
        friendForMe.put("name", req.getFrom_name());
        friendForMe.put("added_at", com.google.firebase.Timestamp.now());

        // 相手の friend_list に追加するデータ
        HashMap<String, Object> friendForOther = new HashMap<>();
        friendForOther.put("uid", myUid);
        friendForOther.put("name", req.getTo_name()); // ← あとで直す
        friendForOther.put("added_at", com.google.firebase.Timestamp.now());

        batch.set(
                db.collection("users")
                        .document(myUid)
                        .collection("friend_list")
                        .document(req.getFrom_id()),
                friendForMe
        );

        batch.set(
                db.collection("users")
                        .document(req.getFrom_id())
                        .collection("friend_list")
                        .document(myUid),
                friendForOther
        );

        batch.update(
                db.collection("friend_requests")
                        .document(req.getDocId()),
                "status", "accepted"
        );

        batch.commit();
    }


    // --------------------
    // ViewHolder
    // --------------------
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUser;
        Button btnAccept, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvUser);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
