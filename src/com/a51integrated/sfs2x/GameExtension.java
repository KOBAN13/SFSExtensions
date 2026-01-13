package com.a51integrated.sfs2x;

import com.a51integrated.sfs2x.handlers.*;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.loop.PlayerMovementLoop;
import com.a51integrated.sfs2x.services.CollisionMapService;
import com.a51integrated.sfs2x.services.RoomStateService;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameExtension extends SFSExtension
{
    private RoomStateService roomStateService;
    private CollisionMapService collisionMapService;
    private ScheduledFuture<?> gameLoop;

    public RoomStateService getRoomStateService()
    {
        return roomStateService;
    }

    public CollisionMapService getCollisionMapService()
    {
        return collisionMapService;
    }

    @Override
    public void init()
    {
        var room = getParentRoom();
        roomStateService = new RoomStateService(room);

        var path = getConfigProperties().getProperty("collision.map.path");

        collisionMapService = new CollisionMapService(path, this);

        var sfs = SmartFoxServer.getInstance();

        addRequestHandler(SFSResponseHelper.PLAYER_INPUT, PlayerInputHandler.class);
        addRequestHandler(SFSResponseHelper.PLAYER_CLIENT_STATE, PlayerStateHandler.class);
        addRequestHandler(SFSResponseHelper.RAYCAST, new RaycastHandler(collisionMapService));
        addRequestHandler(SFSResponseHelper.COLLISION_DATA, new CollisionDataHandler(collisionMapService));
        addEventHandler(SFSEventType.USER_JOIN_ROOM, new JoinGameRoomServerEventHandler(collisionMapService));
        addEventHandler(SFSEventType.USER_LEAVE_ROOM, new LeaveGameRoomServerEventHandler(collisionMapService));

        gameLoop = sfs.getTaskScheduler().scheduleAtFixedRate(
                new PlayerMovementLoop(this, roomStateService, collisionMapService),
                0,
                33,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void destroy()
    {
        if  (gameLoop != null)
        {
            gameLoop.cancel(true);
        }
    }
}
