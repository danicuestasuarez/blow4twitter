package com.trabajo.sdm.blow.modules;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.google.gson.annotations.SerializedName;
import com.trabajo.sdm.blow.R;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;
import java.util.List;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 */
public class NoFollowerFragment extends Fragment{

    private final static Long cursor_inicial= Long.valueOf(-1);

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private TwitterSession session = Twitter.getInstance().core.getSessionManager().getActiveSession();
    private Long idUser;
    private List<User> followers= new ArrayList<>();
    private List<User> friends= new ArrayList<>();
    public List<User> nofollowers= new ArrayList<>();
    private TwitterUserAdapter tAdapter;

    public static NoFollowerFragment newInstance(Long idUser) {
        NoFollowerFragment fragment = new NoFollowerFragment();
        Bundle args = new Bundle();
        args.putLong("id", idUser);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoFollowerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            idUser = getArguments().getLong("id");
        }

        getFriends(cursor_inicial);
        tAdapter = new TwitterUserAdapter(getActivity(),nofollowers,session);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nofollower, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(tAdapter);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    /**
     * Saca los seguidores del usuario registrado y los guarda en la lista followers
     * Luego llama al metodo que saca los no-followers
     */
    private void getFollowers(Long c)
    {
        if(c==0)
            getNoFollowers();
        else {
            new MyTwitterApiClient(session).getFollowersService().show(idUser, null, c, true, true, 200, new Callback<MyTwitterApiClient.Followers>() {
                @Override
                public void success(Result<MyTwitterApiClient.Followers> result) {
                    Log.i("------Get followers", "" + result.data.users.size());
                    //followers = result.data.users;
                    for (User u : result.data.users)
                        followers.add(u);

                    //((ArrayAdapter) mAdapter).notifyDataSetChanged();
                    //tAdapter.notifyDataSetChanged();
                    //getNoFollowers();
                    //Log.i("---------Followers", String.valueOf(followers.size()));
                    getFollowers(result.data.nextCursor);
                }

                @Override
                public void failure(com.twitter.sdk.android.core.TwitterException e) {
                    Log.w("--Error", "followers");
                    e.printStackTrace();
                    Toast.makeText(getContext(),"Error descargando los seguidores",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    /**
     * Saca los seguidos del usuario registrado y los guarda en la lista friends
     * Luego llama al metodo que saca los followers
     */
    private void getFriends(Long c)
    {
        if(c==0)//Si el cursor está a 0 significa que no hay mas resultados
            getFollowers(cursor_inicial);
        else {
            new MyTwitterApiClient(session).getFriendsService().show(idUser, null, c, true, true, 200, new Callback<MyTwitterApiClient.Followers>() {

                @Override
                public void success(Result<MyTwitterApiClient.Followers> result) {
                    Log.i("------Get friends", "" + result.data.users.size());
                    //friends =result.data.users;
                    for (User u : result.data.users) {
                        friends.add(u);
                    }
                    //((ArrayAdapter) mAdapter).notifyDataSetChanged();
                    //tAdapter.notifyDataSetChanged();
                    //Log.i("----cursor", String.valueOf(result.data.nextCursor));
                    //getFollowers();
                    getFriends(result.data.nextCursor);//Es una gochada pero las otras maneras para evitar la concurrencia daban problemas
                }

                @Override
                public void failure(com.twitter.sdk.android.core.TwitterException e) {
                    Log.w("--Error","friends");
                    e.printStackTrace();
                    Toast.makeText(getContext(),"Error descargando los amigos",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Calcula los no-followers
     * Para llamar a este metodo tienen que haber sido llamados (Y FINALIZADOS) los metodos getFollowers() y getFriends()
     */
    private void getNoFollowers(){


        Log.i("-----Total Followers", String.valueOf(followers.size()));
        Log.i("-----Total Friends", String.valueOf(friends.size()));

        for(User friend: friends)
        {
            boolean following=false;
            for(User follower: followers) {
                if (follower.getId() == friend.getId()) {
                    following=true;
                    break;
                }
            }
            if(!following) {
                //Log.i("-", friend.name);
                nofollowers.add(friend);
            }
        }

        Log.i("----Total No followers", String.valueOf(nofollowers.size()));
        tAdapter.notifyDataSetChanged();
    }


}
