package com.trabajo.sdm.blow;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trabajo.sdm.blow.modules.MapTrendsFragment;
import com.trabajo.sdm.blow.modules.MejoresMomentosFragment;
import com.trabajo.sdm.blow.modules.NoFollowerFragment;
import com.trabajo.sdm.blow.modules.TweetsFragment;
import com.trabajo.sdm.blow.utility.CustomList;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity{

    private Toolbar toolbar;
    private ImageView header;
    private ScrimInsetsFrameLayout sifl;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView ndList;

    TwitterSession session = Twitter.getInstance().core.getSessionManager().getActiveSession();
    private Long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sifl = (ScrimInsetsFrameLayout)findViewById(R.id.scrimInsetsFrameLayout);

        //Toolbar
        toolbar  = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        header  = (ImageView) findViewById(R.id.menu_header_img);
        //Menu del Navigation Drawer

        ndList = (ListView)findViewById(R.id.navdrawerlist);

        id = getSharedPreferences("blow", Context.MODE_PRIVATE).getLong("id",0);

        Twitter.getApiClient(session).getAccountService()
                .verifyCredentials(false, true, new com.twitter.sdk.android.core.Callback<User>() {
                    @Override
                    public void failure(TwitterException e) {
                    }

                    @Override
                    public void success(Result<User> userResult) {

                        User user = userResult.data;
                        id=user.getId();
                        CircleImageView profileImg = (CircleImageView) findViewById(R.id.circleView);
                        TextView name = (TextView) findViewById(R.id.name);
                        TextView bio = (TextView) findViewById(R.id.biografia);
                        URL url;
                        try {
//                            if (android.os.Build.VERSION.SDK_INT > 9) {
//                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//                                StrictMode.setThreadPolicy(policy);
//                            }
                            url = new URL(user.profileImageUrl.replace("normal","bigger"));
                            profileImg.setImageBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()));

                            name.setText(user.name + ": @" + session.getUserName());

                            bio.setText(user.description);
                            url = new URL(user.profileBannerUrl);
                            header.setImageBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()));
                            header.setColorFilter(new LightingColorFilter(0xff888888, 0x000000));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });

        final String[] opciones = new String[]{
                "Tweets",
                "Quién no me sigue",
                "Influencias mundiales",
                "Mejores Momentos"};

        final Integer[] imageId = {
                R.drawable.tw__composer_logo_blue,
                R.drawable.menu2,
                R.drawable.menu3,
                android.R.drawable.btn_star_big_off};

        CustomList ndMenuAdapter =
                new CustomList(this,
                         opciones,imageId);

        ndList.setAdapter(ndMenuAdapter);

        ndList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Fragment fragment = null;

                switch (pos) {
                    //TODO: Implementar los fragments para cada caso --> actualmente tira excepción
                    case 0:
                        fragment = new TweetsFragment();
                        break;
                    case 1:
                        fragment = NoFollowerFragment.newInstance(id);
                        break;
                    case 2:
                        fragment = new MapTrendsFragment();
                        break;
                    case 3:
                        fragment = new MejoresMomentosFragment();
                        break;
                }
                drawerLayout.closeDrawer(sifl);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .commit();

                ndList.setItemChecked(pos, true);

                getSupportActionBar().setTitle(opciones[pos]);
            }
        });

        //Drawer Layout

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.color_primary_dark));

        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerOpened(View drawerView) {
                //Cierra el teclado si se abre
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);
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