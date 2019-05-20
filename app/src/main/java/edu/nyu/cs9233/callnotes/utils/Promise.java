package edu.nyu.cs9233.callnotes.utils;

import android.os.AsyncTask;


public abstract class Promise<P, T> implements ParametarizedRunnable<P> {
    private SuccessListener<T> successListener;
    private FailureListener failureListener;

    protected void setSuccess(T response) {
        if (successListener == null) {
            return;
        }
        successListener.onSuccess(response);
    }

    protected void setFailure(Exception exception) {
        if (failureListener == null) {
            return;
        }
        failureListener.onFailure(exception);
    }

    public Promise<P, T> execute(P param) {
        BackgroundTask<P> backgroundTask = new BackgroundTask<>(this);
        backgroundTask.execute(param);
        return this;
    }

    public Promise onSuccess(SuccessListener<T> listener) {
        this.successListener = listener;
        return this;
    }

    public Promise onFailure(FailureListener listener) {
        this.failureListener = listener;
        return this;
    }

    public interface SuccessListener<T> {
        void onSuccess(T response);
    }

    public interface FailureListener {
        void onFailure(Exception exception);
    }

    private static class BackgroundTask<P> extends AsyncTask<P, Void, Void> {

        private final ParametarizedRunnable<P> runnable;

        private BackgroundTask(ParametarizedRunnable<P> runnable) {
            this.runnable = runnable;
        }

        @Override
        protected Void doInBackground(P... ps) {
            runnable.run(ps[0]);
            return null;
        }

    }

}