package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.GameExtension;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class PlayerInputHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User sender, ISFSObject data)
    {
        var roomState = ((GameExtension)getParentExtension()).roomStateService;
        var playerState = roomState.get(sender);

        playerState.h = data.getFloat("h");
        playerState.v = data.getFloat("v");
        playerState.isRunning = data.getBool("isRunning");

        if (data.getBool("isJumping"))
        {
            playerState.isJumping = true;
        }
    }
}
