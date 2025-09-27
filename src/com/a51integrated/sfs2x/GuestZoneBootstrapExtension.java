package com.a51integrated.sfs2x;

import com.a51integrated.sfs2x.handlers.RegisterHandler;
import com.a51integrated.sfs2x.handlers.RestoreHandler;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class GuestZoneBootstrapExtension extends SFSExtension
{
    @Override
    public void init()
    {
        addRequestHandler(SFSResponseHelper.REGISTER_RESULT, RegisterHandler.class);
        addRequestHandler(SFSResponseHelper.RESTORE_RESULT, RestoreHandler.class);
    }

    @Override
    public void destroy()
    {
        super.destroy();
    }
}
