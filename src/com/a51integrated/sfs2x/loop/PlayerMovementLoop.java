package com.a51integrated.sfs2x.loop;

import com.a51integrated.sfs2x.GameExtension;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.RoomStateService;

public class PlayerMovementLoop implements Runnable
{
    private final GameExtension game;
    private final RoomStateService roomStateService;

    public PlayerMovementLoop(GameExtension game, RoomStateService roomStateService)
    {
        this.game = game;
        this.roomStateService = roomStateService;
    }

    @Override
    public void run()
    {
        var room = roomStateService.getRoom();

        for (var user : room.getPlayersList())
        {
            var playerState = roomStateService.get(user);

            var speed = playerState.isRunning ? 8f : 4f;

            playerState.x += playerState.horizontal * speed * 0.05f;
            playerState.z += playerState.vertical * speed * 0.05f;

            if (playerState.isJumping)
            {
                playerState.y += 0.3f;
                playerState.isJumping = false;
            }
        }

        var packet = roomStateService.toSFSObject();

        game.send(SFSResponseHelper.PLAYER_STATE, packet, room.getPlayersList(), true);
    }
}
