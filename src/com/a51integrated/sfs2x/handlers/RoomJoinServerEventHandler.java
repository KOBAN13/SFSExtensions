package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class RoomJoinServerEventHandler extends BaseServerEventHandler
{
    @Override
    public void handleServerEvent(ISFSEvent event)
    {
        var room = (Room) event.getParameter(SFSEventParam.ROOM);

        var data = new SFSObject();
        data.putInt("roomId", room.getId());

        send(SFSResponseHelper.ROOM_USER_CONNECTED, data, room.getUserList());
    }
}
