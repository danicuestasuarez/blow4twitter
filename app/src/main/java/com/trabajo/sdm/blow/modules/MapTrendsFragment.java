package com.trabajo.sdm.blow.modules;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
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
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class MapTrendsFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    private SearchView mSearchView;
    private ViewGroup cancel;

    private Geocoder geocoder;
    private TwitterApiClient twitterApiClient;

    private ListView mListView;
    private TweetTimelineListAdapter mAdapter;

    private FrameLayout tweets;

    private LatLng currentGeoPos;
    private Marker currentMarker;
    private int currentRadio = 1;

    public MapTrendsFragment(){
        //Empty Constructor
    }

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
            Toast.makeText(getContext(),R.string.MT_noConection,Toast.LENGTH_LONG).show();
        }

        googleMap = mMapView.getMap();
        // Perform any camera updates here

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                try {
                    List<Address> adds =geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if(adds.size() == 0){
                        Toast.makeText(getContext(),R.string.MT_placeUnknown,Toast.LENGTH_LONG).show();
                        return;
                    }
                    Address address = adds.get(0);
                    mSearchView.setQuery(address.getAddressLine(0) +", " + address.getAddressLine(1),false);
                    updateMap(address,15);
                } catch (IOException e) {
                    Toast.makeText(getContext(),R.string.MT_noConection,Toast.LENGTH_LONG).show();
                }
            }
        });

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
        cancel = (LinearLayout) v.findViewById(R.id.cancelar_view);

        //Boton cancelar
        Button cancelar = (Button) v.findViewById(R.id.button);
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel.setVisibility(View.INVISIBLE);
                cancel.animate().translationY(tweets.getHeight() + cancel.getHeight());
                tweets.setVisibility(View.INVISIBLE);
                tweets.animate().translationY(tweets.getHeight());
            }
        });

        tweets.setVisibility(View.INVISIBLE);
        tweets.animate().translationY(tweets.getHeight());
        cancel.setVisibility(View.INVISIBLE);
        cancel.animate().translationY(tweets.getHeight() + cancel.getHeight());
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
            List<Address> addresses = geocoder.getFromLocationName(query, 5);

            if(addresses.size() == 0) {
                Toast.makeText(getContext(),R.string.MT_noLugar,Toast.LENGTH_SHORT).show();
                return;
            }

            generarDialog(addresses);
        } catch (IOException e) {
            Toast.makeText(getContext(),R.string.MT_noConection,Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTimeline(final ListView list, TwitterApiClient twitterApiClient) {
        tweets.setVisibility(View.VISIBLE);
        tweets.animate().translationY(0);
        cancel.setVisibility(View.VISIBLE);
        cancel.animate().translationY(0);
        twitterApiClient.getSearchService().tweets("",
                new Geocode(currentGeoPos.latitude, currentGeoPos.longitude, currentRadio, Geocode.Distance.KILOMETERS),
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

    private void updateMap(Address address, int radio){
        currentGeoPos = new LatLng(address.getLatitude(),address.getLongitude());

        //Actualizar mapa
        if(currentMarker != null)
            currentMarker.remove();

        //Add marker
        currentMarker = googleMap.addMarker(new MarkerOptions().position(currentGeoPos));

        //centrar camara
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentGeoPos,7));

        currentRadio = radio;
        updateTimeline(mListView, twitterApiClient);
    }

    private void generarDialog(final List<Address> list){
        //Cargar layout
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View layout = inflater.inflate(R.layout.dialog_list, null);
        final Spinner spinner= (Spinner) layout.findViewById(R.id.dialog_spinner);

        List<String> addresses = new LinkedList<>();
        for(Address a:list){
            addresses.add(a.getAddressLine(0) + ", " +a.getAddressLine(1));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1,addresses);

        spinner.setAdapter(adapter);

        final SeekBar seekBar = (SeekBar) layout.findViewById(R.id.seekBar);
        final TextView actual = (TextView) layout.findViewById(R.id.actual);
        seekBar.incrementProgressBy(1);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int actualProgress = progress + 1;
                actual.setText(actualProgress + "Km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //No se necesita
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //No se necesita
            }
        });

        //Generar dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout);

        builder.setTitle(R.string.MT_dialogTitle);
        builder.setPositiveButton(R.string.MT_buscarTweets, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateMap(list.get(spinner.getSelectedItemPosition()),seekBar.getProgress() + 1);
            }
        });
        builder.show();
    }
}