package com.softiberia.recetarium;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

public class FragmentReceptaVisualitzarValoracio extends Fragment {

    TextView tv_autor, tv_comentari;
    RatingBar rb_valoracio;
    float val;
    String autor, comentari;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receptes_visualitzar_valoracio, container, false);
        super.onCreate(savedInstanceState);


        tv_autor = (TextView) view.findViewById(R.id.tv_autor);
        tv_comentari = (TextView) view.findViewById(R.id.tv_comentari);
        rb_valoracio = (RatingBar) view.findViewById(R.id.rb_valoracio);

        Bundle bundle=getArguments();
        //Obtenemos datos enviados en el intent.
        if (bundle != null) {
            val = bundle.getFloat("not_valoracio");
            autor = bundle.getString("not_autor");
            comentari = bundle.getString("not_comentari");
        }

        tv_autor.setText(autor);
        tv_comentari.setText(comentari);
        rb_valoracio.setRating(val);
        return view;
    }
}