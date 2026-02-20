package com.a51integrated.sfs2x.loop;

import com.a51integrated.sfs2x.extensions.GameExtension;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.data.state.InputFrame;
import com.a51integrated.sfs2x.data.state.PlayerState;
import com.a51integrated.sfs2x.services.collision.CollisionMapService;
import com.a51integrated.sfs2x.services.precondition.InputCommandProcessor;
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
    private final InputCommandProcessor inputCommandProcessor;

    private static final float GRAVITY = -9.81f;
    private static final float JUMP_VELOCITY = 8f;
    private static final float MAX_JUMP_HEIGHT = 9f;

    private static final float DELTA_TIME = 0.033f;

    private static final float THRESHOLD = 1.5f;
    private static final float THRESHOLD_SQR = THRESHOLD * THRESHOLD;

    private long snapshotId = 0;

    private Vector3f targetDirection = new Vector3f();

    public PlayerMovementLoop(
            GameExtension game,
            RoomStateService roomStateService,
            CollisionMapService collisionMapService,
            SnapshotsHistoryService snapshotsHistoryService,
            InputCommandProcessor inputCommandProcessor
    )
    {
        this.game = game;
        this.roomStateService = roomStateService;
        this.collisionMapService = collisionMapService;
        this.snapshotsHistoryService = snapshotsHistoryService;
        this.inputCommandProcessor = inputCommandProcessor;
    }

    @Override
    public void run()
    {
        try
        {
            var room = roomStateService.getRoom();

            snapshotId++;
            var serverTime = snapshotId * DELTA_TIME;

            for (var user : room.getPlayersList())
            {
                var userId = user.getId();

                var playerState = roomStateService.get(user);
                var inputFrame = inputCommandProcessor.pollNext(userId);

                if (inputFrame == null)
                {
                    inputFrame = createFallbackInput(playerState);
                }

                playerState.serverTime = serverTime;
                playerState.snapshotId = snapshotId;

                playerState.horizontal = inputFrame.horizontal;
                playerState.vertical = inputFrame.vertical;
                playerState.isRunning = inputFrame.isRunning;
                playerState.eulerAngleY = inputFrame.eulerAngleY;

                var targetSpeed = inputFrame.isRunning ? 8f : 4f;

                var inputDirection = new Vector3f(inputFrame.horizontal, 0f, inputFrame.vertical);

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
                    playerState.rotation = Math.toDegrees(Math.atan2(inputFrame.horizontal, inputFrame.vertical)) + inputFrame.eulerAngleY;

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

                playerState.isJumping = inputFrame.isJumping;

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

                playerState.inputTick = inputFrame.inputTick;

                playerState.aimPitch = inputFrame.aimPitch;
                playerState.aimDirectionX = inputFrame.aimDirectionX;
                playerState.aimDirectionZ = inputFrame.aimDirectionZ;
                playerState.aimDirectionY = inputFrame.aimDirectionY;

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
        catch (Exception ex)
        {
            game.trace("PlayerMovementLoop error", ex);
        }
    }

    private InputFrame createFallbackInput(PlayerState playerState)
    {
        var inputFrame = new InputFrame();
        inputFrame.inputTick = playerState.inputTick;
        inputFrame.horizontal = playerState.horizontal;
        inputFrame.vertical = playerState.vertical;
        inputFrame.isRunning = playerState.isRunning;
        inputFrame.isJumping = false;
        inputFrame.eulerAngleY = playerState.eulerAngleY;
        inputFrame.aimDirectionX = playerState.aimDirectionX;
        inputFrame.aimDirectionY = playerState.aimDirectionY;
        inputFrame.aimDirectionZ = playerState.aimDirectionZ;
        inputFrame.aimPitch = playerState.aimPitch;
        return inputFrame;
    }
}
