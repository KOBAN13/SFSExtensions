package com.a51integrated.sfs2x.extensions;

import com.a51integrated.sfs2x.handlers.game.CreateGameRoomHandler;
import com.a51integrated.sfs2x.handlers.game.KickPlayerHandler;
import com.a51integrated.sfs2x.handlers.game.StartGameHandler;
import com.a51integrated.sfs2x.handlers.lobby.JoinLobbyRoomServerEventHandler;
import com.a51integrated.sfs2x.handlers.lobby.UpdateLobbyHandler;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class LobbyExtension extends SFSExtension
{
    @Override
    public void init() {
        addRequestHandler(SFSResponseHelper.KICK_USER_IN_ROOM, KickPlayerHandler.class);
        addRequestHandler(SFSResponseHelper.UPDATE_LOBBY_DATA, UpdateLobbyHandler.class);
        addRequestHandler(SFSResponseHelper.ROOM_START_GAME, StartGameHandler.class);
        addRequestHandler(SFSResponseHelper.CREATE_GAME_ROOM, CreateGameRoomHandler.class);

        addEventHandler(SFSEventType.USER_JOIN_ROOM, JoinLobbyRoomServerEventHandler.class);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
