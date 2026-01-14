package com.example.sosbaton;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
public class BoardViewModel extends ViewModel {

    private final BoardRepository repository = new BoardRepository();
    private final MutableLiveData<BoardRepository.CaseType> registrationResult = new MutableLiveData<>();

    public LiveData<BoardRepository.CaseType> getRegistrationResult() {
        return registrationResult;
    }

    public void registerEvacuationUser(String userId, String userName, String pinDocId) {
        repository.registerEvacuation(userId, userName, pinDocId, resultCase -> {
            registrationResult.postValue(resultCase);
        });
    }
}
