package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class KickPlayerHandler extends BaseClientRequestHandler {

    @Override
    public void handleClientRequest(User sender, ISFSObject params)
    {
        var resultObject = new SFSObject();
        var room = sender.getLastJoinedRoom();

        if (!isRoomOwner(sender))
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

        getApi().kickUser(targetUser, null, "Kicked by room owner", 0);
        sendSuccess(resultObject, targetUser.getId(), sender);
    }

    private boolean isRoomOwner(User sender)
    {
        var ownerVar = sender.getVariable("ownerId");
        return ownerVar != null && sender.getId() == ownerVar.getIntValue();
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
        send(SFSResponseHelper.KICK_USER, resultObject, sender);
    }

    private void sendSuccess(SFSObject resultObject, int kickedId, User sender)
    {
        resultObject.putInt("kickedId", kickedId);
        resultObject.putBool(SFSResponseHelper.OK, true);
        send(SFSResponseHelper.PLAYER_KICKED, resultObject, sender);
    }
}

