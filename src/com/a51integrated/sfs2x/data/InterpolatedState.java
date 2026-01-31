package com.a51integrated.sfs2x.data;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class InterpolatedState {
    public Vector3f pos = new Vector3f();
    public Quaternionf rot = new Quaternionf();

    public void setLerp(PlayerState a, PlayerState b, float t) {
        pos.lerp(new Vector3f(a.x, a.y, a.z), t, new Vector3f(b.x, b.y, b.z));
    }

    public void clear() {
        pos.set(0, 0, 0);
        rot.x = 0;
        rot.y = 0;
        rot.z = 0;
        rot.w = 1;
    }
}