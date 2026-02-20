package com.a51integrated.sfs2x.handlers.game;

import com.a51integrated.sfs2x.extensions.GameExtension;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.collision.CollisionMapService;
import com.a51integrated.sfs2x.services.collision.SnapshotsHistoryService;
import com.a51integrated.sfs2x.services.precondition.InputCommandProcessor;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class DisconnectGameRoomServerEventHandler extends BaseServerEventHandler
{
    private final CollisionMapService collisionMapService;
    private final SnapshotsHistoryService snapshotsHistoryService;
    private final InputCommandProcessor inputCommandProcessor;

    public DisconnectGameRoomServerEventHandler(
            CollisionMapService collisionMapService,
            SnapshotsHistoryService snapshotsHistoryService,
            InputCommandProcessor inputCommandProcessor
    )
    {
        this.collisionMapService = collisionMapService;
        this.snapshotsHistoryService = snapshotsHistoryService;
        this.inputCommandProcessor = inputCommandProcessor;
    }

    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException
    {
        var user = (User) event.getParameter(SFSEventParam.USER);

        var userId = user.getId();
        var gameExtension = (GameExtension) getParentExtension();
        var roomStateService = gameExtension.getRoomStateService();

        if (!roomStateService.hasPlayer(userId))
        {
            return;
        }

        roomStateService.remove(userId);
        collisionMapService.removePlayerShape(userId);
        snapshotsHistoryService.removePlayer(userId);
        inputCommandProcessor.removePlayer(userId);

        var result = SFSObject.newInstance();
        result.putBool(SFSResponseHelper.OK, true);
        result.putInt(SFSResponseHelper.USER_ID, userId);

        var playersInRoom = roomStateService.getRoom().getPlayersList();
        playersInRoom.remove(user);

        trace("DisconnectGameRoomServerEventHandler.handleServerEvent");

        send(SFSResponseHelper.PLAYER_LEAVE_GAME_ROOM, result, playersInRoom);
    }
}
