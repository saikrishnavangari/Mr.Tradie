package au.com.appscore.mrtradie.FacebookUtils;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import au.com.appscore.mrtradie.FacebookUtils.FacebookJsonClass.Data;

/**
 * Created by lijiazhou on 21/01/16.
 */

public class FriendList implements Serializable{

    JSONArray friends;
    Data friendData;
    String name;
    String id;

    public  FriendList() {
        this.getUserID();
    }

    public final ArrayList getFriends()
    {
        ArrayList<String> emails = new ArrayList<>();
        for(int i = 0; i< friendData.size(); i++)
        {
            emails.add(friendData.get(i).id);
        }
        return emails;
    }
    private void getFriendList() {
        GraphRequest fbFriends = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback(){

                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONObject data = response.getJSONObject();
                        if(null == friends)
                        {
                            try {
                                friends = data.getJSONArray("data");
                                friendData = new Gson().fromJson(friends.toString(), Data.class);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        //Bundle params = new Bundle();
        //params.putString("fields", "id, email, account, name, picture");
        //fbFriends.setParameters(params);
        fbFriends.executeAsync();
    }

    private void getUserID() {
        GraphRequest fbUser = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code
                        try {
                            id = object.getString("id");
                            name = object.getString("name");
                            getFriendList();
                        }catch (JSONException je)
                        {
                            Log.d("FaceBook Error", je.getLocalizedMessage());
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name");
        fbUser.setParameters(parameters);
        fbUser.executeAsync();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
