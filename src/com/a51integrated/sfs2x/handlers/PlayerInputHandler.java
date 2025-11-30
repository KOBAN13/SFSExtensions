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

        playerState.horizontal = data.getFloat("horizontal");
        playerState.vertical = data.getFloat("vertical");
        playerState.isRunning = data.getBool("isRunning");
        playerState.isOnGround = data.getBool("isOnGround");
        playerState.isJumping = data.getBool("isJumping");
    }
}
