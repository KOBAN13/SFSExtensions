package com.a51integrated.sfs2x.extensions;

import com.a51integrated.sfs2x.handlers.collision.RaycastHandler;
import com.a51integrated.sfs2x.handlers.game.DisconnectGameRoomServerEventHandler;
import com.a51integrated.sfs2x.handlers.game.JoinGameRoomServerEventHandler;
import com.a51integrated.sfs2x.handlers.game.LeaveGameRoomServerEventHandler;
import com.a51integrated.sfs2x.handlers.player.PredictionPlayerHandler;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.loop.CollisionDataLoop;
import com.a51integrated.sfs2x.loop.PlayerMovementLoop;
import com.a51integrated.sfs2x.services.collision.CollisionMapService;
import com.a51integrated.sfs2x.services.collision.RewindSnapshotService;
import com.a51integrated.sfs2x.services.precondition.InputCommandProcessor;
import com.a51integrated.sfs2x.services.room.RoomStateService;
import com.a51integrated.sfs2x.services.collision.SnapshotsHistoryService;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameExtension extends SFSExtension
{
    private RoomStateService roomStateService;
    private CollisionMapService collisionMapService;
    private final SnapshotsHistoryService snapshotsHistoryService = new SnapshotsHistoryService();
    private final RewindSnapshotService rewindSnapshotService = new RewindSnapshotService(snapshotsHistoryService);
    private final InputCommandProcessor inputCommandProcessor = new InputCommandProcessor();
    private ScheduledFuture<?> gameLoop;
    private ScheduledFuture<?> colliderDebug;

    public RoomStateService getRoomStateService() {
        return roomStateService;
    }

    public CollisionMapService getCollisionMapService() {
        return collisionMapService;
    }

    @Override
    public void init()
    {
        trace("Initializing GameExtension");

        var room = getParentRoom();
        roomStateService = new RoomStateService(room);

        var path = getConfigProperties().getProperty("collision.map.path");

        collisionMapService = new CollisionMapService(path, this, rewindSnapshotService);

        var sfs = SmartFoxServer.getInstance();

        addRequestHandler(SFSResponseHelper.PLAYER_PRECONDITION_STATE, new PredictionPlayerHandler(inputCommandProcessor));
        addRequestHandler(SFSResponseHelper.RAYCAST, new RaycastHandler(collisionMapService, roomStateService));
        addEventHandler(SFSEventType.USER_JOIN_ROOM, new JoinGameRoomServerEventHandler(collisionMapService));
        addEventHandler(SFSEventType.USER_LEAVE_ROOM, new LeaveGameRoomServerEventHandler(collisionMapService, snapshotsHistoryService, inputCommandProcessor));
        addEventHandler(SFSEventType.USER_DISCONNECT, new DisconnectGameRoomServerEventHandler(collisionMapService, snapshotsHistoryService, inputCommandProcessor));

        gameLoop = sfs.getTaskScheduler().scheduleAtFixedRate(
                new PlayerMovementLoop(this, roomStateService, collisionMapService, snapshotsHistoryService, inputCommandProcessor),
                0,
                33,
                TimeUnit.MILLISECONDS
        );

        colliderDebug = sfs.getTaskScheduler().scheduleAtFixedRate(
                new CollisionDataLoop(this, collisionMapService, roomStateService),
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

        if (colliderDebug != null)
        {
            colliderDebug.cancel(true);
        }
    }
}
