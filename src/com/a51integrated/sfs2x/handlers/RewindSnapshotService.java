package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.GameExtension;
import com.a51integrated.sfs2x.data.InterpolatedState;
import com.a51integrated.sfs2x.services.SnapshotsHistoryService;
import org.joml.Math;

public class RewindSnapshotService {

    private final SnapshotsHistoryService snapshotsHistoryService;
    private final GameExtension gameExtension;
    private final int MAX_REWIND_MS = 400;
    private int snapshotDeltaMs = 50;

    private final InterpolatedState interpolatedState = new InterpolatedState();

    public RewindSnapshotService(SnapshotsHistoryService snapshotsHistoryService, GameExtension gameExtension) {
        this.snapshotsHistoryService = snapshotsHistoryService;
        this.gameExtension = gameExtension;
    }

    public InterpolatedState getInterpolatePlayerState(int userId, long clientSnapshotId, long serverSnapshotId, int clientAlpha)
    {
        interpolatedState.clear();

        var baseId = clampBaseId(clientSnapshotId, serverSnapshotId, 60);
        var time = Math.clamp(0, 1, alpha01(clientAlpha));
        var pair = snapshotsHistoryService.getPair(userId, baseId);

        if (pair == null || pair.playerStateFirst == null || pair.playerStateSecond == null)
            return null;

        gameExtension.trace("BaseId: " + baseId + " Time: " + time + " ClientAlpha: " + clientAlpha + " Pair: " + pair.playerStateFirst.snapshotId + " " + pair.playerStateSecond.snapshotId);

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
