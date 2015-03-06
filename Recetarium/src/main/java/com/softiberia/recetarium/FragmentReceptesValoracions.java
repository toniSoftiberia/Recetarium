package com.softiberia.recetarium;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

/*PANTALLA DE BIENVENIDA*/
public class FragmentReceptesValoracions extends Fragment {

    ListView lista;
    TextView titol;
    ArrayList<ValoracioEntrada>  total_datos;

    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receptes_valoracions, container, false);
        super.onCreate(savedInstanceState);

        lista = (ListView) view.findViewById(R.id.ListView_listado);
        titol = (TextView) view.findViewById(R.id.titol);

        lista.setAdapter(new LlistatAdaptador(getActivity(),
                R.layout.fragment_receptes_visualitzar_valoracio,
                total_datos){
            @Override
            public void onEntrada(Object entrada, View view) {
                if (entrada != null) {

                    TextView tv_autor = (TextView) view.findViewById(R.id.tv_autor);
                    if (tv_autor != null)
                        tv_autor.setText(((ValoracioEntrada) entrada).get_emisor());

                    TextView tv_comentari = (TextView) view.findViewById(R.id.tv_comentari);
                    if (tv_comentari != null)
                        tv_comentari.setText(((ValoracioEntrada) entrada).get_hora()+" "+
                                ((ValoracioEntrada) entrada).get_data()+": "+
                                ((ValoracioEntrada) entrada).get_comentari());

                    RatingBar rb_valoracio = (RatingBar) view.findViewById(R.id.rb_valoracio);
                    if (rb_valoracio != null)
                        rb_valoracio.setRating((float)((ValoracioEntrada) entrada).get_nota());
                }
            }
        });
       return view;
	 }


    //Omple els camps amb les dades obtingudes de la base de dades
    public void setTotalDatos(ArrayList<ValoracioEntrada>  total_datos){
        this.total_datos = total_datos;
    }

}
