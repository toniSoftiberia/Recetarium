package com.softiberia.recetarium;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentReceptaCrearInstruccio extends Fragment {

    Boolean modificantIns, modificantRec, afegirDades;
    TextView tv_ins;
    EditText text_inst;
    Button b_crear, b_mod, b_treure;
    int id_instruccio,id_lay;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receptes_crear_instruccio, container, false);
        super.onCreate(savedInstanceState);

        modificantIns=false;
        afegirDades=false;
        tv_ins = (TextView) view.findViewById(R.id.cr_idins);
        text_inst = (EditText) view.findViewById(R.id.cr_ins);
        b_crear = (Button)view.findViewById(R.id.BAfegir);
        b_mod = (Button)view.findViewById(R.id.BMod);
        b_treure = (Button)view.findViewById(R.id.Btreure);

        Bundle bundle=getArguments();
        //Obtenemos datos enviados en el intent.
        if (bundle != null) {
            modificantRec = bundle.getBoolean("modificant");
            afegirDades = bundle.getBoolean("afegir_dades");
            id_lay = bundle.getInt("id_lay");
            id_instruccio = bundle.getInt("id_instruccio");
        }

        tv_ins.setText("Instrucció: "+(id_lay+1));

        if ( afegirDades == true){
            modificantIns = true;
            text_inst.setText(bundle.getString("text"));
            disableElements();
        }

        b_crear.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){

                String st_ins=text_inst.getText().toString();
                //verificamos si estan en blanco
                if(! st_ins.equals("")){

                    //si pasamos esa validacion
                    Context context = getActivity().getApplicationContext();
                    Toast toast1 = Toast.makeText(context,"Enhorabona, Instrucció afegida", Toast.LENGTH_SHORT);
                    toast1.show();
                    guardarInstruccio(st_ins);
                    disableElements();
                    if( modificantIns == false){
                        String newFrag = "ing"+String.valueOf(id_lay+1);
                        Log.e("IdInstruccionewFrag", newFrag);
                        FragmentReceptaCrearInstruccio frc = new FragmentReceptaCrearInstruccio();
                        Bundle args = new Bundle();
                        args.putInt("id_lay", id_lay + 1);
                        args.putInt("id_instruccio", id_instruccio + 1);
                        args.putBoolean("modificant",modificantRec);
                        frc.setArguments(args);
                        getFragmentManager().beginTransaction()
                                .add(R.id.lay_ins, frc, newFrag )
                                .commit();
                    }
                }else{
                    //si detecto un error en la primera validacion vibrar y mostrar un Toast con un mensaje de error.
                    error("La instrucció no pot estar en blanc");
                }
            }
        });

        b_mod.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                ableElements();
                modificantIns = true;
            }
        });

        b_treure.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){

                Log.e("IdInstruccio", "" + id_lay);
                if (id_lay>0){
                    borrarInstruccio();
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentReceptaCrearInstruccio frci = (FragmentReceptaCrearInstruccio)fragmentManager
                            .findFragmentByTag("ins" + id_lay);
                    getFragmentManager().beginTransaction()
                            .hide(frci)
                            .commit();
                }
            }
        });

        return view;
    }

    //vibra y muestra un Toast
    public void error(String strerror){
        Vibrator vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);

        Context context = getActivity().getApplicationContext();
        Toast toast1 = Toast.makeText(context,"Error: "+strerror, Toast.LENGTH_SHORT);
        toast1.show();
    }


    public void guardarInstruccio(String stins){
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        if(modificantRec==false){
            FragmentReceptaCrear frc = (FragmentReceptaCrear)fragmentManager
                    .findFragmentByTag("FragmentReceptaCrear");
                fragmentManager.beginTransaction();
                frc.setNumIns(id_lay+1);
                frc.setInstruccio(id_lay, stins , modificantIns);
        }else{
            FragmentReceptaModificar frm = (FragmentReceptaModificar)fragmentManager
                    .findFragmentByTag("FragmentReceptaModificar");
            fragmentManager.beginTransaction();
            frm.setNumIns(id_lay+1);
            frm.setInstruccio(id_lay,id_instruccio, stins , modificantIns, !afegirDades);
        }
        getFragmentManager().beginTransaction().commit();

    }

    public void borrarInstruccio(){
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        if(modificantRec==false){
            FragmentReceptaCrear frc = (FragmentReceptaCrear)fragmentManager
                    .findFragmentByTag("FragmentReceptaCrear");
            fragmentManager.beginTransaction();
            frc.deleteInstruccio(id_lay);
        }else{int calcul_id_lay=id_lay;
            if(afegirDades){

            }
            FragmentReceptaModificar frm = (FragmentReceptaModificar)fragmentManager
                    .findFragmentByTag("FragmentReceptaModificar");
            fragmentManager.beginTransaction();
            frm.deleteInstruccio(calcul_id_lay);
        }
        getFragmentManager().beginTransaction().commit();
    }

    public void disableElements(){
        text_inst.setEnabled(false);
        b_mod.setEnabled(true);
        b_treure.setEnabled(true);
        b_crear.setEnabled(false);
    }

    public void ableElements(){
        text_inst.setEnabled(true);
        b_mod.setEnabled(false);
        b_treure.setEnabled(false);
        b_crear.setEnabled(true);
    }

}