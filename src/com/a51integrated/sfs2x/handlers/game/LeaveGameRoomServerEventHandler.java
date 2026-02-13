package com.a51integrated.sfs2x.handlers.game;

import com.a51integrated.sfs2x.extensions.GameExtension;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.collision.CollisionMapService;
import com.a51integrated.sfs2x.services.collision.SnapshotsHistoryService;
import com.a51integrated.sfs2x.services.precondition.InputCommandProcessor;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class LeaveGameRoomServerEventHandler extends BaseServerEventHandler
{
    private final CollisionMapService collisionMapService;
    private final SnapshotsHistoryService snapshotsHistoryService;
    private final InputCommandProcessor inputCommandProcessor;

    public LeaveGameRoomServerEventHandler(
            CollisionMapService collisionMapService,
            SnapshotsHistoryService snapshotsHistoryService, InputCommandProcessor inputCommandProcessor
    )
    {
        this.collisionMapService = collisionMapService;
        this.snapshotsHistoryService = snapshotsHistoryService;
        this.inputCommandProcessor = inputCommandProcessor;
    }

    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException
    {
        var result = SFSObject.newInstance();

        var user = (User) event.getParameter(SFSEventParam.USER);
        var room = (Room) event.getParameter(SFSEventParam.ROOM);

        result.putBool(SFSResponseHelper.OK, false);

        var userId = user.getId();
        var playersInRoom = room.getPlayersList();
        var gameExtension = (GameExtension) getParentExtension();

        gameExtension.getRoomStateService().remove(user);
        collisionMapService.removePlayerShape(userId);
        snapshotsHistoryService.removePlayer(userId);
        inputCommandProcessor.removePlayer(userId);

        playersInRoom.remove(userId);

        send(SFSResponseHelper.PLAYER_LEAVE_GAME_ROOM, result, playersInRoom);
    }
}
