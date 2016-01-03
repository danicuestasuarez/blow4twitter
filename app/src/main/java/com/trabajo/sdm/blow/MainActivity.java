package com.trabajo.sdm.blow;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ScrimInsetsFrameLayout sifl;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView ndList;


    TwitterSession session = Twitter.getInstance().core.getSessionManager().getActiveSession();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sifl = (ScrimInsetsFrameLayout)findViewById(R.id.scrimInsetsFrameLayout);

        //Toolbar

        toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        //Menu del Navigation Drawer

        ndList = (ListView)findViewById(R.id.navdrawerlist);


        Twitter.getApiClient(session).getAccountService()
                .verifyCredentials(false, true, new com.twitter.sdk.android.core.Callback<User>() {
                    @Override
                    public void failure(TwitterException e) {
                    }

                    @Override
                    public void success(Result<User> userResult) {

                        User user = userResult.data;
                        CircleImageView profileImg = (CircleImageView) findViewById(R.id.circleView);
                        TextView name = (TextView) findViewById(R.id.name);
                        TextView bio = (TextView) findViewById(R.id.biografia);
                        URL url;
                        try {
                            if (android.os.Build.VERSION.SDK_INT > 9) {
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                            }
                            url = new URL(user.profileImageUrl.replace("normal","bigger"));
                            profileImg.setImageBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()));
                            name.setText(user.name + ": @" + session.getUserName());
                            bio.setText(user.description);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });

        final String[] opciones = new String[]{"Tweets", "Quién no me sigue", "Influencias mundiales"};

        ArrayAdapter<String> ndMenuAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_activated_1, opciones);

        ndList.setAdapter(ndMenuAdapter);

        ndList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Fragment fragment = null;

                switch (pos) {
                    //TODO: Implementar los fragments para cada caso --> actualmente tira excepción
                    case 0:
                        //fragment = new Fragment1();
                        break;
                    case 1:
                        //fragment = new Fragment2();
                        break;
                    case 2:
                        //fragment = new Fragment3();
                        break;
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .commit();

                ndList.setItemChecked(pos, true);

                getSupportActionBar().setTitle(opciones[pos]);

                drawerLayout.closeDrawer(sifl);
            }
        });

        //Drawer Layout

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.color_primary_dark));

        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}