package com.a51integrated.sfs2x.data.state;

public class PlayerState
{
    public final int id;

    public long inputTick;
    public long snapshotId;
    public float serverTime;
    public float rotation;
    public float eulerAngleY;
    public float prevX, prevY, prevZ;
    public float horizontal, vertical;
    public float verticalVelocity;
    public float horizontalVelocity;
    public boolean isJumping;
    public boolean isRunning;
    public boolean isOnGround;
    public boolean isStartRun;
    public boolean isEndRun;
    public boolean isDie;
    public boolean isAim;
    public float aimDirectionX, aimDirectionY, aimDirectionZ;
    public float aimPitch;
    public float x, y ,z;

    public void copyFrom(PlayerState other)
    {
        snapshotId = other.snapshotId;
        inputTick = other.inputTick;
        serverTime = other.serverTime;
        rotation = other.rotation;
        eulerAngleY = other.eulerAngleY;
        prevX = other.prevX;
        prevY = other.prevY;
        prevZ = other.prevZ;
        horizontal = other.horizontal;
        vertical = other.vertical;
        verticalVelocity = other.verticalVelocity;
        horizontalVelocity = other.horizontalVelocity;
        isDie = other.isDie;
        isStartRun = other.isStartRun;
        isEndRun = other.isEndRun;
        isJumping = other.isJumping;
        isRunning = other.isRunning;
        isOnGround = other.isOnGround;
        aimDirectionX = other.aimDirectionX;
        aimDirectionY = other.aimDirectionY;
        aimDirectionZ = other.aimDirectionZ;
        aimPitch = other.aimPitch;
        isAim = other.isAim;
        x = other.x;
        y = other.y;
        z = other.z;
    }

    public PlayerState(int id)
    {
        this.id = id;
    }
}
