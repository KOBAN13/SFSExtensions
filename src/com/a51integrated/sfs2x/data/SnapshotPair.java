package com.a51integrated.sfs2x.data;

public final class SnapshotPair
{
    public final PlayerState playerStateFirst;
    public final PlayerState playerStateSecond;

    public SnapshotPair(PlayerState playerStateFirst, PlayerState playerStateSecond) {
        this.playerStateFirst = playerStateFirst;
        this.playerStateSecond = playerStateSecond;
    }
}
