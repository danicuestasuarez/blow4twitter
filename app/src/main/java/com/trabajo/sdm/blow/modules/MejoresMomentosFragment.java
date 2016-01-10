package com.trabajo.sdm.blow.modules;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;

import com.trabajo.sdm.blow.R;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by Barri on 06/01/2016.
 */
public class MejoresMomentosFragment extends Fragment {

    private BestTweets bestTweets;

    private ViewGroup best1;
    private ViewGroup best2;
    private ViewGroup best3;

    private ProgressDialog dialog;

    private View globalView;

    public MejoresMomentosFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        globalView = inflater.inflate(R.layout.fragment_mej_mom, container, false);

        best1 = (LinearLayout) globalView.findViewById(R.id.tweetBox1);

        best2 = (LinearLayout) globalView.findViewById(R.id.tweetBox2);

        best3 = (LinearLayout) globalView.findViewById(R.id.tweetBox3);

        //Mientras carga no se muestra
        globalView.setVisibility(View.INVISIBLE);

        return globalView;
    }

    @Override
    public void onStart() {
        super.onStart();

        dialog = new ProgressDialog(getActivity());
        updateLoading(0);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        boolean isCached = getContext().getSharedPreferences("blow",Context.MODE_PRIVATE)
                .getBoolean("cacheMejoresMomentos",false);

        if(isCached) {
            bestTweets = new BestTweets(true);
            updateView(best1,best2,best3);
            return;
        }

        final Handler puente = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                updateLoading((int) msg.obj);
            }
        };

        //TODO: pantalla de carga
        //TODO: excepciones en worker
        //En este caso no existe limite de peticiones asi que ok

        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread thread, Throwable e) {
                        Log.i("Thread", "Exception: " + e);
                    }
                });

                try {
                    Twitter twitter = Twitter4JFactory.getInstance();

                    long id = getContext().getSharedPreferences("blow", Context.MODE_PRIVATE)
                            .getLong("id", 0);
                    User user = twitter.showUser(id);
//                    User user = twitter.showUser("fabiomg13");
                    double statusCount = user.getStatusesCount();
                    int paginas = (int) Math.ceil(statusCount / 200.0);

                    //TODO: si es menor de 3 dar excepcion o algo
                    //TODO: limite de 3200

                    bestTweets = new BestTweets(false);
                    int loadingCount = 0;
                    Message msg;

                    //ahora iteramos sobre GET statuses/user_timeline
//                    for (int i=1; i <= 1; i++){
                    for (int i=1; i <= paginas; i++){
                    ResponseList<Status> result = twitter.timelines().getUserTimeline(id, new Paging(i, 200));
//                        ResponseList<Status> result = twitter.timelines().getUserTimeline("fabiomg13", new Paging(i, 200)); Sorry fabio, no tengo a otro pa hacer pruebas
                        msg = new Message();
                        msg.obj = (int) ((i * 100)/paginas);
                        puente.sendMessage(msg);
                        for (Status status : result) {
                            Log.i("Tweet","tweet #" + loadingCount);
                            loadingCount++;
                            if(!status.isRetweet() && !status.isFavorited())
                                bestTweets.addTweet(status);
                        }
                        Log.i("Ejecucion","id:" + i);
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bestTweets.cacheResult();
                            updateView(best1,best2,best3);
                        }
                    });

                }catch(TwitterException e) {
                    Log.i("FalloTwitter","Twitter ha fallado");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),"Se ha perdido la conexión con Twitter",Toast.LENGTH_SHORT);
                        }
                    });
                }
            }
        });

        worker.start();
    }

    protected void updateView(final ViewGroup l1, final ViewGroup l2, final ViewGroup l3) {
        dialog.dismiss();
        globalView.setVisibility(View.VISIBLE);
        final List<Long> tweets = bestTweets.getBestTweets();
        TweetUtils.loadTweets(bestTweets.getBestTweets(), new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                List<Tweet> resultado = result.data;
                l1.addView(new TweetView(getContext(), resultado.get(0)));

                TextView tx1 = (TextView) globalView.findViewById(R.id.retweets1);
                TextView tx2 = (TextView) globalView.findViewById(R.id.favoritos1);

                tx1.setText("Retweets: " + resultado.get(0).retweetCount);
                tx2.setText("Favoritos: " + resultado.get(0).favoriteCount);

                l2.addView(new TweetView(getContext(), resultado.get(1)));

                TextView tx3 = (TextView) globalView.findViewById(R.id.retweets2);
                TextView tx4 = (TextView) globalView.findViewById(R.id.favoritos2);

                tx3.setText("Retweets: " + resultado.get(1).retweetCount);
                tx4.setText("Favoritos: " + resultado.get(1).favoriteCount);

                l3.addView(new TweetView(getContext(), resultado.get(2)));

                TextView tx5 = (TextView) globalView.findViewById(R.id.retweets3);
                TextView tx6 = (TextView) globalView.findViewById(R.id.favoritos3);

                tx5.setText("Retweets: " + resultado.get(2).retweetCount);
                tx6.setText("Favoritos: " + resultado.get(2).favoriteCount);
            }

            @Override
            public void failure(com.twitter.sdk.android.core.TwitterException e) {
                Toast.makeText(getContext(), "Fallo de Twitter", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void updateLoading(int loading) {
        dialog.setMessage("Analizando tus twits: " + loading + "%.");
    }

    class BestTweets {
        private long[] tweets = new long[3];
        private long[] values = new long[3];
        private long minValue = 0;

        public BestTweets(boolean fromCache) {
            if(fromCache) {
                SharedPreferences sp = getContext()
                        .getSharedPreferences("blow", Context.MODE_PRIVATE);

                tweets[0] = sp.getLong("bestTweet1",0);
                tweets[1] = sp.getLong("bestTweet2",0);
                tweets[2] = sp.getLong("bestTweet3",0);
            }
        }

        public void addTweet(Status status) {
            long value = status.getFavoriteCount() + status.getRetweetCount();
            if(value >= minValue)
                storeTweet(status.getId(),value);
        }

        private void storeTweet(long id, long value) {
            for (int i=0; i < tweets.length;i++) {
                if(values[i] <= value) {
                    exchange(i,id,value);
                    break;
                }
            }
        }

        private void exchange(int pos, long id, long value) {
            if(pos >= tweets.length - 1) {
                tweets[2] = id;
                values[2] = value;
                minValue = value;
            } else {
                long tempTweet = tweets[pos];
                long tempValue = values[pos];

                tweets[pos] = id;
                values[pos] = value;

                exchange(pos+1,tempTweet, tempValue);
            }
        }

        public void cacheResult() {
            SharedPreferences.Editor edit = getContext()
                    .getSharedPreferences("blow", Context.MODE_PRIVATE).edit();
            edit.putLong("bestTweet1",tweets[0]).apply();
            edit.putLong("bestTweet2",tweets[1]).apply();
            edit.putLong("bestTweet3",tweets[2]).apply();

            edit.putBoolean("cacheMejoresMomentos",true).apply();
        }

        public List<Long> getBestTweets(){
            List<Long> result = new ArrayList<>();
            for(long id : tweets)
                result.add(id);
            return result;
        }
    }

}