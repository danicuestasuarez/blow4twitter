package com.trabajo.sdm.blow.utility;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;


public class MyTwitterApiClient extends TwitterApiClient {
    public MyTwitterApiClient(TwitterSession session) {
        super(session);
    }

    public FollowersService getFollowersService() {
        return getService(FollowersService.class);
    }

    public FriendsService getFriendsService() {
        return getService(FriendsService.class);
    }

    public UnfollowService getUnfollowService() {
        return getService(UnfollowService.class);
    }

    public FollowService getFollowService() {
        return getService(FollowService.class);
    }


    public interface FollowersService {
        @GET("/1.1/followers/list.json")
        void show(@Query("user_id") Long userId, @Query("screen_name") String
                var,@Query("cursor") Long var4, @Query("skip_status") Boolean var1, @Query("include_user_entities") Boolean var2 ,@Query("count") Integer var3, Callback<Followers> cb);
    }


    public interface FriendsService {
        @GET("/1.1/friends/list.json")
        void show(@Query("user_id") Long userId, @Query("screen_name") String
                var,@Query("cursor") Long var4, @Query("skip_status") Boolean var1, @Query("include_user_entities") Boolean var2, @Query("count") Integer var3, Callback<Followers> cb);
    }
    public interface FollowersIDService {
        @GET("/1.1/followers/list.json")
        void show(@Query("user_id") Long userId, @Query("screen_name") String
                var, @Query("skip_status") Boolean var1, @Query("include_user_entities") Boolean var2, @Query("count") Integer var3, Callback<Followers> cb);
    }


    public interface FriendsIDService {
        @GET("/1.1/friends/list.json")
        void show(@Query("user_id") Long userId, @Query("screen_name") String
                var, @Query("skip_status") Boolean var1, @Query("include_user_entities") Boolean var2, @Query("count") Integer var3, Callback<Followers> cb);
    }


    public interface UnfollowService {
        @POST("/1.1/friendships/destroy.json")
        void show(@Query("user_id") Long userId, @Query("screen_name") String var, Callback<Followers> cb);
    }

    public interface FollowService {
        @POST("/1.1/friendships/create.json")
        void show(@Query("user_id") Long userId, @Query("screen_name") String var, Callback<Followers> cb);
    }


    public class Followers {
        @SerializedName("users")
        public final List<User> users;

        @SerializedName("next_cursor")
        public final Long nextCursor;

        @SerializedName("previous_cursor")
        public final Long previousCursor;

        public Followers(Long previousCursor, List<User> users, Long nextCursor) {
            Log.i("-----------", String.valueOf(nextCursor));
            this.previousCursor=previousCursor;
            this.users = users;
            this.nextCursor=nextCursor;
        }
    }
}