package com.a51integrated.sfs2x;

import com.a51integrated.sfs2x.handlers.JoinGameRoomServerEventHandler;
import com.a51integrated.sfs2x.handlers.JoinLobbyRoomServerEventHandler;
import com.a51integrated.sfs2x.handlers.PlayerInputHandler;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.loop.PlayerMovementLoop;
import com.a51integrated.sfs2x.services.RoomStateService;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameExtension extends SFSExtension
{
    public RoomStateService roomStateService;
    private ScheduledFuture<?> gameLoop;

    @Override
    public void init()
    {
        var room = getParentRoom();
        roomStateService = new RoomStateService(room);

        var sfs = SmartFoxServer.getInstance();

        addRequestHandler(SFSResponseHelper.PLAYER_INPUT, PlayerInputHandler.class);
        addEventHandler(SFSEventType.USER_JOIN_ROOM, JoinGameRoomServerEventHandler.class);

        gameLoop = sfs.getTaskScheduler().scheduleAtFixedRate(
                new PlayerMovementLoop(this, roomStateService),
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
