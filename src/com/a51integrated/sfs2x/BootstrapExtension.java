package com.a51integrated.sfs2x;
import com.a51integrated.sfs2x.handlers.LoginEventHandler;
import com.a51integrated.sfs2x.handlers.RegisterHandler;
import com.a51integrated.sfs2x.handlers.RestoreHandler;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class BootstrapExtension extends SFSExtension
{
    @Override
    public void init()
    {
        trace("Beam cocksucker");
        trace("Initialize bootstrap extension");

        addRequestHandler("register", RegisterHandler.class);
        addRequestHandler("restorePassword", RestoreHandler.class);

        addEventHandler(SFSEventType.USER_LOGIN, LoginEventHandler.class);
    }

    @Override
    public void destroy()
    {
        super.destroy();
    }
}
