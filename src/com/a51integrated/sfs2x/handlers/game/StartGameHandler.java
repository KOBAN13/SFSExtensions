package com.a51integrated.sfs2x.handlers.game;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import koban.roomModule.RoleService;

public class StartGameHandler extends BaseClientRequestHandler
{
    public void handleClientRequest(User sender, ISFSObject isfsObject)
    {
        final var lobbyRoom = sender.getLastJoinedRoom();
        final var users = lobbyRoom.getUserList();
        final var result = new SFSObject();

        if (!RoleService.isOwner(lobbyRoom, sender))
        {
            result.putUtfString(SFSResponseHelper.ERROR, "You are not the owner of the room");
            result.putBool(SFSResponseHelper.OK, false);
            send(SFSResponseHelper.ROOM_START_GAME, result, sender);
            return;
        }

        var lobbyRoomVariable = new SFSRoomVariable("gameStarted", true);
        lobbyRoomVariable.setPrivate(true);
        lobbyRoomVariable.setGlobal(false);
        lobbyRoomVariable.setPersistent(true);

        try
        {
            lobbyRoom.setVariable(lobbyRoomVariable);
        }
        catch (SFSVariableException e)
        {
            result.putBool(SFSResponseHelper.OK, false);
            send(SFSResponseHelper.ROOM_START_GAME, result, users);
        }

        result.putBool(SFSResponseHelper.OK, true);
        send(SFSResponseHelper.ROOM_START_GAME, result, users);
    }
}
