package com.a51integrated.sfs2x.loop;

import com.a51integrated.sfs2x.GameExtension;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.CollisionMapService;
import com.a51integrated.sfs2x.services.RoomStateService;

public class PlayerMovementLoop implements Runnable
{
    private final GameExtension game;
    private final RoomStateService roomStateService;
    private final CollisionMapService collisionMapService;

    private static final float GRAVITY = -9.81f;
    private static final float JUMP_VELOCITY = 8f;
    private static final float MAX_JUMP_HEIGHT = 9f;
    private static final float DELTA_TIME = 0.05f;
    private static final float THRESHOLD = 1.5f;

    private long snapshotId = 0;

    public PlayerMovementLoop(GameExtension game, RoomStateService roomStateService, CollisionMapService collisionMapService)
    {
        this.game = game;
        this.roomStateService = roomStateService;
        this.collisionMapService = collisionMapService;
    }

    @Override
    public void run()
    {
        var room = roomStateService.getRoom();

        snapshotId++;
        var serverTime = snapshotId * DELTA_TIME;

        for (var user : room.getPlayersList())
        {
            var playerState = roomStateService.get(user);

            playerState.serverTime = serverTime;
            playerState.snapshotId = snapshotId;

            var speed = playerState.isRunning ? 8f : 4f;

            var prevX = playerState.prevX;
            var prevZ = playerState.prevZ;
            var prevY = playerState.y;

            var currentX = prevX + playerState.horizontal * speed * DELTA_TIME;
            var currentZ = prevZ + playerState.vertical * speed * DELTA_TIME;
            var currentY = playerState.y;

            var isColliding = collisionMapService.isColliding(currentX, currentZ, currentY);

            game.trace("isColliding " + isColliding);
            game.trace("currentX: " + currentX + " currentZ: " + currentZ + " currentY: " + currentY);

            if (isColliding)
            {
                playerState.x = prevX;
                playerState.z = prevZ;
                return;
            }

            playerState.x = currentX;
            playerState.z = currentZ;

            var distance = Math.sqrt(Math.pow(currentX - prevX, 2) + Math.pow(currentZ - prevZ, 2));

            if (distance > THRESHOLD)
            {
                playerState.x = prevX;
                playerState.z = prevZ;
            }
            else
            {
                playerState.prevX = currentX;
                playerState.prevZ = currentZ;
            }

            if (playerState.isJumping && playerState.isOnGround)
            {
                playerState.verticalVelocity = JUMP_VELOCITY;
                playerState.isJumping = false;
                playerState.isOnGround = false;
            }
            else if (playerState.isJumping && !playerState.isOnGround)
            {
                playerState.isJumping = false;
            }

            playerState.verticalVelocity += GRAVITY * DELTA_TIME;
            playerState.y += playerState.verticalVelocity * DELTA_TIME;

            if (playerState.y <= 0f)
            {
                playerState.y = 0f;
                playerState.verticalVelocity = 0f;
                playerState.isOnGround = true;
            }

            if (playerState.y > MAX_JUMP_HEIGHT)
            {
                playerState.y  = MAX_JUMP_HEIGHT;
                playerState.verticalVelocity = 0f;
            }
        }

        var packet = roomStateService.toSFSObject();

        game.send(SFSResponseHelper.PLAYER_STATE, packet, room.getPlayersList());
    }
}
