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

    // 20 тиков/сек
    private static final float DELTA_TIME = 0.05f;

    // Макс. допустимый шаг по XZ за тик (античит/антиспайк)
    private static final float THRESHOLD = 1.5f;
    private static final float THRESHOLD_SQR = THRESHOLD * THRESHOLD;

    // Параметры капсулы игрока для серверной коллизии
    // (желательно брать из конфига/room settings)
    private static final float PLAYER_RADIUS = 0.4f;
    private static final float PLAYER_HEIGHT = 1.8f;

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
        float serverTime = snapshotId * DELTA_TIME;

        for (var user : room.getPlayersList())
        {
            var playerState = roomStateService.get(user);

            playerState.serverTime = serverTime;
            playerState.snapshotId = snapshotId;

            var speed = playerState.isRunning ? 8f : 4f;

            var baseX = playerState.x;
            var baseY = playerState.y;
            var baseZ = playerState.z;

            playerState.prevX = baseX;
            playerState.prevZ = baseZ;

            var dx = playerState.horizontal * speed * DELTA_TIME;
            var dz = playerState.vertical   * speed * DELTA_TIME;

            var stepSqr = dx * dx + dz * dz;

            if (stepSqr > THRESHOLD_SQR)
            {
                continue;
            }

            var targetX = baseX + dx;
            var targetZ = baseZ + dz;

            var isColliding = collisionMapService.isColliding(targetX, baseY, targetZ);

            game.trace("isColliding " + isColliding);

            if (!isColliding)
            {
                playerState.x = targetX;
                playerState.z = targetZ;
            }

            if (playerState.isJumping)
            {
                if (playerState.isOnGround)
                {
                    playerState.verticalVelocity = JUMP_VELOCITY;
                    playerState.isOnGround = false;
                }
                playerState.isJumping = false;
            }

            playerState.verticalVelocity += GRAVITY * DELTA_TIME;
            playerState.y += playerState.verticalVelocity * DELTA_TIME;

            if (playerState.y > MAX_JUMP_HEIGHT)
            {
                playerState.y = MAX_JUMP_HEIGHT;
                playerState.verticalVelocity = 0f;
            }

            if (playerState.y <= 0f)
            {
                playerState.y = 0f;
                playerState.verticalVelocity = 0f;
                playerState.isOnGround = true;
            }
            else
            {
                playerState.isOnGround = false;
            }
        }

        var packet = roomStateService.toSFSObject();
        game.send(SFSResponseHelper.PLAYER_STATE, packet, room.getPlayersList());
    }
}
