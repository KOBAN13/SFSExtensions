package com.a51integrated.sfs2x.data.state;

import org.joml.Vector3f;

public final class InterpolatedState {
    private final Vector3f positionFirstState = new Vector3f();
    private final Vector3f positionSecondState = new Vector3f();
    public Vector3f interpolatedPosition  = new Vector3f();

    public void setLerp(PlayerState a, PlayerState b, float t) {
        positionFirstState.set(a.x, a.y, a.z);
        positionSecondState.set(b.x, b.y, b.z);
        interpolatedPosition = positionFirstState.lerp(positionSecondState, t);
    }

    public void clear() {
        positionFirstState.zero();
        positionSecondState.zero();
        interpolatedPosition.zero();
    }
}
