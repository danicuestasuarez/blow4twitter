package com.trabajo.sdm.blow.modules;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trabajo.sdm.blow.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.params.Geocode;
import com.twitter.sdk.android.tweetui.FixedTweetTimeline;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.GeoLocation;
import twitter4j.Location;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public class MapTrendsFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    private SearchView mSearchView;

    private Geocoder geocoder;
    private TwitterApiClient twitterApiClient;

    private ListView mListView;
    private TweetTimelineListAdapter mAdapter;

    private FrameLayout tweets;

    private LatLng currentGeoPos;
    private Marker currentMarker;

    //Empty Constructor
    public MapTrendsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_map_trends, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        // Perform any camera updates here

        //Toma el layout actualizable deslizando hacia arriba
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_layout);

        //Toma instancia del cliente de api y geocoder
        twitterApiClient = TwitterCore.getInstance().getApiClient();
        geocoder = new Geocoder(getContext());

        mListView = (ListView) v.findViewById(android.R.id.list);

        //Explica al layout con actualizacion como actualizar su contenido
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                updateTimeline(mListView, twitterApiClient);
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        //Search view
        mSearchView = (SearchView) v.findViewById(R.id.searchView);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                loadQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        tweets = (FrameLayout) v.findViewById(R.id.geo_tweets);

        //Boton cancelar
        Button cancelar = (Button) v.findViewById(R.id.button);
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tweets.setVisibility(View.INVISIBLE);
            }
        });

        tweets.setVisibility(View.INVISIBLE);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    protected void loadQuery(String query) {
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);

            if(addresses.size() == 0) {
                Toast.makeText(getContext(),"No se ha encontrado el lugar",Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = addresses.get(0);
            currentGeoPos = new LatLng(address.getLatitude(),address.getLongitude());

            //Actualizar mapa
            if(currentMarker != null)
                currentMarker.remove();

            //Add marker
            currentMarker = googleMap.addMarker(new MarkerOptions().position(currentGeoPos));

            //centrar camara
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentGeoPos,5));

            updateTimeline(mListView, twitterApiClient);

        } catch (IOException e) {
            Toast.makeText(getContext(),"Se ha perdido la conexión",Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTimeline(final ListView list, TwitterApiClient twitterApiClient) {
        tweets.setVisibility(View.VISIBLE);

        twitterApiClient.getSearchService().tweets("",
                new Geocode(currentGeoPos.latitude, currentGeoPos.longitude, 50, Geocode.Distance.KILOMETERS),
                null, null, "mixed", 100, null, null, null, true, new Callback<Search>() {
                    @Override
                    public void success(Result<Search> result) {
                        List<Tweet> tweets = result.data.tweets;
                        final FixedTweetTimeline userTimeline = new FixedTweetTimeline.Builder().setTweets(tweets).build();

                        mAdapter = new TweetTimelineListAdapter.Builder(getActivity())
                                .setTimeline(userTimeline).setViewStyle(R.style.tw__TweetLightWithActionsStyle).build();

                        list.setAdapter(mAdapter);
                    }

                    @Override
                    public void failure(com.twitter.sdk.android.core.TwitterException e) {
                        Log.d("Twitter", "twitter " + e);
                    }
                });

    }
}