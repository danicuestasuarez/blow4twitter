package com.trabajo.sdm.blow.modules;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.trabajo.sdm.blow.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.FixedTweetTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import java.util.List;

public class TweetsFragment extends Fragment {

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;
    private TweetTimelineListAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TweetsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_tweets_list, container, false);

        //Toma el layout actualizable deslizando hacia arriba
        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);

        //Toma instancia del cliente de api
        final TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();

        mListView = (ListView) view.findViewById(android.R.id.list);
        updateTimeline(mListView, twitterApiClient);

        //Explica al layout con actualizacion como actualizar su contenido
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(true);
                updateTimeline(mListView, twitterApiClient);
                swipeLayout.setRefreshing(false);
            }
        });

        return view;
    }

    /**
     * Actualiza la timeline con un nuevo feed de tweets
     *
     * @param list la lista sobre la que actualizar la timeline
     * @param twitterApiClient el cliente sobre el que actualizar, por motivos de rendimiento
     *
     *
     **/
    private void updateTimeline(final ListView list, TwitterApiClient twitterApiClient) {
        twitterApiClient.getStatusesService().homeTimeline(50, null, null, null, null, null, null, new Callback<List<Tweet>>() {

            @Override
            public void success(Result<List<Tweet>> listResult) {
                List<Tweet> tweets = listResult.data;
                final FixedTweetTimeline userTimeline = new FixedTweetTimeline.Builder().setTweets(tweets).build();

                mAdapter = new TweetTimelineListAdapter.Builder(getActivity())
                        .setTimeline(userTimeline).setViewStyle(R.style.tw__TweetLightWithActionsStyle).build();

                list.setAdapter(mAdapter);
            }

            @Override
            public void failure(TwitterException e) {
                Log.d("Twitter", "twitter " + e);
            }
        });
    }
}
