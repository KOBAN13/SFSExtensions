package koban.roomModule;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;

import java.util.HashMap;
import java.util.Map;

public class RoleService
{
    private static final String PROP_ROLES = "roles";

    @SuppressWarnings("unchecked")
    private static Map<Integer, ERoomRole> getRoleMap(Room room)
    {
        var roleMap = (Map<Integer, ERoomRole>) room.getProperty(PROP_ROLES);

        if (roleMap == null)
        {
            roleMap = new HashMap<Integer, ERoomRole>();
            room.setProperty(PROP_ROLES, roleMap);
        }

        return roleMap;
    }

    public static void assignRole(Room room, User user, ERoomRole role)
    {
        var rolesMap = getRoleMap(room);
        rolesMap.put(user.getId(), role);
        room.setProperty(PROP_ROLES, rolesMap);
    }

    public static ERoomRole getRole(Room room, User user)
    {
        var rolesMap = getRoleMap(room);
        return rolesMap.get(user.getId());
    }

    public static void removeRole(Room room, User user)
    {
        var rolesMap = getRoleMap(room);
        rolesMap.remove(user.getId());
        room.setProperty(PROP_ROLES, rolesMap);
    }

    public static boolean isOwner(Room room, User user)
    {
        return getRole(room, user) == ERoomRole.OWNER;
    }

    public static int getOwnerId(Room room)
    {
        for (var entry : getRoleMap(room).entrySet())
        {
            if (entry.getValue().equals(ERoomRole.OWNER))
                return entry.getKey();
        }

        return -1;
    }
}
