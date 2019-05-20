package edu.nyu.cs9233.callnotes.home;

import java.io.Serializable;

public class CallUser implements Serializable {

    private final String name;
    private final String email;
    private final String id;

    public CallUser(String name, String email, String id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "CallUser{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
