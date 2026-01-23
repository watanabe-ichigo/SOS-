package com.example.sosbaton;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

public class FriendViewModel extends ViewModel {
   //フレンドリストのViewModelクラス

    // RecyclerViewなどに表示する現在のフレンド一覧ライブデータリスト
    private final MutableLiveData<List<FriendModel>> _friendList = new MutableLiveData<>(new ArrayList<>());
    //フレンド一覧を参照させる用のリスト
    public final LiveData<List<FriendModel>> friendList = _friendList;

    //新しく誰かが追加された瞬間だけ通知するためのライブデータ(処理が終わったらnullを代入して消費)
    private final MutableLiveData<FriendModel> _newFriendAddedEvent = new MutableLiveData<>();

    //追加通知を参照させる変数
    public final LiveData<FriendModel> newFriendAddedEvent = _newFriendAddedEvent;

    // 前回のリストサイズを覚えておくための変数(これと比較して「増えたかどうか」を判定)
    private int previousListSize = -1;

    // 監視の「権利書」を保持する変数
    private com.google.firebase.firestore.ListenerRegistration friendListener;


    //FriendRepositoryクラスのインスタンス
    private final FriendRepository repository = new FriendRepository();

    // 検索結果を格納する「観察可能な」箱
    private final MutableLiveData<List<FriendModel>> searchResults = new MutableLiveData<>();

    //リクエスト送信結果の格納ライブデータ宣言
    private final MutableLiveData<FriendRequestResult> _friendRequestResult = new MutableLiveData<>();

    //ActivityでObserveする用のリストを渡すメソッド　検索用
    public LiveData<List<FriendModel>> getSearchResults() {
        return searchResults;
    }

    //ActivityでObserveする用のライブデータを渡すメソッド　リクエスト用
    public LiveData<FriendRequestResult> getFriendRequestResult() {
        return _friendRequestResult;
    }

    //ActivityとFriendRepositoryとの受け渡し　行き：メソッド実行　帰り：コールバック
    public void findFriend(String id) {
        repository.searchUserByName(id, friends -> {
            // 結果をLiveDataにセット（これでActivityに通知が飛ぶ）
            searchResults.postValue(friends);
        });
    }


    //検索終了ごとにサーチ用のリストリセット
    public void clearSearchResult() {
        if (searchResults != null) {
            // LiveDataの中身を空（null）にする
            searchResults.setValue(null);
        }
    }


    //ログイン中のuserが指定した相手にフレンド申請送信ロジック　引数:指定user
    public void sendFriendRequest(String targetUid) {

        //メモリ上の認証状態から現在user取得
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // ログインチェック
        if (currentUser == null) {
            _friendRequestResult.setValue(new FriendRequestResult(
                    FriendRequestResult.Status.ERROR, "ログインが必要です"));
            return;
        }

        //現在のuseridを取得
        String myUid = currentUser.getUid();

        // 自分と指定userを比較して自分自身だったらreturn
        if (targetUid.equals(myUid)) {
            _friendRequestResult.setValue(new FriendRequestResult(
                    FriendRequestResult.Status.ERROR, "自分自身には送信できません"));
            return;
        }

        // Firebaseクラスのフレンド申請送信ロジックへ
        repository.processFriendRequest(myUid, targetUid, result -> {
            _friendRequestResult.postValue(result);
        });
    }

    //リクエスト用のライブデータの結果をリセット
    public void clearFriendRequestResult() {
        _friendRequestResult.setValue(null);
    }


    /**
     * 監視を開始するメソッド
     */
    public void init() {
        // すでに監視中なら二重に登録しないようにする
        if (friendListener != null) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String myUid = user.getUid();

        // Repositoryの監視メソッドを呼び出す
        friendListener = repository.observeFriendList(myUid, updatedList -> {

            // 【重要】追加判定ロジック
            // 初回（previousListSizeが-1）は無視し、2回目以降の更新でサイズが増えていたら「追加」
            if (previousListSize != -1 && updatedList.size() > previousListSize) {
                // リストの最後に入った人が「新しく追加されたフレンド」
                FriendModel newlyAdded = updatedList.get(updatedList.size() - 1);

                // Activityに通知を送る（Event発火）
                _newFriendAddedEvent.postValue(newlyAdded);
            }

            // 1. リスト本体を更新（ActivityのRecyclerViewが動く）
            _friendList.postValue(updatedList);

            // 2. 現在のサイズを記録（次回の比較用）
            previousListSize = updatedList.size();
        });
    }

    /**
     * 通知を表示し終わった後に呼ぶ（二重通知防止）
     */
    public void consumeNewFriendEvent() {
        _newFriendAddedEvent.setValue(null);
    }

    /**
     * ViewModelが破棄されるときに呼ばれる（後片付け）
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // 監視を止めてメモリリークを防ぐ
        if (friendListener != null) {
            friendListener.remove();
            friendListener = null;
        }
    }
}
