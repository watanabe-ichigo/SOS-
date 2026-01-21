package com.example.sosbaton;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;

public class FriendViewModel extends ViewModel {
   //フレンドリストのViewModelクラス

    //FriendRepositoryクラスのインスタンス
    private final FriendRepository repository = new FriendRepository();

    // 検索結果を格納する「観察可能な」箱
    private final MutableLiveData<List<FriendModel>> searchResults = new MutableLiveData<>();

    //ActivityでObserveする用のリストを渡すメソッド
    public LiveData<List<FriendModel>> getSearchResults() {
        return searchResults;
    }

    //ActivityとFriendRepositoryとの受け渡し　行き：メソッド実行　帰り：コールバック
    public void findFriend(String id) {
        repository.searchUserByName(id, friends -> {
            // 結果をLiveDataにセット（これでActivityに通知が飛ぶ）
            searchResults.postValue(friends);
        });
    }


    //検索終了ごとにサーチようのリストリセット
    public void clearSearchResult() {
        if (searchResults != null) {
            // LiveDataの中身を空（null）にする
            searchResults.setValue(null);
        }
    }
}
