package edu.nyu.cs9233.callnotes.utils;

/**
 * Created by ujjwalchadha8 on 1/8/2018.
 */

public class Listeners {
    public interface LoadObjectListener<T> {
        void onObjectInitialised(T object);
        void onLoadProgress();
        void onLoadingComplete();
        void onFailure(Exception e);
    }

    public interface GetObjectListener<T> {
        void onSuccess(T object);
        void onFailure(Exception e);
    }

    public interface PersistObjectListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface ChangeListener<T>{
        void onChange(T entity);
        void onFailure(Exception e);
        void onCompleted(T entity);
    }
}
