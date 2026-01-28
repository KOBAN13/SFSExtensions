package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.data.PlayerState;
import com.a51integrated.sfs2x.services.SnapshotsHistoryService;
import org.joml.Math;

public class RewindSnapshotService {

    private final SnapshotsHistoryService snapshotsHistoryService;
    private final int MAX_REWIND_MS = 400;
    private int snapshotDeltaMs = 50;

    public RewindSnapshotService(SnapshotsHistoryService snapshotsHistoryService) {
        this.snapshotsHistoryService = snapshotsHistoryService;
    }

    public PlayerState getPlayerState(int playerId, int snapshotId, int snapshotNowId)
    {
        var maxRewindSnapshots = MAX_REWIND_MS / snapshotDeltaMs;
        var rewindId = Math.clamp(snapshotId, snapshotNowId - maxRewindSnapshots, snapshotNowId);

        var playerStateFirst = snapshotsHistoryService.getRecord(playerId, rewindId);
        var playerStateSecond = snapshotsHistoryService.getRecord(playerId, rewindId + 1);
    }
}
