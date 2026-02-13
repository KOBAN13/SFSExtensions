package com.a51integrated.sfs2x.data.state;

public class InputFrame
{
    public long inputTick;
    public long snapshotId;

    public float eulerAngleY;
    public float horizontal, vertical;
    public boolean isJumping;
    public boolean isRunning;
    public float aimDirectionX, aimDirectionY, aimDirectionZ;
    public float aimPitch;
}
