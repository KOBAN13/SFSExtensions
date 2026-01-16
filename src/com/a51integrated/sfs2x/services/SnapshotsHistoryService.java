package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.data.PlayerState;
import com.a51integrated.sfs2x.data.RingBuffer;

import java.util.concurrent.ConcurrentHashMap;

public class SnapshotsHistoryService
{
    private ConcurrentHashMap<Integer, RingBuffer<PlayerState>> history = new ConcurrentHashMap<>();

    private final int maxSizeTick = 60;

    public void record(int userId, PlayerState playerState)
    {
        var buffer = history.get(userId);

        if (buffer == null)
            return;

        var slot = buffer.acquireSlot(() -> new PlayerState(userId));
        slot.copyFrom(playerState);
    }

    public PlayerState getRecord(int userId, long snapshotId)
    {
        return history.get(userId).poll();
    }

    public void ensurePlayer(int userId)
    {
        history.computeIfAbsent(userId, k -> new RingBuffer<>(maxSizeTick));
    }

    public void removePlayer(int userId)
    {
        history.remove(userId);
    }
}
