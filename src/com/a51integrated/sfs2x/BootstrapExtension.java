package com.a51integrated.sfs2x;
import com.a51integrated.sfs2x.handlers.LoginEventHandler;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class BootstrapExtension extends SFSExtension
{
    @Override
    public void init()
    {
        trace("Beam cocksucker");
        trace("Initialize bootstrap extension");

        addEventHandler("register", );
        addEventHandler("restorePassword", );
        addEventHandler(SFSEventType.USER_LOGIN, LoginEventHandler.class);
    }

    @Override
    public void destroy()
    {
        super.destroy();
    }
}
