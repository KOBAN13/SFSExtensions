package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.GameExtension;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class JoinGameRoomServerEventHandler extends BaseServerEventHandler
{
    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException
    {
        var user = (User)event.getParameter(SFSEventParam.USER);
        var room = (Room)event.getParameter(SFSEventParam.ROOM);

        var gameExtension = (GameExtension) getParentExtension();

        var roomState = gameExtension.roomStateService;

        var playerState = roomState.get(user);

        playerState.x = (float) (Math.random() * 5f);
        playerState.y = 0f;
        playerState.z = (float) (Math.random() * 3f);

        var result = roomState.toSFSObject();

        trace("Good join game room room state");

        gameExtension.send(SFSResponseHelper.PLAYER_JOIN_ROOM, result, room.getPlayersList());
    }
}
