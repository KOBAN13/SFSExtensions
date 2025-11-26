package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class JoinLobbyRoomServerEventHandler extends BaseServerEventHandler
{
    @Override
    public void handleServerEvent(ISFSEvent event)
    {
        var room = (Room) event.getParameter(SFSEventParam.ROOM);
        var user = (User) event.getParameter(SFSEventParam.USER);

        var isGameStarted = room.getVariable("gameStarted").getBoolValue();
        var result = new SFSObject();

        if (isGameStarted)
        {
            result.putBool(SFSResponseHelper.OK, true);
            send(SFSResponseHelper.ROOM_START_GAME, result, user);
        }
        else
        {
            result.putInt("roomId", room.getId());

            send(SFSResponseHelper.ROOM_USER_CONNECTED, result, room.getUserList());
        }
    }
}
