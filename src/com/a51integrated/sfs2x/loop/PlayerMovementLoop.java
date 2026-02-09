package com.a51integrated.sfs2x.loop;

import com.a51integrated.sfs2x.extensions.GameExtension;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.collision.CollisionMapService;
import com.a51integrated.sfs2x.services.room.RoomStateService;
import com.a51integrated.sfs2x.services.collision.SnapshotsHistoryService;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PlayerMovementLoop implements Runnable
{
    private final GameExtension game;
    private final RoomStateService roomStateService;
    private final CollisionMapService collisionMapService;
    private final SnapshotsHistoryService snapshotsHistoryService;

    private static final float GRAVITY = -9.81f;
    private static final float JUMP_VELOCITY = 8f;
    private static final float MAX_JUMP_HEIGHT = 9f;

    private static final float DELTA_TIME = 0.05f;

    private static final float THRESHOLD = 1.5f;
    private static final float THRESHOLD_SQR = THRESHOLD * THRESHOLD;

    private long snapshotId = 0;

    private Vector3f targetDirection = new Vector3f();

    public PlayerMovementLoop(
            GameExtension game,
            RoomStateService roomStateService,
            CollisionMapService collisionMapService,
            SnapshotsHistoryService snapshotsHistoryService
    )
    {
        this.game = game;
        this.roomStateService = roomStateService;
        this.collisionMapService = collisionMapService;
        this.snapshotsHistoryService = snapshotsHistoryService;
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
            var userId = user.getId();

            playerState.serverTime = serverTime;
            playerState.snapshotId = snapshotId;

            var targetSpeed = playerState.isRunning ? 8f : 4f;

            var inputDirection = new Vector3f(playerState.horizontal, 0f, playerState.vertical);

            if (inputDirection.lengthSquared() <= 0f)
                targetSpeed = 0f;

            var speedOffset = 0.1f;
            var inputMagnitude = 1f;

            var baseX = playerState.x;
            var baseY = playerState.y;
            var baseZ = playerState.z;

            playerState.prevX = baseX;
            playerState.prevZ = baseZ;

            if (inputDirection.lengthSquared() > 0f)
            {
                playerState.rotation = Math.toDegrees(Math.atan2(playerState.horizontal, playerState.vertical)) + playerState.eulerAngleY;

                targetDirection = new Quaternionf()
                        .rotateY(Math.toRadians(playerState.rotation))
                        .transform(new Vector3f(0, 0, 1)).normalize();
            }

            var dx = targetDirection.x * targetSpeed * DELTA_TIME;
            var dz = targetDirection.z * targetSpeed * DELTA_TIME;

            var stepSqr = dx * dx + dz * dz;

            if (stepSqr > THRESHOLD_SQR)
            {
                continue;
            }

            var targetX = baseX + dx;
            var targetZ = baseZ + dz;

            var isColliding = collisionMapService.isColliding(userId, targetX, baseY, targetZ);

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

            collisionMapService.updatePlayerShapeCenter(
                    playerState.id,
                    playerState.x,
                    playerState.y,
                    playerState.z);

            snapshotsHistoryService.ensurePlayer(userId);
            snapshotsHistoryService.record(userId, playerState, snapshotId);
        }

        var packet = roomStateService.toSFSObject();
        game.send(SFSResponseHelper.PLAYER_SERVER_STATE, packet, room.getPlayersList());
    }
}

