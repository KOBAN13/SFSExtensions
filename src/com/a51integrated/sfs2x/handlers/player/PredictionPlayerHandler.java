package com.a51integrated.sfs2x.handlers.player;

import com.a51integrated.sfs2x.data.state.InputFrame;
import com.a51integrated.sfs2x.services.precondition.InputCommandProcessor;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class PredictionPlayerHandler extends BaseClientRequestHandler {

    private final InputCommandProcessor inputCommandProcessor;

    public PredictionPlayerHandler(InputCommandProcessor inputCommandProcessor) {
        this.inputCommandProcessor = inputCommandProcessor;
    }

    @Override
    public void handleClientRequest(User sender, ISFSObject data) {
        var inputsBuffer = data.getSFSArray("inputs");

        for (var i = 0; i < inputsBuffer.size(); i++)
        {
            var input = inputsBuffer.getSFSObject(i);
            var inputFrame = new InputFrame();

            inputFrame.inputTick = input.getLong("inputTick");

            inputFrame.horizontal = input.getFloat("horizontal");
            inputFrame.vertical = input.getFloat("vertical");
            inputFrame.isRunning = input.getBool("isRunning");
            inputFrame.isJumping = input.getBool("isJumping");

            inputFrame.eulerAngleY = input.getFloat("eulerAngleY");

            inputFrame.aimDirectionX = input.getFloat("aimDirectionX");
            inputFrame.aimDirectionY = input.getFloat("aimDirectionY");
            inputFrame.aimDirectionZ = input.getFloat("aimDirectionZ");
            inputFrame.aimPitch = input.getFloat("aimPitch");
            inputFrame.isAim = input.getBool("isAim");

            inputCommandProcessor.AddInputFrame(sender.getId(), inputFrame);
        }
    }
}
