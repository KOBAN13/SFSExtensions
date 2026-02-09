package com.a51integrated.sfs2x.handlers.game;

import com.a51integrated.sfs2x.extensions.GameExtension;
import com.a51integrated.sfs2x.data.math.Vector3;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.collision.CollisionMapService;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class JoinGameRoomServerEventHandler extends BaseServerEventHandler
{
    private final CollisionMapService collisionMapService;

    public JoinGameRoomServerEventHandler(CollisionMapService collisionMapService) {
        this.collisionMapService = collisionMapService;
    }

    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException
    {
        var user = (User) event.getParameter(SFSEventParam.USER);
        var room = (Room) event.getParameter(SFSEventParam.ROOM);

        var gameExtension = (GameExtension) getParentExtension();

        var roomState = gameExtension.getRoomStateService();

        var playerState = roomState.get(user);

        playerState.x = (float) (Math.random() * 10f);
        playerState.y = 0f;
        playerState.z = (float) (Math.random() * 5f);
        playerState.animationState = "idle";

        var result = roomState.toSFSObject();

        trace("Register player: " + user.getId());

        collisionMapService.registerPlayerShape(
                user.getId(),
                new Vector3(playerState.x, playerState.y, playerState.z));

        send(SFSResponseHelper.PLAYER_JOIN_GAME_ROOM, result, room.getPlayersList());
    }
}
