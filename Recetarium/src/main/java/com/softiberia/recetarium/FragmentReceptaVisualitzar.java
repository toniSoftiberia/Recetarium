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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentReceptaVisualitzar extends Fragment {

    TextView tv_titol, tv_cuiner, tv_temps, tv_cost, tv_comensals, tv_dificultat, tv_tipus,
            tv_categoria, tv_coccio, tv_idioma, tv_ingredients, tv_instruccions, tv_observacio;
    ImageView img_receta;
    RatingBar r_valoracio;
    Button b_valoracions;
    LinearLayout lay_obs, lay_val, list_vals;
    int num_ing, num_ins;
    boolean mostrar_boto_vals;
    Httppostaux post;
    private ProgressDialog pDialog,pDialog2;
    Global g;

    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_select="http://"+IP_Server+"/droidlogin/selectrecepta.php";//ruta en donde estan nuestros archivos
    String URL_valoracions="http://"+IP_Server+"/droidlogin/selectvalsrecepta.php";//ruta en donde estan nuestros archivos
    ArrayList<ValoracioEntrada> datos, total_datos;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receptes_visualitzar, container, false);
        super.onCreate(savedInstanceState);
        datos = new ArrayList<ValoracioEntrada>();
        total_datos = new ArrayList<ValoracioEntrada>();
        post=new Httppostaux();
        lay_obs = (LinearLayout) view.findViewById(R.id.lay_obs);
        lay_val = (LinearLayout) view.findViewById(R.id.lay_val);
        tv_titol = (TextView) view.findViewById(R.id.v_titol);
        tv_cuiner = (TextView) view.findViewById(R.id.v_cuiner);
        tv_temps = (TextView) view.findViewById(R.id.v_temps);
        tv_cost = (TextView) view.findViewById(R.id.v_cost);
        tv_comensals = (TextView) view.findViewById(R.id.v_comensals);
        tv_dificultat = (TextView) view.findViewById(R.id.v_dificultat);
        tv_tipus = (TextView) view.findViewById(R.id.v_tipus);
        tv_categoria = (TextView) view.findViewById(R.id.v_categoria);
        tv_coccio = (TextView) view.findViewById(R.id.v_coccio);
        tv_idioma = (TextView) view.findViewById(R.id.v_idoma);
        tv_ingredients = (TextView) view.findViewById(R.id.v_ingredients);
        tv_instruccions = (TextView) view.findViewById(R.id.v_instruccions);
        tv_observacio = (TextView) view.findViewById(R.id.v_observacio);
        img_receta = (ImageView) view.findViewById(R.id.recepta_img);
        b_valoracions = (Button) view.findViewById(R.id.Bvaloracions);
        r_valoracio = (RatingBar) view.findViewById(R.id.v_valoracio);
        list_vals = (LinearLayout)view.findViewById(R.id.lay_val2);
        mostrar_boto_vals = false;
        g = (Global)getActivity().getApplication();

        if(g.getUserLog()){
            ((MainActivity)getActivity()).desactivarOpcions();
            if(g.getModeProp()){
                g.setMenuMode(4);
                ((MainActivity)getActivity()).activarOpcionsReceptaPropia();
            }else{
                g.setMenuMode(3);
                ((MainActivity)getActivity()).activarOpcionsRecepta();
            }
        }
        else{
            g.setMenuMode(0);
        }

        new asyncSelectRecepta().execute(g.getRecId());

        new asyncSelectValoracions().execute();

        b_valoracions.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){

               FragmentReceptesValoracions fragment = new FragmentReceptesValoracions();
                fragment.setTotalDatos(total_datos);
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
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

    //Omple els camps amb les dades obtingudes de la base de dades
    public void mostrar_dades(JSONObject data_rec,JSONObject data_num_ing,JSONArray data_ing,JSONObject data_num_ins,JSONArray data_ins){
        try {
            tv_titol.setText(data_rec.getString("titol"));
            g.setRecName(data_rec.getString("titol"));
            tv_cuiner.setText(data_rec.getString("alias"));
            int in_temps = data_rec.getInt("temps");
            String st_temps="";
            if((in_temps / 60) > 0 ){
                st_temps = st_temps + (in_temps / 60) + "h ";
            }
            st_temps = st_temps + (in_temps % 60) + "min";
            tv_temps.setText("Temps: "+st_temps);
            tv_cost.setText("Cost: "+data_rec.getString("cost")+"€");
            tv_comensals.setText("Nº comensals: "+data_rec.getString("comensals"));
            tv_dificultat.setText("Dificultat: "+data_rec.getString("dificultat"));
            tv_tipus.setText("Tipus de plat: "+ data_rec.getString("tipus_plat"));
            tv_categoria.setText("Categoria: "+data_rec.getString("categoria"));
            tv_coccio.setText("Mètode de cocció: "+data_rec.getString("metode_coccio"));
            tv_idioma.setText("Idioma: "+data_rec.getString("idioma"));
            String st_observacio = data_rec.getString("observacio");
            if (st_observacio.equals(""))lay_obs.setVisibility(View.GONE);
            tv_observacio.setText(st_observacio);
            r_valoracio.setRating((float)data_rec.getLong("valoracio"));
            if(data_rec.getString("fotografia")!="null"){
                byte[] decodedString = Base64.decode(data_rec.getString("fotografia"), Base64.NO_WRAP);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                img_receta.setImageBitmap(decodedByte);
            }

            num_ing=data_num_ing.getInt("num_ing");
            String ingredients = "";
            int i;
            for (i =0; i<num_ing;){
                JSONObject json_ing = data_ing.getJSONObject(i);
                ingredients = ingredients + (i+1) + ". " ;
                ingredients = ingredients + json_ing.getString("nom") ;
                ingredients = ingredients + ", " ;
                ingredients = ingredients + json_ing.getString("quantitat");
                ingredients = ingredients + " " ;
                ingredients = ingredients + json_ing.getString("escala") ;
                i++;
                if(i!=num_ing) ingredients = ingredients + '\n' ;
            }
            tv_ingredients.setText(ingredients);


            num_ins=data_num_ins.getInt("num_ins");
            String instruccions = "";
            for (i =0; i<num_ins;){
                JSONObject json_ins = data_ins.getJSONObject(i);
                instruccions = instruccions + (i+1) + ". " ;
                instruccions = instruccions + json_ins.getString("text") ;
                i++;
                if(i!=num_ins) instruccions = instruccions + '\n' ;
            }
            tv_instruccions.setText(instruccions);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //Omple els camps amb les dades obtingudes de la base de dades
    public void mostrar_valoracions(){

        for (int i=0; i<datos.size(); i++) {
            FragmentReceptaVisualitzarValoracio frc = new FragmentReceptaVisualitzarValoracio();
            Bundle args = new Bundle();
            args.putFloat("not_valoracio",  datos.get(i).get_nota());
            args.putString("not_autor", datos.get(i).get_emisor());
            args.putString("not_comentari",datos.get(i).get_hora()+" "+
                                            datos.get(i).get_data()+" "+
                                            datos.get(i).get_comentari());
            frc.setArguments(args);
            getFragmentManager().beginTransaction()
                    .add(R.id.lay_val2, frc)
                    .commit();
        }
        if (mostrar_boto_vals) b_valoracions.setVisibility(View.VISIBLE);
    }

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public JSONArray receptastatus(String recepta) {


    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("id_recepta",recepta));

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_select);
        return jdata;

    }

    public JSONObject getRecepta (JSONArray arraydata){
        //si lo que obtuvimos no es null
        JSONObject data_rec = null;
        if (arraydata!=null && arraydata.length() > 0){
            try {
                data_rec = arraydata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return data_rec;
    }

    public JSONObject getNumIng (JSONArray arraydata){
        //si lo que obtuvimos no es null
        JSONObject data_num_ing = null;
        if (arraydata!=null && arraydata.length() > 0){
            try {
                data_num_ing = arraydata.getJSONObject(1); //leemos el primer segmento en nuestro caso el unico
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return data_num_ing;
    }

    public JSONArray getIngredients (JSONArray arraydata){
        //si lo que obtuvimos no es null
        JSONArray data_ing = null;
        if (arraydata!=null && arraydata.length() > 0){
            try {
                data_ing = arraydata.getJSONArray(2); //leemos el primer segmento en nuestro caso el unico
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return data_ing;
    }

    public JSONObject getNumIns (JSONArray arraydata){
        //si lo que obtuvimos no es null
        JSONObject data_num_ins = null;
        if (arraydata!=null && arraydata.length() > 0){
            try {
                data_num_ins = arraydata.getJSONObject(3); //leemos el primer segmento en nuestro caso el unico
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return data_num_ins;
    }

    public JSONArray getInstruccions (JSONArray arraydata){
        //si lo que obtuvimos no es null
        JSONArray data_ins = null;
        if (arraydata!=null && arraydata.length() > 0){
            try {
                data_ins = arraydata.getJSONArray(4); //leemos el primer segmento en nuestro caso el unico
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return data_ins;
    }

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public JSONObject valsstatus() {
        JSONObject json_data=null; //creamos un objeto JSON

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("id_recepta",g.getRecId()));

        Log.e("doInBackground-g.getRecId()=", "" + g.getRecId());
        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_valoracions);

        datos = new ArrayList<ValoracioEntrada>();
        total_datos = new ArrayList<ValoracioEntrada>();

        //si lo que obtuvimos no es null
        int i=0;
        while (jdata!=null && jdata.length() > 0 && i<jdata.length()){
            try {
                json_data = jdata.getJSONObject(i++); //leemos el primer segmento en nuestro caso el unico
                if (i < 4) datos.add(new ValoracioEntrada(json_data.getString("id_notificacio"), json_data.getLong("nota"), json_data.getString("comentari"),
                        json_data.getString("alias"), json_data.getString("data"), json_data.getString("hora")));
                total_datos.add(new ValoracioEntrada(json_data.getString("id_notificacio"), json_data.getLong("nota"), json_data.getString("comentari"),
                        json_data.getString("alias"), json_data.getString("data"), json_data.getString("hora")));
                Log.e("i = ", (i - 1) + " id_notificacio = " + json_data.getString("id_notificacio"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                json_data=null;
            }
        }
        if (i > 3) mostrar_boto_vals=true;
        return json_data;

    }

    class asyncSelectRecepta extends AsyncTask< String, String, String > {

        String recepta;
        JSONObject data_rec=null,data_num_ing=null,data_num_ins=null;
        JSONArray arraydata,data_ing=null,data_ins=null;
        protected void onPreExecute() {
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Carregant dades....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass
            recepta=params[0];

            //enviamos y recibimos y analizamos los datos en segundo plano.
            Log.e("doInBackground-params0=", "" + params[0]);
            arraydata=receptastatus(recepta);
            data_rec= getRecepta(arraydata);
            Log.e("doInBackground-rec=", "" + data_rec);
            data_num_ing= getNumIng(arraydata);
            Log.e("doInBackground-numing=", "" + data_num_ing);
            data_ing= getIngredients(arraydata);
            Log.e("doInBackground-ing=", "" + data_ing);
            data_num_ins= getNumIns(arraydata);
            Log.e("doInBackground-numins=", "" + data_num_ins);
            data_ins= getInstruccions(arraydata);
            Log.e("doInBackground-ins=", "" + data_ins);
            if (data_rec!=null){
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
                mostrar_dades(data_rec,data_num_ing,data_ing,data_num_ins,data_ins);
            }else{
                error("No existeix connexió amb la base de dades");
            }

        }

    }

    class asyncSelectValoracions extends AsyncTask< String, String, String > {


        JSONObject data=null;
        protected void onPreExecute() {
            pDialog2 = new ProgressDialog(getActivity());
            pDialog2.setMessage("Carregant dades....");
            pDialog2.setIndeterminate(false);
            pDialog2.setCancelable(false);
            pDialog2.show();
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass

            //enviamos y recibimos y analizamos los datos en segundo plano.
            data=valsstatus();
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
            pDialog2.dismiss();//ocultamos progess dialog.
            Log.e("onPostExecute=", "" + result);
            if (result.equals("ok")){
                mostrar_valoracions();
            }else{
                lay_val.setVisibility(View.GONE);
            }

        }

    }



}