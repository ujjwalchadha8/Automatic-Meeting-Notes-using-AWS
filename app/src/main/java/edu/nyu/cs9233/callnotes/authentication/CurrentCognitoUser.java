package edu.nyu.cs9233.callnotes.authentication;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;

import java.util.Map;

import edu.nyu.cs9233.callnotes.utils.Listeners;

public class CurrentCognitoUser {

    private final String sub;
    private final String emailVerified;
    private final String gender;
    private final String name;
    private final String email;

    public static void load(final Listeners.GetObjectListener<CurrentCognitoUser> listener) {
        AWSMobileClient.getInstance().getUserAttributes(new Callback<Map<String, String>>() {
            @Override
            public void onResult(final Map<String, String> result) {
                if (result == null) {
                    listener.onSuccess(null);
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSuccess(new CurrentCognitoUser(result.get("sub"), result.get("email_verified"), result.get("gender"), result.get("name"), result.get("email")));
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CurrentCognitoUser(String sub, String emailVerified, String gender, String name, String email) {
        this.sub = sub;
        this.emailVerified = emailVerified;
        this.gender = gender;
        this.name = name;
        this.email = email;
    }

    public String getSub() {
        return sub;
    }

    public String isEmailVerified() {
        return emailVerified;
    }

    public String getGender() {
        return gender;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override @NonNull
    public String toString() {
        return "CurrentCognitoUser{" +
                "sub='" + sub + '\'' +
                ", emailVerified='" + emailVerified + '\'' +
                ", gender='" + gender + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
