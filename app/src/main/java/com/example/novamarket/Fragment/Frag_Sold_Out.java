package com.example.novamarket.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.novamarket.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Frag_Sold_Out#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Frag_Sold_Out extends Fragment {


    public Frag_Sold_Out() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static Frag_Sold_Out newInstance() {
        Frag_Sold_Out fragment = new Frag_Sold_Out();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_frag__buy, container, false);
    }
}