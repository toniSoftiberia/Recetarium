package com.softiberia.recetarium;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainFragmentReceptes extends Fragment {

    Spinner spcatego, spcoccio, spdificu, spidioma, sptipusp, sping1, sping2, sping3;
    String stcatego, stcoccio, stdificu, stidioma, sttipusp, sting1, sting2, sting3;
    LinearLayout lay_auto, lay_cocc, lay_come, lay_cost, lay_difi, lay_idio, lay_ingr1, lay_ingr2, lay_ingr3, lay_temp, lay_tipu, lay_valo;
    CheckBox checkadv;
    TextView comensal, cost, tempm, temph, paraula, autor;
    RatingBar rbval;
    Button bcerca;
    Httppostaux post;
    ArrayList<String> ingredients;
    ArrayList<LlistatEntrada> receptes;
    int num_rec;
    private ProgressDialog pDialog, pDialog2;
    Global g;

    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_connect="http://"+IP_Server+"/droidlogin/seling.php";//ruta en donde estan nuestros archivos
    String URL_search="http://"+IP_Server+"/droidlogin/selectreceptescerca.php";//ruta en donde estan nuestros archivos
    String URL_searchprop="http://"+IP_Server+"/droidlogin/selectreceptescercaprop.php";//ruta en donde estan nuestros archivos
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receptes, container, false);
        super.onCreate(savedInstanceState);

        g = (Global)getActivity().getApplication();
        g.setMenuMode(0);


        num_rec=0;
        post=new Httppostaux();
        spcatego = (Spinner) view.findViewById(R.id.spinner_cat);
        spcoccio = (Spinner) view.findViewById(R.id.spinner_coc);
        spdificu = (Spinner) view.findViewById(R.id.spinner_dif);
        spidioma = (Spinner) view.findViewById(R.id.spinner_idi);
        sptipusp = (Spinner) view.findViewById(R.id.spinner_tip);
        sping1 = (Spinner) view.findViewById(R.id.spinner_ing1);
        sping2 = (Spinner) view.findViewById(R.id.spinner_ing2);
        sping3 = (Spinner) view.findViewById(R.id.spinner_ing3);
        rbval = (RatingBar) view.findViewById(R.id.ratingRec);
        comensal = (TextView) view.findViewById(R.id.rc_comen);
        cost = (TextView) view.findViewById(R.id.rc_cost);
        tempm = (TextView) view.findViewById(R.id.rc_temm);
        temph = (TextView) view.findViewById(R.id.rc_temph);
        paraula = (TextView) view.findViewById(R.id.rc_paraula);
        autor = (TextView) view.findViewById(R.id.rc_autor);

        lay_auto = (LinearLayout) view.findViewById(R.id.lay_aut);
        lay_cocc = (LinearLayout) view.findViewById(R.id.lay_coc);
        lay_come = (LinearLayout) view.findViewById(R.id.lay_com);
        lay_cost = (LinearLayout) view.findViewById(R.id.lay_cos);
        lay_difi = (LinearLayout) view.findViewById(R.id.lay_dif);
        lay_idio = (LinearLayout) view.findViewById(R.id.lay_idi);
        lay_ingr1 = (LinearLayout) view.findViewById(R.id.lay_ing1);
        lay_ingr2 = (LinearLayout) view.findViewById(R.id.lay_ing2);
        lay_ingr3 = (LinearLayout) view.findViewById(R.id.lay_ing3);
        lay_temp = (LinearLayout) view.findViewById(R.id.lay_tem);
        lay_tipu = (LinearLayout) view.findViewById(R.id.lay_tip);
        lay_valo = (LinearLayout) view.findViewById(R.id.lay_val);
        checkadv = (CheckBox)view.findViewById(R.id.checkAdv);
        bcerca = (Button)view.findViewById(R.id.Bcerca);
        ingredients = new ArrayList<String>();
        receptes = new ArrayList<LlistatEntrada>();

        loadSpinnerCategorias();
        loadSpinnerCoocio();
        loadSpinnerDificultat();
        loadSpinnerIdioma();
        loadSpinnerTipusPlat();

        checkadv.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                if(checkadv.isChecked()){
                    new asyncSearchIngredients().execute();
                    setAdvMod();

                }else{
                    setNoAdvMod();
                }
            }
        });

        bcerca.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                new asyncSearchReceptes().execute();
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

    private void setAdvMod() {
        lay_auto.setVisibility(View.VISIBLE);
        lay_cocc.setVisibility(View.VISIBLE);
        lay_come.setVisibility(View.VISIBLE);
        lay_cost.setVisibility(View.VISIBLE);
        lay_difi.setVisibility(View.VISIBLE);
        lay_idio.setVisibility(View.VISIBLE);
        lay_ingr1.setVisibility(View.VISIBLE);
        lay_ingr2.setVisibility(View.VISIBLE);
        lay_ingr3.setVisibility(View.VISIBLE);
        lay_temp.setVisibility(View.VISIBLE);
        lay_tipu.setVisibility(View.VISIBLE);
        lay_valo.setVisibility(View.VISIBLE);
    }

    private void setNoAdvMod() {
        lay_auto.setVisibility(View.GONE);
        lay_cocc.setVisibility(View.GONE);
        lay_come.setVisibility(View.GONE);
        lay_cost.setVisibility(View.GONE);
        lay_difi.setVisibility(View.GONE);
        lay_idio.setVisibility(View.GONE);
        lay_ingr1.setVisibility(View.GONE);
        lay_ingr2.setVisibility(View.GONE);
        lay_ingr3.setVisibility(View.GONE);
        lay_temp.setVisibility(View.GONE);
        lay_tipu.setVisibility(View.GONE);
        lay_valo.setVisibility(View.GONE);
    }

    private void loadSpinnerCategorias() {
        // Create an ArrayAdapter using the string array and a default spinner
        // layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.categoria, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spcatego.setAdapter(adapter);

        // This activity implements the AdapterView.OnItemSelectedListener
        spcatego.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                stcatego = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    private void loadSpinnerCoocio() {
        // Create an ArrayAdapter using the string array and a default spinner
        // layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.metode_coccio, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spcoccio.setAdapter(adapter);

        // This activity implements the AdapterView.OnItemSelectedListener
        spcoccio.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                stcoccio = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    private void loadSpinnerDificultat() {
        // Create an ArrayAdapter using the string array and a default spinner
        // layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.dificultat, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spdificu.setAdapter(adapter);

        // This activity implements the AdapterView.OnItemSelectedListener
        spdificu.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                stdificu = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    private void loadSpinnerIdioma() {
        // Create an ArrayAdapter using the string array and a default spinner
        // layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.idioma_recepta, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spidioma.setAdapter(adapter);

        // This activity implements the AdapterView.OnItemSelectedListener
        spidioma.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                stidioma = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    private void loadSpinnerTipusPlat() {
        // Create an ArrayAdapter using the string array and a default spinner
        // layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.tipus_plat, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sptipusp.setAdapter(adapter);

        // This activity implements the AdapterView.OnItemSelectedListener
        sptipusp.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                sttipusp = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    private void mostrar_dades(){
        ArrayAdapter<CharSequence> adapter =  new ArrayAdapter(getActivity(),
                android.R.layout.simple_spinner_item, ingredients);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sping1.setAdapter(adapter);

        // This activity implements the AdapterView.OnItemSelectedListener
        sping1.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                sting1 = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        // Apply the adapter to the spinner
        sping2.setAdapter(adapter);

        // This activity implements the AdapterView.OnItemSelectedListener
        sping2.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                sting2 = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        // Apply the adapter to the spinner
        sping3.setAdapter(adapter);

        // This activity implements the AdapterView.OnItemSelectedListener
        sping3.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                sting3 = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    private ArrayList<NameValuePair> obtenir_dades(){
        ArrayList<NameValuePair> array =  new ArrayList<NameValuePair>();

        String stcomensal=comensal.getText().toString();
        String stcost=cost.getText().toString();
        String sttemh=temph.getText().toString();
        String sttemm=tempm.getText().toString();
        String stparaula=paraula.getText().toString();
        String stautor=autor.getText().toString();

        stcoccio = spcoccio.getSelectedItem().toString();
        stcatego = spcatego.getSelectedItem().toString();
        stdificu = spdificu.getSelectedItem().toString();
        stidioma = spidioma.getSelectedItem().toString();
        sttipusp = sptipusp.getSelectedItem().toString();
        if(checkadv.isChecked()){
            sting1 = sping1.getSelectedItem().toString();
            sting2 = sping2.getSelectedItem().toString();
            sting3 = sping3.getSelectedItem().toString();
        }

        if(stcomensal.equals("")) stcomensal="0";
        if(stcost.equals("")) stcost="0";
        if(sttemh.equals("")) sttemh="0";
        if(sttemm.equals("")) sttemm="0";

        int intcomensal=Integer.valueOf(stcomensal);
        int intcost=Integer.valueOf(stcost);
        int inttemp=((Integer.valueOf(sttemh)*60) + Integer.valueOf(sttemm));
        float flvalo = rbval.getRating();

        if (!stparaula.equals(""))
            array.add(new BasicNameValuePair("paraula",stparaula));
        Log.e("obtenir_dades-stparaula=", "" + stparaula);
        if (!stcatego.equals("Tria una opció"))
            array.add(new BasicNameValuePair("categoria",stcatego));
        Log.e("obtenir_dades-stcatego=", "" + stcatego);
        if(checkadv.isChecked()){
            if (!(intcomensal <= 0))
                array.add(new BasicNameValuePair("comensal",String.valueOf(intcomensal)));
            Log.e("obtenir_dades-comensal=", "" + String.valueOf(intcomensal));
            if (!(intcost <= 0))
                array.add(new BasicNameValuePair("cost",String.valueOf(intcost)));
            Log.e("obtenir_dades-cost=", "" + String.valueOf(intcost));
            if (!(inttemp <= 0))
                array.add(new BasicNameValuePair("temp",String.valueOf(inttemp)));
            Log.e("obtenir_dades-temp=", "" + String.valueOf(inttemp));
            if (!stcoccio.equals("Tria una opció"))
                array.add(new BasicNameValuePair("coccio",stcoccio));
            Log.e("obtenir_dades-stcoccio=", "" + stcoccio);
            if (!stdificu.equals("Tria una opció"))
                array.add(new BasicNameValuePair("dificultat",stdificu));
            Log.e("obtenir_dades-stdificu=", "" + stdificu);
            if (!stidioma.equals("Tria una opció"))
                array.add(new BasicNameValuePair("idioma",stidioma));
            Log.e("obtenir_dades-stidioma=", "" + stidioma);
            if (!sttipusp.equals("Tria una opció"))
                array.add(new BasicNameValuePair("tipus",sttipusp));
            Log.e("obtenir_dades-sttipusp=", "" + sttipusp);
            if (!sting1.equals("--"))
                array.add(new BasicNameValuePair("ing1",sting1));
            Log.e("obtenir_dades-sting1=", "" + sting1);
            if (!sting2.equals("--"))
                array.add(new BasicNameValuePair("ing2",sting2));
            Log.e("obtenir_dades-sting2=", "" + sting2);
            if (!sting3.equals("--"))
                array.add(new BasicNameValuePair("ing3",sting3));
            Log.e("obtenir_dades-sting3=", "" + sting3);
            if (!stautor.equals(""))
                array.add(new BasicNameValuePair("autor",stautor));
            Log.e("obtenir_dades-stautor=", "" + stautor);
            if (!(flvalo <= 0))
                array.add(new BasicNameValuePair("valoracio",String.valueOf(flvalo)));
            Log.e("obtenir_dades-flvalo=", "" + String.valueOf(flvalo));
        }
        if(g.getModeProp())
            array.add(new BasicNameValuePair("id_cuiner",g.getUserId()));
        Log.e("obtenir_dades-id_cuiner=", "" + g.getUserId());
        return array;
    }

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public JSONObject ingstatus() {
        JSONObject json_data=null; //creamos un objeto JSON

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_connect);


        //si lo que obtuvimos no es null
        ingredients.add("--");
        int i=0;
        while (jdata!=null && jdata.length() > 0 && i<jdata.length()){
            try {
                json_data = jdata.getJSONObject(i++); //leemos el primer segmento en nuestro caso el unico
                ingredients.add(json_data.getString("nom"));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                json_data=null;
            }
        }

        return json_data;

    }

    class asyncSearchIngredients extends AsyncTask< String, String, String > {

        String user;
        JSONObject data=null;
        protected void onPreExecute() {//para el progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Buscant ingredeints....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass

            //enviamos y recibimos y analizamos los datos en segundo plano.
            data=ingstatus();
            Log.e("doInBackground-status=", "" + data);
            if (data!=null){
                return "ok";
            }else{
                return "errBD";
            }
        }

        /*Una vez terminado doInBackground segun lo que halla ocurrido
        pasamos a la sig. activity
        o mostramos error*/

        protected void onPostExecute(String result) {
            pDialog.dismiss();//ocultamos progess dialog.
            Log.e("onPostExecute=", "" + result);
            if (result.equals("ok")){
                mostrar_dades();
            }else{
                error("No s'han pogut trobar ingredients");
            }

        }

    }

    public JSONObject recstatus() {
        JSONObject json_data=null; //creamos un objeto JSON

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send = obtenir_dades();

        //realizamos una peticion y como respuesta obtenes un array JSON
        Log.e("Mode: ", ""+g.getModeProp());
        String URL = URL_search;
        if(g.getModeProp()) URL = URL_searchprop;
        JSONArray jdata=post.getserverdata(postparameters2send, URL);


        //si lo que obtuvimos no es null
        int i=0;
        while (jdata!=null && jdata.length() > 0 && i<jdata.length()){
            try {
                json_data = jdata.getJSONObject(i++); //leemos el primer segmento en nuestro caso el unico
                Bitmap foto = BitmapFactory.decodeResource(getResources(), R.drawable.receta);
                if(json_data.getString("foto_min")!="null"){
                    byte[] decodedString = Base64.decode(json_data.getString("foto_min"), Base64.NO_WRAP);
                    foto = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                }
                LlistatEntrada Ll = new LlistatEntrada(foto, json_data.getString("titol"), json_data.getString("id_recepta"));
                Log.e("i = ", (i - 1) + " titol = " + json_data.getString("titol"));
                Ll.set_idAutor(json_data.getString("id_cuiner"));
                receptes.add(Ll);
                num_rec++;
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                json_data=null;
            }
        }

        return json_data;

    }

    class asyncSearchReceptes extends AsyncTask< String, String, String > {

        JSONObject data=null;
        protected void onPreExecute() {//para el progress dialog
            pDialog2 = new ProgressDialog(getActivity());
            pDialog2.setMessage("Buscant receptes....");
            pDialog2.setIndeterminate(false);
            pDialog2.setCancelable(false);
            pDialog2.show();
        }

        protected String doInBackground(String... params) {
            //enviamos y recibimos y analizamos los datos en segundo plano.
            data=recstatus();
            Log.e("doInBackground-status=", "" + data);
            if (data!=null){
                return "ok";
            }else{
                return "errBD";
            }
        }

        /*Una vez terminado doInBackground segun lo que halla ocurrido
        pasamos a la sig. activity
        o mostramos error*/

        protected void onPostExecute(String result) {
            pDialog2.dismiss();//ocultamos progess dialog.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        pDialog.dismiss();//ocultamos progess dialog.
            Log.e("onPostExecute=", "" + result);
            if (result.equals("ok")){
                FragmentReceptesBusqueda fragment = new FragmentReceptesBusqueda();
                fragment.setData(receptes,num_rec);
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }else{
                error("No hi ha trobat cap recepta");
            }

        }

    }

}