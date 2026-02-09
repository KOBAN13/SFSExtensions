package com.a51integrated.sfs2x.handlers.lobby;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import koban.roomModule.RoleService;

public class UpdateLobbyHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User sender, ISFSObject params)
    {
        var room = sender.getLastJoinedRoom();
        var resultObject = new SFSObject();

        if (RoleService.isOwner(room, sender))
        {
            resultObject.putUtfString(SFSResponseHelper.ERROR, "Only owner can update lobby data");
            send(SFSResponseHelper.UPDATE_LOBBY_DATA, resultObject, sender);
            return;
        }

        ChangeRoomSettings(params, room);

        var usersInRoom = room.getUserList();

        resultObject.putUtfString(SFSResponseHelper.OK, "OK");
        send(SFSResponseHelper.UPDATE_LOBBY_DATA, resultObject, usersInRoom);
    }

    private static void ChangeRoomSettings(ISFSObject params, Room room)
    {
        if (params.containsKey("nameLobby"))
        {
            var nameLobby = params.getUtfString("nameLobby");
            room.setName(nameLobby);
        }

        if (params.containsKey("maxUsers"))
        {
            var maxUsers = params.getInt("maxUsers");
            room.setMaxUsers(maxUsers);
        }

        if (params.containsKey("isPrivate"))
        {
            if (params.getBool("isPrivate"))
            {
                var isPrivate = params.getUtfString("password");
                room.setPassword(isPrivate);
            }
        }
    }
}
