package com.a51integrated.sfs2x;

import com.a51integrated.sfs2x.handlers.KickPlayerHandler;
import com.a51integrated.sfs2x.handlers.UpdateLobbyHandler;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class LobbyExtension extends SFSExtension
{
    @Override
    public void init()
    {
        addRequestHandler(SFSResponseHelper.KICK_USER, KickPlayerHandler.class);
        addRequestHandler(SFSResponseHelper.UPDATE_LOBBY_DATA, UpdateLobbyHandler.class);

        addEventListener(SFSEventType.ROOM_ADDED);
    }
}
