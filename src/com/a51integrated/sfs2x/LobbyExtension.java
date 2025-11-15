package com.a51integrated.sfs2x;

import com.a51integrated.sfs2x.handlers.KickPlayerHandler;
import com.a51integrated.sfs2x.handlers.RoomJoinServerEventHandler;
import com.a51integrated.sfs2x.handlers.StartGameHandler;
import com.a51integrated.sfs2x.handlers.UpdateLobbyHandler;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class LobbyExtension extends SFSExtension
{
    @Override
    public void init()
    {
        addRequestHandler(SFSResponseHelper.KICK_USER_IN_ROOM, KickPlayerHandler.class);
        addRequestHandler(SFSResponseHelper.UPDATE_LOBBY_DATA, UpdateLobbyHandler.class);
        addRequestHandler(SFSResponseHelper.ROOM_START_GAME, StartGameHandler.class);

        addEventHandler(SFSEventType.USER_JOIN_ROOM, RoomJoinServerEventHandler.class);
    }

    @Override
    public void destroy()
    {
        super.destroy();
    }
}
