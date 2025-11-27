package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.SFSRoomRemoveMode;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import koban.roomModule.ERoomRole;
import koban.roomModule.RoleService;

public class CreateLobbyHandler extends BaseClientRequestHandler
{
    private final String ROOM_GROUP_NAME = "Game";

    @Override
    public void handleClientRequest(User sender, ISFSObject params)
    {
        var resultObject = new SFSObject();

        try
        {
            var roomSettings = buildRoomSettings(params);
            var api = getApi();

            var room = api.createRoom(getParentExtension().getParentZone(), roomSettings, sender);
            RoleService.assignRole(room, sender, ERoomRole.OWNER);
            api.joinRoom(sender, room, roomSettings.getPassword(), false, null, true, true);

            var lobbyRoomVariable = new SFSRoomVariable("gameStarted", false);
            lobbyRoomVariable.setPrivate(true);
            lobbyRoomVariable.setGlobal(false);
            lobbyRoomVariable.setPersistent(true);

            room.setVariable(lobbyRoomVariable);

            sendSuccess(resultObject, room, sender);

        }
        catch (SFSCreateRoomException | SFSJoinRoomException | SFSVariableException exception)
        {
            trace("Error creating or joining room", exception);
            sendError(resultObject, "Error creating or joining room", sender);
        }
    }

    private CreateRoomSettings buildRoomSettings(ISFSObject params)
    {
        var roomName = params.containsKey("roomName")
                ? params.getUtfString("roomName")
                : "Lobby_" + System.currentTimeMillis();

        var roomPassword = params.containsKey("roomPassword")
                ? params.getUtfString("roomPassword")
                : "";

        var maxUsers = params.containsKey("maxUsers")
                ? params.getShort("maxUsers")
                : 1;

        var roomSettings = new CreateRoomSettings();
        roomSettings.setName(roomName);
        roomSettings.setMaxUsers(maxUsers);
        roomSettings.setGroupId(ROOM_GROUP_NAME);
        roomSettings.setGame(false);
        roomSettings.setDynamic(true);
        roomSettings.setAutoRemoveMode(SFSRoomRemoveMode.WHEN_EMPTY_AND_CREATOR_IS_GONE);

        if (!roomPassword.isEmpty()) {
            roomSettings.setPassword(roomPassword);
        }

        roomSettings.setExtension(new CreateRoomSettings.RoomExtensionSettings(
                "ServerExtensions",
                "com.a51integrated.sfs2x.LobbyExtension"
        ));

        return roomSettings;
    }

    private void sendSuccess(SFSObject resultObject, Room room, User sender)
    {
        var role = RoleService.getRole(room, sender);

        resultObject.putBool(SFSResponseHelper.OK, true);
        resultObject.putUtfString("roomName", room.getName());
        resultObject.putUtfString("role", role.name());
        resultObject.putInt("roomId", room.getId());
        send(SFSResponseHelper.CREATE_ROOM, resultObject, sender);
    }

    private void sendError(SFSObject resultObject, String message, User sender)
    {
        resultObject.putUtfString(SFSResponseHelper.ERROR, message);
        resultObject.putBool(SFSResponseHelper.OK, false);
        send(SFSResponseHelper.CREATE_ROOM, resultObject, sender);
    }
}

