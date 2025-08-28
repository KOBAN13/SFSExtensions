package com.a51integrated.sfs2x;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class TestExtension extends SFSExtension
{
    @Override
    public void init()
    {
        trace("Hello, My First Extension");

        this.addRequestHandler("math", SumReqHandler.class);
    }
}
