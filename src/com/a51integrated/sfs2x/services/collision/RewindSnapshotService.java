package com.a51integrated.sfs2x.services.collision;

import com.a51integrated.sfs2x.data.state.InterpolatedState;
import org.joml.Math;

public class RewindSnapshotService {

    private final SnapshotsHistoryService snapshotsHistoryService;
    private final InterpolatedState interpolatedState = new InterpolatedState();
    private final int maxSizeTick = 60;

    public RewindSnapshotService(SnapshotsHistoryService snapshotsHistoryService) {
        this.snapshotsHistoryService = snapshotsHistoryService;
    }

    public InterpolatedState getInterpolatePlayerState(int userId, long clientSnapshotId, long serverSnapshotId, int clientAlpha)
    {
        interpolatedState.clear();

        var baseId = clampBaseId(clientSnapshotId, serverSnapshotId, maxSizeTick);
        var time = Math.clamp(0, 1, alpha01(clientAlpha));
        var pair = snapshotsHistoryService.getPair(userId, baseId);

        if (pair == null || pair.playerStateFirst == null || pair.playerStateSecond == null)
            return null;

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
