package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.Utils.ERoomRole;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.RoleService;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import java.util.ArrayList;

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
            getApi().joinRoom(sender, room, roomPassword, false, null, false, true);
            RoleService.assignRole(room, sender, ERoomRole.PLAYER);
            sendSuccess(result, room, sender);
            sendRoomJoinedEvent(sender, room);
        }
        catch (SFSJoinRoomException e)
        {
            sendError(result, "Error joining room", sender);
        }
    }

    private void sendRoomJoinedEvent(User sender, Room room) throws SFSJoinRoomException
    {
        var users = new ArrayList<User>();
        var data = new SFSObject();

        var userArray = new SFSArray();

        for (var user : room.getUserList())
        {
            var userObject = new SFSObject();
            userObject.putInt("userId", user.getId());
            userArray.addSFSObject(userObject);
            users.add(user);
        }

        data.putSFSArray("users", userArray);
        users.add(sender);

        send(SFSResponseHelper.ROOM_JOIN, data, users);
    }

    private void sendSuccess(SFSObject resultObject, Room room, User sender)
    {
        var role = RoleService.getRole(room, sender);

        resultObject.putBool(SFSResponseHelper.OK, true);
        resultObject.putUtfString("role", role.name());
        resultObject.putInt("userId", sender.getId());
        send(SFSResponseHelper.JOIN_ROOM, resultObject, sender);
    }

    private void sendError(SFSObject resultObject, String message, User sender)
    {
        resultObject.putUtfString(SFSResponseHelper.ERROR, message);
        resultObject.putBool(SFSResponseHelper.OK, false);
        send(SFSResponseHelper.JOIN_ROOM, resultObject, sender);
    }
}
