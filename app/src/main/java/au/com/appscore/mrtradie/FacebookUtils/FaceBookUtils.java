package au.com.appscore.mrtradie.FacebookUtils;


/**
 * Created by lijiazhou on 22/01/16.
 */
public class FaceBookUtils {
    private static FriendList friendList;
    private static boolean loggedInWithFb = false;

    public FaceBookUtils()
    {

    }

    public FaceBookUtils(boolean loginFlag)
    {
        loggedInWithFb = loginFlag;
        if(friendList == null && loggedInWithFb)
            friendList = new FriendList();
    }

    public final boolean getLoginStatus()
    {
        return loggedInWithFb;
    }

    public final FriendList getFriendList()
    {
        return friendList;
    }

}
