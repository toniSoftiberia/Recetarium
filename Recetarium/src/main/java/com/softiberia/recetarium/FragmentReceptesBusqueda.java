package com.softiberia.recetarium;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/*PANTALLA DE BIENVENIDA*/
public class FragmentReceptesBusqueda extends Fragment {

    ListView lista;
    TextView titol;
    int num_rec;
    Global g;
    ArrayList<LlistatEntrada>  total_datos;

    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receptes_valoracions, container, false);
        super.onCreate(savedInstanceState);

        lista = (ListView) view.findViewById(R.id.ListView_listado);
        titol = (TextView) view.findViewById(R.id.titol);
        g = (Global)getActivity().getApplication();

        titol.setText("Receptes trobades: "+num_rec);

        lista.setAdapter(new LlistatAdaptador(getActivity(),
                R.layout.fragment_receptes_llistat_entrada,
                total_datos){
            @Override
            public void onEntrada(Object entrada, View view) {
                if (entrada != null) {
                    TextView texto_entrada = (TextView) view.findViewById(R.id.textView_titulo);
                    if (texto_entrada != null)
                        texto_entrada.setText(((LlistatEntrada) entrada).get_texto());

                    ImageView imagen_entrada = (ImageView) view.findViewById(R.id.imageView_miniatura);
                    if (imagen_entrada != null)
                        imagen_entrada.setImageBitmap(((LlistatEntrada) entrada).get_idImagen());
                }
            }
        });

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                LlistatEntrada elegido = (LlistatEntrada) pariente.getItemAtPosition(posicion);
                g.setRecId(elegido.get_id());
                g.setRecName(elegido.get_texto());
                g.setRecAutorId(elegido.get_idAutor());
                FragmentReceptaVisualitzar fragmentV  = new FragmentReceptaVisualitzar();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentV,"FragmentReceptaVisualitzar")
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
	 }


    //Omple els camps amb les dades obtingudes de la base de dades
    public void setData(ArrayList<LlistatEntrada>  total_datos, int num_rec){
        this.total_datos = total_datos;
        this.num_rec = num_rec;
    }

}
