package com.example.reuvenbagrut;

public interface FirebaseCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
}

