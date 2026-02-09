package com.a51integrated.sfs2x.handlers.game;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import koban.roomModule.RoleService;

public class KickPlayerHandler extends BaseClientRequestHandler {

    @Override
    public void handleClientRequest(User sender, ISFSObject params)
    {
        var resultObject = new SFSObject();
        var room = sender.getLastJoinedRoom();

        if (!RoleService.isOwner(room, sender))
        {
            sendError(resultObject, "Only owner can kick players.", sender);
            return;
        }

        var targetUser = getTargetUser(params, room);

        if (targetUser == null)
        {
            sendError(resultObject, "Target user not found.", sender);
            return;
        }

        getApi().leaveRoom(targetUser, room);
        sendSuccess(resultObject, targetUser.getId(), targetUser);
    }

    private User getTargetUser(ISFSObject params, Room room)
    {
        var targetId = params.getInt("targetId");
        var targetUser = getParentExtension().getParentZone().getUserById(targetId);

        return (targetUser != null && room.containsUser(targetUser)) ? targetUser : null;
    }

    private void sendError(SFSObject resultObject, String message, User sender)
    {
        resultObject.putUtfString(SFSResponseHelper.ERROR, message);
        resultObject.putBool(SFSResponseHelper.OK, false);
        send(SFSResponseHelper.KICK_USER_IN_ROOM, resultObject, sender);
    }

    private void sendSuccess(SFSObject resultObject, int kickedId, User targetUser)
    {
        resultObject.putBool(SFSResponseHelper.OK, true);
        send(SFSResponseHelper.KICK_USER_IN_ROOM, resultObject, targetUser);
    }
}

