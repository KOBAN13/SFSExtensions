package com.a51integrated.sfs2x.data.math;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Quaternion
{
    public float x;
    public float y;
    public float z;
    public float w;

    @JsonCreator
    public Quaternion(
            @JsonProperty("x") float x,
            @JsonProperty("y") float y,
            @JsonProperty("z") float z,
            @JsonProperty("w") float w
    )
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
}
