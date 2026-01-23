package com.example.sosbaton;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

public class FriendViewModel extends ViewModel {
   //フレンドリストのViewModelクラス

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
}
