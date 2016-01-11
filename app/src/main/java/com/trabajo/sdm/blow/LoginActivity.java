package com.trabajo.sdm.blow;

import com.trabajo.sdm.blow.modules.Twitter4JFactory;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import io.fabric.sdk.android.Fabric;


public class LoginActivity extends Activity {

    private TwitterLoginButton loginButton;

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "BRvBPIU1UQOI43cJEKly9gVth";
    private static final String TWITTER_SECRET = "7900oxGF98Cbqb7P0ot5humKEqzLMrnM9Q9idEsa4EcjwHotPM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        TwitterSession session = Twitter.getInstance().core.getSessionManager().getActiveSession();

        if (session != null){
            Twitter4JFactory.generateInstance(TWITTER_KEY,TWITTER_SECRET,
                    session.getAuthToken().token,session.getAuthToken().secret);
            Twitter.getApiClient(session).getAccountService()
                    .verifyCredentials(false, true, new com.twitter.sdk.android.core.Callback<User>() {

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                        @Override
                        public void success(Result<User> result) {
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void failure(TwitterException e) {
                            loadLoginScreen();
                            //Continues execution
                        }
                    });
        }
        else {
            loadLoginScreen();
        }
    }

    public void loadLoginScreen()
    {
        setContentView(R.layout.activity_login);

        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                TwitterSession session = result.data;

                //Añadido para Twitter4j
                Twitter4JFactory.generateInstance(TWITTER_KEY,TWITTER_SECRET,
                        session.getAuthToken().token,session.getAuthToken().secret);

                //Guardado en sharedpreferences el id de usuario
                SharedPreferences sp = getSharedPreferences("blow", Context.MODE_PRIVATE);
                sp.edit().putLong("id",session.getUserId()).commit();

                // TODO: Remove toast and use the TwitterSession's userID
//                String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
                String msg = "@" + session.getUserName() + " logged in! (#" + sp.getLong("id",0) + ")";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void failure(TwitterException exception) {
                ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo i = conMgr.getActiveNetworkInfo();
                if(i == null) {
                    Toast.makeText(LoginActivity.this,"No se ha podido conectar a Internet",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "No se ha podido conectar a Twitter," +
                            " Intentelo de nuevo", Toast.LENGTH_SHORT).show();
                }
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Make sure that the loginButton hears the result from any
        // Activity that it triggered.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }
}
