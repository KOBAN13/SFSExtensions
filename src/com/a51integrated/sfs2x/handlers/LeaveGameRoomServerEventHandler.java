package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.GameExtension;
import com.a51integrated.sfs2x.services.CollisionMapService;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class LeaveGameRoomServerEventHandler extends BaseServerEventHandler
{
    private final CollisionMapService collisionMapService;

    public LeaveGameRoomServerEventHandler(CollisionMapService collisionMapService)
    {
        this.collisionMapService = collisionMapService;
    }

    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException
    {
        var user = (User) event.getParameter(SFSEventParam.USER);
        var gameExtension = (GameExtension) getParentExtension();

        gameExtension.getRoomStateService().remove(user);
        collisionMapService.removePlayerShape(user.getId());
    }
}
