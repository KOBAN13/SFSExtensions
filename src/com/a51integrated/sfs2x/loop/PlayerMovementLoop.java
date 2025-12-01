package com.a51integrated.sfs2x.loop;

import com.a51integrated.sfs2x.GameExtension;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.RoomStateService;

public class PlayerMovementLoop implements Runnable
{
    private final GameExtension game;
    private final RoomStateService roomStateService;

    private static final float GRAVITY = -9.81f;
    private static final float JUMP_VELOCITY = 12f;
    private static final float DELTA_TIME = 0.05f;

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

            playerState.x += playerState.horizontal * speed * DELTA_TIME;
            playerState.z += playerState.vertical * speed * DELTA_TIME;

            game.trace("PlayerMovementLoop: playerState.x=" + playerState.x + ", playerState.z=" + playerState.z);
            game.trace("Jump:" + playerState.isJumping + "IsOnGround:" + playerState.isOnGround);

            if (playerState.isJumping && playerState.isOnGround)
            {
                playerState.verticalVelocity = JUMP_VELOCITY;
                playerState.isJumping = false;
                playerState.isOnGround = false;
            }

            playerState.verticalVelocity += GRAVITY * DELTA_TIME;
            playerState.y += playerState.verticalVelocity * DELTA_TIME;

            game.trace("VerticalVelocity:" + playerState.verticalVelocity + ", y:" + playerState.y);

            if (playerState.y <= 0f)
            {
                playerState.y = 0f;
                playerState.verticalVelocity = 0f;
                playerState.isOnGround = false;
            }
        }

        var packet = roomStateService.toSFSObject();

        game.send(SFSResponseHelper.PLAYER_STATE, packet, room.getPlayersList());
    }
}
