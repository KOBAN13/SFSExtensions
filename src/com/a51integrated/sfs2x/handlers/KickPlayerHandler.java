package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class KickPlayerHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User sender, ISFSObject params)
    {
        var room = sender.getLastJoinedRoom();
        var ownerVariables = sender.getVariable("ownerId");
        var resultObject = new SFSObject();

        if(ownerVariables == null)
        {
            send(SFSResponseHelper.KICK_USER, resultObject, sender);
            return;
        }
        var ownerId = ownerVariables.getIntValue();

        if (sender.getId() != ownerId)
        {
            resultObject.putUtfString(SFSResponseHelper.ERROR, "Only owner can be kicked.");
            resultObject.putBool(SFSResponseHelper.OK, false);

            send(SFSResponseHelper.KICK_USER, resultObject, sender);
            return;
        }

        var targetId = params.getInt("targetId");
        var targetUser = getParentExtension().getParentZone().getUserById(targetId);

        if (targetUser == null || !room.containsUser(targetUser))
        {
            resultObject.putUtfString(SFSResponseHelper.ERROR, "Target User not found.");
            resultObject.putBool(SFSResponseHelper.OK, false);

            send(SFSResponseHelper.KICK_USER, resultObject, sender);
            return;
        }

        getApi().kickUser(targetUser, null, "Kicked by room owner", 0);

        resultObject.putInt("kickedId", targetId);
        resultObject.putBool(SFSResponseHelper.OK, true);
        send(SFSResponseHelper.PLAYER_KICKED, resultObject, sender);
    }
}
