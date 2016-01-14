package com.trabajo.sdm.blow.utility;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.trabajo.sdm.blow.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Fabio on 04/01/2016.
 */
public class TwitterUserAdapter extends ArrayAdapter<User> {

    private final Activity context;
    private final List<User> users;
    private TwitterSession session;


    public TwitterUserAdapter(Activity context, List<User> users, TwitterSession session) {
        super(context, R.layout.rowuser, users);

        this.context = context;
        this.users = users;
        this.session = session;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.rowuser, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.username);
        //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        CircleImageView imageView = (CircleImageView) rowView.findViewById(R.id.icon);
        TextView extratxt = (TextView) rowView.findViewById(R.id.userat);
        final Button btUnfollow = (Button) rowView.findViewById(R.id.btUnfollow);

        btUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((Long[]) btUnfollow.getTag())[1] == Long.valueOf(1))
                    unfollowUser(btUnfollow);
                else
                    followUser(btUnfollow);

            }
        });

        URL url;
        try {
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            url = new URL(users.get(position).profileImageUrl.replace("normal", "bigger"));
            imageView.setImageBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()));
            txtTitle.setText(users.get(position).name);
            extratxt.setText("@" + users.get(position).screenName);
            Long[] tag = new Long[]{users.get(position).getId(), Long.valueOf(1)};
            btUnfollow.setTag(tag);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rowView;
    }

    private void unfollowUser(final Button btUnfollow) {
        new MyTwitterApiClient(session).getUnfollowService().show(((Long[]) btUnfollow.getTag())[0], null, new Callback<MyTwitterApiClient.Followers>() {
            @Override
            public void success(Result<MyTwitterApiClient.Followers> result) {
                Log.i("----------------", "unfollow");
                /*for (User u : nofollowers) {
                    if (u.getId() == (Long) btUnfollow.getTag()) {
                        nofollowers.remove(u);
                        notifyDataSetChanged();
                        break;
                    }
                }*/
                btUnfollow.setText("FOLLOW");
                btUnfollow.setTag(new Long[]{((Long[]) btUnfollow.getTag())[0], Long.valueOf(0)});
                btUnfollow.setTextColor(Color.parseColor("#05d7bf"));
                StateListDrawable gradientDrawable = (StateListDrawable) btUnfollow.getBackground();
                DrawableContainer.DrawableContainerState drawableContainerState = (DrawableContainer.DrawableContainerState) gradientDrawable.getConstantState();
                Drawable[] children = drawableContainerState.getChildren();
                GradientDrawable selectedDrawable = (GradientDrawable) children[0];
                selectedDrawable.setStroke(3, Color.parseColor("#05d7bf"));
                Toast.makeText(getContext(),"Unfollowed",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
                Toast.makeText(getContext(),"No se ha podido dejar de seguir al usuario",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void followUser(final Button btUnfollow) {
        new MyTwitterApiClient(session).getFollowService().show(((Long[]) btUnfollow.getTag())[0], null, new Callback<MyTwitterApiClient.Followers>() {
            @Override
            public void success(Result<MyTwitterApiClient.Followers> result) {
                Log.i("----------------", "follow");

                btUnfollow.setText("UNFOLLOW");
                btUnfollow.setTag(new Long[]{((Long[]) btUnfollow.getTag())[0], Long.valueOf(1)});
                btUnfollow.setTextColor(Color.parseColor("#ff5252"));
                StateListDrawable gradientDrawable = (StateListDrawable) btUnfollow.getBackground();
                DrawableContainer.DrawableContainerState drawableContainerState = (DrawableContainer.DrawableContainerState) gradientDrawable.getConstantState();
                Drawable[] children = drawableContainerState.getChildren();
                GradientDrawable selectedDrawable = (GradientDrawable) children[0];
                selectedDrawable.setStroke(3, Color.parseColor("#ff5252"));
                Toast.makeText(getContext(),"Followed",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
                Toast.makeText(getContext(),"No se ha podido seguir al usuario",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}