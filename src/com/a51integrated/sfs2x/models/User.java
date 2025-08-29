package com.a51integrated.sfs2x.models;

public class User
{
    public final long id;
    public final String name;
    public final String email;
    public final String password;

    public User(long id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
