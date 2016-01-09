package com.trabajo.sdm.blow.modules;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Barri on 08/01/2016.
 */
public class Twitter4JFactory {
    private static Twitter instance;

    private Twitter4JFactory() {

    }

    public static void generateInstance(String consumerKey, String consumerSecret,
                                 String oauthToken, String oauthSecret)
    {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(oauthToken) //esto es para pruebas de dar permisos a esta cuenta
                .setOAuthAccessTokenSecret(oauthSecret);

        TwitterFactory tf = new TwitterFactory(cb.build());
        instance = tf.getInstance();
    }

    public static Twitter getInstance() {
        return instance;
    }
}
