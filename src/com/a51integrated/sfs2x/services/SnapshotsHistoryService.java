package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.data.PlayerState;
import com.a51integrated.sfs2x.data.RingBuffer;
import com.a51integrated.sfs2x.data.SnapshotPair;

import java.util.concurrent.ConcurrentHashMap;

public class SnapshotsHistoryService
{
    private ConcurrentHashMap<Integer, RingBuffer<PlayerState>> history = new ConcurrentHashMap<>();

    private final int maxSizeTick = 60;

    public void record(int userId, PlayerState playerState, long snapshotId)
    {
        var buffer = history.get(userId);

        if (buffer == null)
            return;

        var index = (int) (snapshotId % maxSizeTick);

        var slot = buffer.getOrCreateAt(index, () -> new PlayerState(userId));

        slot.copyFrom(playerState);
        slot.snapshotId = snapshotId;
    }

    public PlayerState getRecord(int userId, long snapshotId)
    {
        var buffer = history.get(userId);

        var index = (int) (snapshotId % maxSizeTick);

        var state = buffer.getAt(index);

        return state.snapshotId == snapshotId ? state : null;
    }

    public SnapshotPair getPair(int userId, long snapshotId)
    {
        var firstPlayerState = getRecord(userId, snapshotId);

        var secondPlayerState = getRecord(userId, snapshotId + 1);

        return new SnapshotPair(firstPlayerState, secondPlayerState);
    }

    public void ensurePlayer(int userId) {
        history.computeIfAbsent(userId, k -> new RingBuffer<>(maxSizeTick));
    }

    public void removePlayer(int userId) {
        history.remove(userId);
    }
}

