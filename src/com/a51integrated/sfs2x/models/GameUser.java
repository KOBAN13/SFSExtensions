package com.a51integrated.sfs2x.models;

public class GameUser
{
    public final long id;
    public final String name;
    public final String email;
    public final String password_hash;

    public GameUser(long id, String name, String email, String password_hash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password_hash = password_hash;
    }
}
