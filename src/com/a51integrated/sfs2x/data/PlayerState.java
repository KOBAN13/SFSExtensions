package com.a51integrated.sfs2x.data;

public class PlayerState
{
    public final int id;

    public long snapshotId;
    public float serverTime;
    public float rotation;
    public float eulerAngleY;
    public float prevX, prevY, prevZ;
    public float horizontal, vertical;
    public float verticalVelocity;
    public boolean isJumping;
    public boolean isRunning;
    public boolean isOnGround;
    public String animationState;
    public float aimDirectionX, aimDirectionY, aimDirectionZ;
    public float aimPitch;
    public float x, y ,z;

    public PlayerState(int id)
    {
        this.id = id;
    }
}
