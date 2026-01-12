package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.GameExtension;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class PlayerStateHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User sender, ISFSObject data)
    {
        var roomState = ((GameExtension)getParentExtension()).getRoomStateService();
        var playerState = roomState.get(sender);

        playerState.eulerAngleY = data.getFloat("eulerAngleY");
        playerState.aimDirectionX = data.getFloat("aimDirectionX");
        playerState.aimDirectionY = data.getFloat("aimDirectionY");
        playerState.aimDirectionZ = data.getFloat("aimDirectionZ");
        playerState.aimPitch = data.getFloat("aimPitch");

        if (data.getBool("isOnGround"))
        {
            playerState.isOnGround = true;
        }
    }
}
