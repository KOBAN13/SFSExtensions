package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.data.InterpolatedState;
import com.a51integrated.sfs2x.services.SnapshotsHistoryService;
import org.joml.Math;

public class RewindSnapshotService {

    private final SnapshotsHistoryService snapshotsHistoryService;
    private final int MAX_REWIND_MS = 400;
    private int snapshotDeltaMs = 50;

    private final InterpolatedState interpolatedState = new InterpolatedState();

    public RewindSnapshotService(SnapshotsHistoryService snapshotsHistoryService) {
        this.snapshotsHistoryService = snapshotsHistoryService;
    }

    public InterpolatedState getInterpolatePlayerState(int userId, long clientSnapshotId, long serverSnapshotId, int clientAlpha)
    {
        interpolatedState.clear();

        var baseId = clampBaseId(clientSnapshotId, serverSnapshotId, 60);
        var time = alpha01(clientAlpha);
        var pair = snapshotsHistoryService.getPair(userId, baseId);

        interpolatedState.setLerp(pair.playerStateFirst, pair.playerStateSecond, time);
        return interpolatedState;
    }


    private long clampBaseId(long clientBaseId, long nowId, int maxSizeTick) {
        var minId = nowId - (maxSizeTick - 2);

        return Math.clamp(clientBaseId,  minId, nowId - 1);
    }

    private float alpha01(int shotAlpha) {
        var alpha = Math.max(0, Math.min(255, shotAlpha));
        return alpha / 255.0f;
    }
}
