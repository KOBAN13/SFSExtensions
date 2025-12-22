package com.a51integrated.sfs2x.loop;

import com.a51integrated.sfs2x.GameExtension;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.CollisionMapService;
import com.a51integrated.sfs2x.services.RoomStateService;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    private static final float THRESHOLD_SQR = THRESHOLD * THRESHOLD;

    private static final float PLAYER_RADIUS = 0.4f;
    private static final float PLAYER_HEIGHT = 1.8f;

    public static final float Rad2Deg = 57.29578f;

    private long snapshotId = 0;

    private Vector3f targetDirection = new Vector3f();

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

            game.trace("Input Direction: " + inputDirection);

            game.trace("Target Direction X : " + targetDirection.x + " Target Direction Z : " + targetDirection.z);
            game.trace("Player State horizontal : " + playerState.horizontal + "Player State vertical : "  + playerState.vertical);

            game.trace("PlayerMovementLoop starting dx=" + dx + ", dz=" + dz);
            game.trace("Rotation: " + playerState.rotation);

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
