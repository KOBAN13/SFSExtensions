package com.a51integrated.sfs2x;

import com.a51integrated.sfs2x.handlers.CreateLobbyHandler;
import com.a51integrated.sfs2x.handlers.JoinRoomHandler;
import com.a51integrated.sfs2x.handlers.LoginHandler;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.handlers.ChangeUserPrivilegeHandler;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class GameZoneBootstrapExtension extends SFSExtension
{
    @Override
    public void init()
    {
        addEventHandler(SFSEventType.USER_LOGIN, LoginHandler.class);
        addRequestHandler(SFSResponseHelper.CHANGE_USER_PRIVILEGE, ChangeUserPrivilegeHandler.class);
        addRequestHandler(SFSResponseHelper.CREATE_ROOM, CreateLobbyHandler.class);
        addRequestHandler(SFSResponseHelper.USER_JOIN_ROOM, JoinRoomHandler.class);
    }

    @Override
    public void destroy()
    {
        super.destroy();
    }
}
