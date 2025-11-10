package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.Utils.ERoomRole;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.RoleService;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class JoinRoomHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User sender, ISFSObject params)
    {
        var roomId = params.containsKey("roomId") ? params.getInt("roomId")  : -1;
        var roomPassword = params.containsKey("roomPassword") ? params.getUtfString("roomPassword") : "";
        var zone = getParentExtension().getParentZone();
        var result = new SFSObject();

        var room = zone.getRoomById(roomId);

        if (room == null)
        {
            sendError(result, "Room not found", sender);
            return;
        }

        try
        {
            getApi().joinRoom(sender, room, roomPassword, false, null, true, true);
            RoleService.assignRole(room, sender, ERoomRole.PLAYER);
            sendSuccess(result, room, sender);
        }
        catch (SFSJoinRoomException e)
        {
            sendError(result, "Error joining room", sender);
        }
    }

    private void sendSuccess(SFSObject resultObject, Room room, User sender)
    {
        var role = RoleService.getRole(room, sender);

        resultObject.putBool(SFSResponseHelper.OK, true);
        resultObject.putUtfString("role", role.name());
        resultObject.putInt("userId", sender.getId());
        send(SFSResponseHelper.USER_JOIN_ROOM, resultObject, sender);
    }

    private void sendError(SFSObject resultObject, String message, User sender)
    {
        resultObject.putUtfString(SFSResponseHelper.ERROR, message);
        resultObject.putBool(SFSResponseHelper.OK, false);
        send(SFSResponseHelper.USER_JOIN_ROOM, resultObject, sender);
    }
}
