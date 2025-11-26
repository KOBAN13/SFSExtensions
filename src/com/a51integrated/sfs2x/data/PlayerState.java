package com.a51integrated.sfs2x.data;

public class PlayerState
{
    public final int id;

    public float x, y, z;
    public float horizontal, vertical;
    public boolean isJumping;
    public boolean isRunning;
    public String animationState;

    public PlayerState(int id)
    {
        this.id = id;
    }
}
