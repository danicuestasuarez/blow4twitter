package com.trabajo.sdm.blow.modules;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.trabajo.sdm.blow.R;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 */
public class NoFollowerFragment extends Fragment{


    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    String[] texts= new String []{"Aqui","Va","La","Gente","Que","No","Te","Sigue"};

    // TODO: Rename and change types of parameters
    public static NoFollowerFragment newInstance(String param1, String param2) {
        NoFollowerFragment fragment = new NoFollowerFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoFollowerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Change Adapter to display your content

        mAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, texts);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nofollower, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        (mListView).setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
