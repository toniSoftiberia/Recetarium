package com.softiberia.recetarium;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentReceptaCrearIngredient extends Fragment {


    Boolean modificantIng, modificantRec, afegirDades;
    Spinner sp_escala;
    String st_escala, st_id_ing;
    TextView tv_ing, tv_quan;
    Button b_crear, b_mod, b_treure;
    Httppostaux post;
    int id_ing_rec, id_lay;
    private ProgressDialog pDialog;

    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_connect="http://"+IP_Server+"/droidlogin/adding.php";//ruta en donde estan nuestros archivos
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receptes_crear_ingredient, container, false);
        super.onCreate(savedInstanceState);

        modificantIng=false;
        afegirDades=false;
        post=new Httppostaux();
        sp_escala = (Spinner) view.findViewById(R.id.spinner_escala);
        tv_quan = (TextView) view.findViewById(R.id.cr_quan);
        tv_ing = (TextView) view.findViewById(R.id.cr_ing);
        b_crear = (Button)view.findViewById(R.id.BAfegir);
        b_mod = (Button)view.findViewById(R.id.BMod);
        b_treure = (Button)view.findViewById(R.id.Btreure);

        Bundle bundle=getArguments();
        //Obtenemos datos enviados en el intent.
        id_lay = 0;
        if (bundle != null) {
            modificantRec = bundle.getBoolean("modificant");
            afegirDades = bundle.getBoolean("afegir_dades");
            id_lay = bundle.getInt("id_lay");
            id_ing_rec = bundle.getInt("id_ing_rec");
            Log.e("Bundle ", "get bundle");
            Log.e("Bundle id_lay ", ""+id_lay);
        }

        loadSpinnerEscala();
        if ( afegirDades == true){
            modificantIng = true;
            mostrarDades(bundle.getString("quantitat"),bundle.getString("nom"),bundle.getString("escala"));
            disableElements();
        }

        b_crear.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                String st_quan=tv_quan.getText().toString();
                String st_ing=tv_ing.getText().toString();

                if(st_quan.equals("")) st_quan="0";

                int int_quan=Integer.valueOf(st_quan);

                String stcheck = checkdata( st_ing, int_quan);
                //verificamos si estan en blanco
                if( stcheck.equals("ok")){
                    //si pasamos esa validacion ejecutamos el asynctask
                    new asyncCrear().execute( st_ing, String.valueOf(int_quan));
                }else{
                    //si detecto un error en la primera validacion vibrar y mostrar un Toast con un mensaje de error.
                    error(stcheck);
                }
            }
        });

        b_mod.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                ableElements();
                modificantIng = true;
            }
        });

        b_treure.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                if (id_lay>0){
                    borrarIngredient();
                    Log.e("IdIngredient", "" + id_lay);

                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentReceptaCrearIngredient frci = (FragmentReceptaCrearIngredient)fragmentManager
                            .findFragmentByTag("ing"+id_lay);
                    getFragmentManager().beginTransaction()
                            .hide(frci)
                            .commit();
                }
            }
        });

        return view;
    }

    public int getIndexArray(String[] arr, String cad) {
        //Aqui pueden usar el método de búsqueda que necesiten.
        int index=-1;
        for(int i=0;i<arr.length;i++)
        {
            if(arr[i] == null ? cad == null : arr[i].equals(cad)){
                index=i;
                break;
            }
        }
        return index;
    }

    public void mostrarDades(String quantitat ,String nom ,String escala){

        tv_ing.setText(nom);
        tv_quan.setText(quantitat);
        int sel=getIndexArray(getResources().getStringArray(R.array.escales), escala);
        Log.e("index-escales",String.valueOf(sel) );
        sp_escala.setSelection(sel);

    }

    //validamos si no hay ningun campo en blanco
    public String checkdata(String st_ing ,int int_quan ){
        if (st_ing.equals("")){
            return "No has introduït l'ingredient";
        }else if (int_quan <= 0){
            return "No has introduït la quantitat";
        }else if (st_escala.equals("--")){
            return "No has introduït l'escala";
        }else{
            return "ok";
        }
    }

    //vibra y muestra un Toast
    public void error(String strerror){
        Vibrator vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);

        Context context = getActivity().getApplicationContext();
        Toast toast1 = Toast.makeText(context,"Error: "+strerror, Toast.LENGTH_SHORT);
        toast1.show();
    }


    public void guardarIngredient(String stiding, String stquan){
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        if(modificantRec==false){
            FragmentReceptaCrear frc = (FragmentReceptaCrear)fragmentManager
                    .findFragmentByTag("FragmentReceptaCrear");
            fragmentManager.beginTransaction();
            frc.setNumIng(id_lay+1);
            frc.setIngredient(id_lay, stiding, stquan, st_escala, modificantIng);
        }else{
            FragmentReceptaModificar frm = (FragmentReceptaModificar)fragmentManager
                    .findFragmentByTag("FragmentReceptaModificar");
            fragmentManager.beginTransaction();
            frm.setNumIng(id_lay+1);
            frm.setIngredient(id_lay,id_ing_rec, stiding, stquan, st_escala, modificantIng, !afegirDades);
        }
        getFragmentManager().beginTransaction().commit();
    }

    public void borrarIngredient(){
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        if(modificantRec==false){
            FragmentReceptaCrear frc = (FragmentReceptaCrear)fragmentManager
                    .findFragmentByTag("FragmentReceptaCrear");
            fragmentManager.beginTransaction();
            frc.deleteIngredient(id_lay);
        }else{
            FragmentReceptaModificar frm = (FragmentReceptaModificar)fragmentManager
                    .findFragmentByTag("FragmentReceptaModificar");
            fragmentManager.beginTransaction();
            frm.deleteIngredient(id_lay);
        }
        getFragmentManager().beginTransaction().commit();
    }

    public void disableElements(){
        sp_escala.setEnabled(false);
        tv_quan.setEnabled(false);
        tv_ing.setEnabled(false);
        b_mod.setEnabled(true);
        b_treure.setEnabled(true);
        b_crear.setEnabled(false);
    }

    public void ableElements(){
        sp_escala.setEnabled(true);
        tv_quan.setEnabled(true);
        tv_ing.setEnabled(true);
        b_mod.setEnabled(false);
        b_treure.setEnabled(false);
        b_crear.setEnabled(true);
    }


    private void loadSpinnerEscala() {
        // Create an ArrayAdapter using the string array and a default spinner
        // layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.escales, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sp_escala.setAdapter(adapter);

        // This activity implements the AdapterView.OnItemSelectedListener
        sp_escala.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                st_escala = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }


    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public int createstatus(String ingredient ) {
        int id_ingredient;

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
        Global g = (Global)getActivity().getApplication();

        postparameters2send.add(new BasicNameValuePair("ingredient",ingredient));
        Log.e("ingredient",ingredient );

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_connect);
        //si lo que obtuvimos no es null

        id_ingredient=-1;
        if (jdata!=null && jdata.length() > 0){
            JSONObject json_data; //creamos un objeto JSON
            try {
                json_data = jdata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
                id_ingredient=json_data.getInt("id_ingredient");//accedemos al valor
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return id_ingredient;
    }

    /*		CLASE ASYNCTASK
 *
 * usaremos esta para poder mostrar el dialogo de progreso mientras enviamos y obtenemos los datos
 * podria hacerse lo mismo sin usar esto pero si el tiempo de respuesta es demasiado lo que podria ocurrir
 * si la conexion es lenta o el servidor tarda en responder la aplicacion sera inestable.
 * ademas observariamos el mensaje de que la app no responde.
 */

    class asyncCrear extends AsyncTask< String, String, String> {

        String sting, stquan;
        int id_ingredient;
        protected void onPreExecute() {
            //para el progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Creant ingredient....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass
            sting=params[0];
            stquan=params[1];

            String resultat;
            id_ingredient=createstatus(sting);
            //enviamos y recibimos y analizamos los datos en segundo plano.


            if(id_ingredient>=0){
                resultat="ok";
            }else{
                resultat="errBD";
            }
            return resultat;

        }

        /*Una vez terminado doInBackground segun lo que halla ocurrido
        pasamos a la sig. activity
        o mostramos error*/

        protected void onPostExecute(String resultat) {

            pDialog.dismiss();//ocultamos progess dialog.

            if(resultat.equals("ok")){
                guardarIngredient(String.valueOf(id_ingredient), stquan);
                disableElements();
                if( modificantIng == false){
                    String newFrag = "ing"+String.valueOf(id_lay+1);
                    Log.e("IdIngredientnewFrag", newFrag);
                    FragmentReceptaCrearIngredient frc = new FragmentReceptaCrearIngredient();
                    Bundle args = new Bundle();
                    args.putInt("id_lay",id_lay+1);
                    args.putInt("id_ing_rec",id_ing_rec+1);
                    args.putBoolean("modificant",modificantRec);
                    frc.setArguments(args);
                    getFragmentManager().beginTransaction()
                            .add(R.id.lay_ing, frc, newFrag )
                            .commit();
                }
            }else if(resultat.equals("errBD")) {
                error("No existeix connexió amb la base de dades");
            }
        }

    }

}