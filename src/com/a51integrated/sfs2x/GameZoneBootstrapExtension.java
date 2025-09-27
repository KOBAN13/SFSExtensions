package com.a51integrated.sfs2x;

import com.a51integrated.sfs2x.handlers.LoginHandler;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class GameZoneBootstrapExtension extends SFSExtension
{
    @Override
    public void init()
    {
        addEventHandler(SFSEventType.USER_LOGIN, LoginHandler.class);
    }

    @Override
    public void destroy()
    {
        super.destroy();
    }
}
