package com.a51integrated.sfs2x.data.state;

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

    public void copyFrom(PlayerState other)
    {
        snapshotId = other.snapshotId;
        serverTime = other.serverTime;
        rotation = other.rotation;
        eulerAngleY = other.eulerAngleY;
        prevX = other.prevX;
        prevY = other.prevY;
        prevZ = other.prevZ;
        horizontal = other.horizontal;
        vertical = other.vertical;
        verticalVelocity = other.verticalVelocity;
        isJumping = other.isJumping;
        isRunning = other.isRunning;
        isOnGround = other.isOnGround;
        animationState = other.animationState;
        aimDirectionX = other.aimDirectionX;
        aimDirectionY = other.aimDirectionY;
        aimDirectionZ = other.aimDirectionZ;
        aimPitch = other.aimPitch;
        x = other.x;
        y = other.y;
        z = other.z;
    }

    public PlayerState(int id)
    {
        this.id = id;
    }
}
