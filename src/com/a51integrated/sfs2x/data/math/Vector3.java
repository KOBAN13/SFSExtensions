package com.a51integrated.sfs2x.data.math;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Vector3
{
    public float x;
    public float y;
    public float z;

    @JsonCreator
    public Vector3(
            @JsonProperty("x") float x,
            @JsonProperty("y") float y,
            @JsonProperty("z") float z
    )
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
