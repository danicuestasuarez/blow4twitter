package com.trabajo.sdm.blow.modules;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;

import com.trabajo.sdm.blow.R;

import com.trabajo.sdm.blow.utility.Twitter4JFactory;
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
    private TweetAnalyst analyst;

    private ViewGroup best1;
    private ViewGroup best2;
    private ViewGroup best3;

    private ProgressDialog dialog;

    private View globalView;

    public MejoresMomentosFragment() {
        //Empty Constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        globalView = inflater.inflate(R.layout.fragment_mej_mom, container, false);

        best1 = (LinearLayout) globalView.findViewById(R.id.tweetBox1);

        best2 = (LinearLayout) globalView.findViewById(R.id.tweetBox2);

        best3 = (LinearLayout) globalView.findViewById(R.id.tweetBox3);

        final SwipeRefreshLayout swipeLayout =
                (SwipeRefreshLayout) globalView.findViewById(R.id.swipe_layout);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(false);
                loadTweets();
            }
        });

        //Mientras carga no se muestra
        globalView.setVisibility(View.INVISIBLE);

        return globalView;
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences sp = getContext().getSharedPreferences("blow",Context.MODE_PRIVATE);
        boolean isCached = sp.getBoolean("cacheMejoresMomentos",false);
        if(!isCached) {
            Log.i("Cache", "Cache de Mejores Momentos NO encontrado");
            loadTweets();
        } else {
            long idCache = sp.getLong("cacheMejoresMomentosID",0);
            long id = sp.getLong("id",0);
            if(idCache == id){
                Log.i("Cache","Cache de Mejores Momentos encontrado");
                bestTweets = new BestTweets(true);
                updateView(best1,best2,best3);
            } else {
                Log.i("Cache", "Cache de Mejores Momentos NO encontrado");
                loadTweets();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(analyst != null) {
            analyst.kill();
        }
        if(dialog != null){
            dialog.dismiss();
        }
    }

    protected void loadTweets() {
        Log.i("MejoresMomentos", "Inicando analisis de tweets");
        //Mantener ventana para que no se cierre
        globalView.setKeepScreenOn(true);

        //Crear el dialogo de carga
        dialog = new ProgressDialog(getActivity());
        updateLoading(0);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        analyst = new TweetAnalyst();
        final Thread thread = new Thread(analyst);

        //Mata el Thread cuando se da a atras
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                analyst.kill();
            }
        });
        thread.start();
    }

    //TODO: placeholder
    protected void updateView(final ViewGroup l1, final ViewGroup l2, final ViewGroup l3) {
        //Limpiar de previos ajustes
        l1.removeAllViewsInLayout();
        l2.removeAllViewsInLayout();
        l3.removeAllViewsInLayout();

        globalView.setVisibility(View.VISIBLE);
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
                Toast.makeText(getContext(), R.string.MM_lost, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void updateLoading(int loading) {
        dialog.setMessage(String.format(getResources().getString(R.string.MM_analisis), loading));
    }

    class TweetAnalyst implements Runnable {

        //por alguna razon, no se muere bien
        private boolean isKill = false;

        public void kill() {
            //Ya no se mantiene la ventana
            globalView.setKeepScreenOn(false);
            isKill = true;
        }

        //Aqui aparece un warning pero en la explicación dice claramente
        //Que en este caso no hay problemas
        final Handler puente = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                updateLoading(msg.what);
            }
        };

        @Override
        public void run() {
            //Para no cerrar la aplicación en caso de fallo
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable e) {
                    Log.e("ThreadException", "Exception: ", e);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),
                                    R.string.MM_uncaught, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            try {
                Twitter twitter = Twitter4JFactory.getInstance();

                long id = getContext().getSharedPreferences("blow", Context.MODE_PRIVATE)
                        .getLong("id", 0);
                User user = twitter.showUser(id);
//                User user = twitter.showUser("fabiomg13");
                double statusCount = user.getStatusesCount();
                int paginas = (int) Math.ceil(statusCount / 200.0);

                //Limite de 3000
                paginas = statusCount > 3000 ? 15 : paginas;

                bestTweets = new BestTweets(false);
                int validTweets = 0;

                if(isKill)
                    return;

                //ahora iteramos sobre GET statuses/user_timeline
                for (int i=1; i <= paginas; i++){
                    if(isKill)
                        return;

                    Log.i("TweetBatch","Tweet Batch #" + i + "/" + paginas + "...Started");
                        ResponseList<Status> result = twitter.timelines().getUserTimeline(id, new Paging(i, 200));
//                    ResponseList<Status> result = twitter.timelines().getUserTimeline("fabiomg13", new Paging(i, 200)); //Sorry fabio, no tengo a otro pa hacer pruebas
                    puente.sendEmptyMessage(((i * 100) / paginas));

                    for (Status status : result) {
                        //Solo se admiten tweets del usuario
                        if(!status.isRetweet() && !status.isFavorited()) {
                            validTweets++;
                            bestTweets.addTweet(status);
                        }
                    }
                    Log.i("TweetBatch","Tweet Batch #" + i + "/" + paginas + "...Completed");
                    if(isKill)
                        return;
                }

                final int valid = validTweets;

                if(isKill)
                    return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        //Ya no se mantiene la ventana
                        globalView.setKeepScreenOn(false);
                        if(valid < 3) {
                            Toast.makeText(getContext(),
                                    R.string.MM_minimo,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            bestTweets.cacheResult();
                            updateView(best1, best2, best3);
                        }
                    }
                });

            }catch(TwitterException e) {
                Log.i("Twitter", "Twitter ha fallado");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        //Ya no se mantiene la ventana
                        globalView.setKeepScreenOn(false);
                        Toast.makeText(getContext(),R.string.MM_lost,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    /**
     * Clase interna para controlar los 3 mejores tweets
     */
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
            Log.i("Cache","Guardado cache Mejores Momentos");
            SharedPreferences sp = getContext()
                    .getSharedPreferences("blow", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putBoolean("cacheMejoresMomentos", false).commit();

            edit.putLong("bestTweet1", tweets[0]).apply();
            edit.putLong("bestTweet2", tweets[1]).apply();
            edit.putLong("bestTweet3",tweets[2]).apply();

            long id = sp.getLong("id",0);

            edit.putLong("cacheMejoresMomentosID",id).apply();
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