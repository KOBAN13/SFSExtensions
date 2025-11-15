package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import koban.roomModule.RoleService;

public class StartGameHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User sender, ISFSObject isfsObject)
    {
        var room = sender.getLastJoinedRoom();
        var result = new SFSObject();

        if (RoleService.isOwner(room, sender))
        {
            try
            {
                startGame(room, result);
            }
            catch (SFSVariableException e)
            {
                result.putUtfString(SFSResponseHelper.ERROR, "Error to set room variables");
                result.putBool(SFSResponseHelper.OK, true);
                send(SFSResponseHelper.ROOM_START_GAME, result, sender);
            }
            return;
        }

        result.putUtfString(SFSResponseHelper.ERROR, "You are not the owner of the room");
        result.putBool(SFSResponseHelper.OK, false);
        send(SFSResponseHelper.ROOM_START_GAME, result, sender);
    }

    private void startGame(Room room, SFSObject result) throws SFSVariableException
    {
        var roomVariable = new SFSRoomVariable("gameStarted", true);
        roomVariable.setGlobal(true);
        roomVariable.setPersistent(true);

        room.setVariable(roomVariable);

        result.putBool(SFSResponseHelper.OK, true);

        send(SFSResponseHelper.ROOM_START_GAME, result, room.getUserList());
    }
}
