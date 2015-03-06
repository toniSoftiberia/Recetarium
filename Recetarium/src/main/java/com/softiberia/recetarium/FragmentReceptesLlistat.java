package com.softiberia.recetarium;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*PANTALLA DE BIENVENIDA*/
public class FragmentReceptesLlistat extends Fragment {

	Button bcrear;
    TextView titol,no_receptes;
    Httppostaux post;
    ListView lista;
    Global g;
    ArrayList<LlistatEntrada> datos;
    private ProgressDialog pDialog;


    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_connect="http://"+IP_Server+"/droidlogin/selectreceptescuiner.php";//ruta en donde estan nuestros archivos
    String URL_eliminar="http://"+IP_Server+"/droidlogin/delrec.php";//ruta en donde estan nuestros archivos
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_receptes_llistat, container, false);
	        super.onCreate(savedInstanceState);

            post=new Httppostaux();
            titol = (TextView) view.findViewById(R.id.titol);
            bcrear = (Button) view.findViewById(R.id.Bnovarec);
            lista = (ListView) view.findViewById(R.id.ListView_listado);
            no_receptes = (TextView) view.findViewById(R.id.noreceptes);
            g = (Global)getActivity().getApplication();
            datos = new ArrayList<LlistatEntrada>();
            g.setMenuMode(0);
            if(g.getUserLog()){
                ((MainActivity)getActivity()).desactivarOpcions();
                ((MainActivity)getActivity()).activarOpcionsReceptesLog();
                g.setMenuMode(2);
            }
            titol.setText("Llistat de receptes");//cambiamos texto de titulo

            new asyncSelectReceptes().execute(g.getUserId());

           return view;
	 }

    //vibra y muestra un Toast
    public void err_login(String strerror){
        Vibrator vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);

        Context context = getActivity().getApplicationContext();
        Toast toast1 = Toast.makeText(context,"Error: "+strerror, Toast.LENGTH_SHORT);
        toast1.show();
    }

    //validamos si no hay ningun campo en blanco
    public void mostrar_dades(){

        lista.setAdapter(new LlistatAdaptador(getActivity(),
                R.layout.fragment_receptes_llistat_entrada,
                datos) {
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
                g.setRecAutorId(g.getUserId());
                g.setModeProp(true);
                ((MainActivity) getActivity()).desactivarOpcions();
                FragmentReceptaVisualitzar fragmentV = new FragmentReceptaVisualitzar();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentV, "FragmentReceptaVisualitzar")
                        .addToBackStack(null)
                        .commit();
            }
        });

        lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> pariente, View view,
                                           int posicion, long id) {
                LlistatEntrada elegido = (LlistatEntrada) pariente.getItemAtPosition(posicion);
                g.setRecId(elegido.get_id());
                g.setRecName(elegido.get_texto());
                g.setRecAutorId(g.getUserId());
                ((MainActivity) getActivity()).desactivarOpcions();
                FragmentReceptaModificar fragmentM  = new FragmentReceptaModificar();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentM,"FragmentReceptaModificar")
                        .addToBackStack(null)
                        .commit();
                return false;
            }
        });
        //Deslizar item para borrarlo
        SwipeListViewTouchListener touchListener =new SwipeListViewTouchListener(lista,new SwipeListViewTouchListener.OnSwipeCallback() {
            @Override
            public void onSwipeLeft(ListView listView, int [] reverseSortedPositions) {
                //Aqui ponemos lo que hara el programa cuando deslizamos un item ha la izquierda

                LlistatEntrada elegido = (LlistatEntrada) listView.getItemAtPosition(reverseSortedPositions[0]);
                g.setRecId(elegido.get_id());
                g.setRecName(elegido.get_texto());
                g.setRecAutorId(g.getUserId());
                Log.e("swipe "," "+reverseSortedPositions[0]);
                ((MainActivity) getActivity()).desactivarOpcions();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Estas segur?").setPositiveButton("Sí", dialogConf)
                        .setNegativeButton("No", dialogConf).show();
            }

            @Override
            public void onSwipeRight(ListView listView, int [] reverseSortedPositions) {
                //Aqui ponemos lo que hara el programa cuando deslizamos un item ha la derecha

                LlistatEntrada elegido = (LlistatEntrada) listView.getItemAtPosition(reverseSortedPositions[0]);
                g.setRecId(elegido.get_id());
                g.setRecName(elegido.get_texto());
                g.setRecAutorId(g.getUserId());
                Log.e("swipe "," "+reverseSortedPositions[0]);
                ((MainActivity) getActivity()).desactivarOpcions();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Eliminar recepta?").setPositiveButton("Sí", dialogConf)
                        .setNegativeButton("No", dialogConf).show();
            }
        },true, false);

        //Escuchadores del listView
        lista.setOnTouchListener(touchListener);
        lista.setOnScrollListener(touchListener.makeScrollListener());
    }

    DialogInterface.OnClickListener dialogConf = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    new asyncDelRec().execute();//eliminar receptes
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public JSONObject recstatus(String username ) {
        JSONObject json_data=null; //creamos un objeto JSON

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("alias",username));

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_connect);


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
                datos.add(new LlistatEntrada(foto, json_data.getString("titol"), json_data.getString("id_recepta")));
                Log.e("i = ", (i - 1) + " titol = " + json_data.getString("titol"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                json_data=null;
            }
        }

        return json_data;

    }

    class asyncSelectReceptes extends AsyncTask< String, String, String > {

        String user;
        JSONObject data=null;
        protected void onPreExecute() {//para el progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Buscant receptes....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass
            user=params[0];
            Log.e("doInBackground-params0=", "" + params[0]);

            //enviamos y recibimos y analizamos los datos en segundo plano.
            data=recstatus(user);
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
                no_receptes.setVisibility(View.VISIBLE);
            }

        }

    }

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public int delstatus() {
        int logstatus=-1;

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("id_cuiner",g.getUserId()));
        postparameters2send.add(new BasicNameValuePair("id_recepta",g.getRecId()));

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_eliminar);
        //si lo que obtuvimos no es null
        if (jdata!=null && jdata.length() > 0){
            logstatus=-2;
            JSONObject json_data; //creamos un objeto JSON
            try {
                json_data = jdata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
                logstatus=json_data.getInt("logstatus");//accedemos al valor
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return logstatus;
    }

    class asyncDelRec extends AsyncTask< String, String, String > {

        JSONObject data=null;
        protected void onPreExecute() {
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass

            String resultat="errBD";
            //enviamos y recibimos y analizamos los datos en segundo plano.
            int status=delstatus();
            Log.e("doInBackground-status=", "" + data);
            resultat=String.valueOf(status);
            if(status==0){
                resultat="ok";
            }
            return resultat;
        }

        /*Una vez terminado doInBackground segun lo que halla ocurrido
        pasamos a la sig. activity
        o mostramos error*/

        protected void onPostExecute(String result) {
            Log.e("onPostExecute=", "" + result);
            if (result.equals("ok")){
                FragmentReceptesLlistat frl = new FragmentReceptesLlistat();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, frl)
                        .commit();
                Context context = getActivity().getApplicationContext();
                Toast toast1 = Toast.makeText(context,"Enhorabona, la recepta a s'ha eliminat!", Toast.LENGTH_SHORT);
                toast1.show();
            }else{
                err_login("No existeix connexió amb la base de dades");
            }

        }

    }


}
