package com.softiberia.recetarium;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.PushService;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

/*PANTALLA DE BIENVENIDA*/
public class FragmentReceptesNotificacions extends Fragment {

	Button belinot;
    TextView titol,no_noti,no_rebu;
    Httppostaux post;
    ListView lista,rebudes;
    MenuItem b_not;
    Global g;
    ArrayList<NotificacioEntrada> datos, datos_rebu;
    int cont_not, cont_rebu, noti_pendent;
    private ProgressDialog pDialog;


    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_actnot="http://"+IP_Server+"/droidlogin/actnot.php";//ruta en donde estan nuestros archivos
    String URL_connect="http://"+IP_Server+"/droidlogin/selectnotificacionscuiner.php";//ruta en donde estan nuestros archivos
    String URL_eliminar="http://"+IP_Server+"/droidlogin/delnot.php";//ruta en donde estan nuestros archivos
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_receptes_notificacions, container, false);
	        super.onCreate(savedInstanceState);

            post=new Httppostaux();
            noti_pendent = 0;
            titol = (TextView) view.findViewById(R.id.titol);
            belinot = (Button) view.findViewById(R.id.Belinot);
            lista = (ListView) view.findViewById(R.id.ListView_listado);
            rebudes = (ListView) view.findViewById(R.id.ListView_rebudes);
            no_noti = (TextView) view.findViewById(R.id.nonotificacions);
            no_rebu = (TextView) view.findViewById(R.id.norebudes);
            g = (Global)getActivity().getApplication();
            datos = new ArrayList<NotificacioEntrada>();
            datos_rebu = new ArrayList<NotificacioEntrada>();
            cont_not = 0;
            cont_rebu = 0;
            no_noti.setVisibility(View.VISIBLE);
            no_rebu.setVisibility(View.VISIBLE);

            g.setMenuMode(1);
            if(g.getUserId()=="-1"){
                MainFragmentCuiner frc = new MainFragmentCuiner();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, frc)
                        .addToBackStack(null)
                        .commit();
            }else{
                startParse(g.getUserId());
            }

            new asyncSelectNotificacions().execute(g.getUserId());

            belinot.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Estas segur?").setPositiveButton("Sí", dialogConf)
                            .setNegativeButton("No", dialogConf).show();
                }
            });


           return view;
	 }


    public void startParse(String id_cuiner){
        String newChanel = "user_"+id_cuiner;
        Parse.initialize(getActivity(), "HeStTY8etNSVU6sPZeMXkaajYCHKxlZ2VxBerJh3",
                "mM7nFEBt2ccCxkLMWiQTZEkDKwbNd8C9Nu74hVow");
        PushService.setDefaultPushCallback(getActivity(), MainActivity.class);
        PushService.subscribe(getActivity(), newChanel, MainActivity.class);
        PushService.getSubscriptions(getActivity());
        Set<String> set = PushService.getSubscriptions(getActivity());
        Log.e("PushsetSt", "set:" + set );
        for (String channelName : set) {
            if (!channelName.equals(newChanel)){
                Log.e("Pushset", "channelName:" + channelName );
                Log.e("Pushset", "newChanel:" + newChanel );
                PushService.unsubscribe(getActivity(), channelName);
            }
        }
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseAnalytics.trackAppOpened(getActivity().getIntent());
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
                R.layout.fragment_receptes_visualitzar_valoracio,
                datos){
            @Override
            public void onEntrada(Object entrada, View view) {
                if (entrada != null) {

                    TextView tv_autor = (TextView) view.findViewById(R.id.tv_autor);
                    if (tv_autor != null)
                        tv_autor.setText(((NotificacioEntrada) entrada).get_recepta());

                    TextView tv_comentari = (TextView) view.findViewById(R.id.tv_comentari);
                    if (tv_comentari != null)
                        tv_comentari.setText(((NotificacioEntrada) entrada).get_hora()+" "+
                                ((NotificacioEntrada) entrada).get_data()+": "+
                                ((NotificacioEntrada) entrada).get_emisor()+" ha comentat: "+
                                ((NotificacioEntrada) entrada).get_comentari());

                    RatingBar rb_valoracio = (RatingBar) view.findViewById(R.id.rb_valoracio);
                    if (rb_valoracio != null)
                        rb_valoracio.setRating((float) ((NotificacioEntrada) entrada).get_nota());
                }
            }
        });
        if(cont_not>0)no_noti.setVisibility(View.GONE);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                NotificacioEntrada elegido = (NotificacioEntrada) pariente.getItemAtPosition(posicion);
                g.setRecId(elegido.get_id_recepta());
                g.setRecAutorId(elegido.get_autor_recepta());
                FragmentReceptaVisualitzar fragmentV  = new FragmentReceptaVisualitzar();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentV,"FragmentReceptaVisualitzar")
                        .addToBackStack(null)
                        .commit();
            }
        });

        rebudes.setAdapter(new LlistatAdaptador(getActivity(),
                R.layout.fragment_receptes_visualitzar_valoracio,
                datos_rebu){
            @Override
            public void onEntrada(Object entrada, View view) {
                if (entrada != null) {

                    TextView tv_autor = (TextView) view.findViewById(R.id.tv_autor);
                    if (tv_autor != null)
                        tv_autor.setText(((NotificacioEntrada) entrada).get_emisor());

                    TextView tv_comentari = (TextView) view.findViewById(R.id.tv_comentari);
                    if (tv_comentari != null)
                        tv_comentari.setText(((NotificacioEntrada) entrada).get_hora()+" "+
                                ((NotificacioEntrada) entrada).get_data()+": Vol que miris la recepta: "+
                                ((NotificacioEntrada) entrada).get_recepta());

                    LinearLayout status = (LinearLayout) view.findViewById(R.id.l_status);
                    if (status != null) {
                        status.setVisibility(View.VISIBLE);
                        if( ((NotificacioEntrada) entrada).get_vist()==1){
                            status.setBackgroundColor(0xCC99CC00);//verde
                        }else{
                            status.setBackgroundColor(0xFFF8B05E);//naranja
                        }
                    }

                    RatingBar rb_valoracio = (RatingBar) view.findViewById(R.id.rb_valoracio);
                    if (rb_valoracio != null)
                        rb_valoracio.setVisibility(View.GONE);

                }
            }
        });
        if(cont_rebu>0)no_rebu.setVisibility(View.VISIBLE);

        rebudes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                NotificacioEntrada elegido = (NotificacioEntrada) pariente.getItemAtPosition(posicion);
                g.setRecId(elegido.get_id_recepta());
                g.setRecAutorId(elegido.get_autor_recepta());
                    FragmentReceptaVisualitzar fragmentV  = new FragmentReceptaVisualitzar();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, fragmentV,"FragmentReceptaVisualitzar")
                            .addToBackStack(null)
                            .commit();
                if(elegido.get_vist()==0){
                    elegido.set_vist(1);
                    LinearLayout status = (LinearLayout) view.findViewById(R.id.l_status);
                    status.setBackgroundColor(0xCC99CC00);//verde
                    ((MainActivity)getActivity()).setNumNoti(--noti_pendent);
                    new asyncMarcar().execute(elegido.get_id());
                }
            }
        });

        ((MainActivity)getActivity()).setNumNoti(noti_pendent);
        ((MainActivity)getActivity()).desactivarOpcions();
        ((MainActivity)getActivity()).activarOpcionsCuinerLog();

    }


    DialogInterface.OnClickListener dialogConf = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    new asyncDelNot().execute();//eliminar receptes
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

        postparameters2send.add(new BasicNameValuePair("id_cuiner",username));

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_connect);


        //si lo que obtuvimos no es null
        int i=0;
        while (jdata!=null && jdata.length() > 0 && i<jdata.length()){
            try {
                json_data = jdata.getJSONObject(i++); //leemos el primer segmento en nuestro caso el unico
                if(json_data.getLong("nota")==-1){
                    datos_rebu.add(new NotificacioEntrada(json_data.getString("id_notificacio"), json_data.getLong("nota"), json_data.getString("comentari"),
                        json_data.getString("alias"), json_data.getString("data"), json_data.getString("hora"), json_data.getString("id_recepta"),
                            json_data.getString("titol"), json_data.getString("id_cuiner"), json_data.getInt("vist")));
                        cont_rebu++;
                    if (json_data.getInt("vist")==0)
                        noti_pendent++;
                }else{
                    datos.add(new NotificacioEntrada(json_data.getString("id_notificacio"), json_data.getLong("nota"), json_data.getString("comentari"),
                        json_data.getString("alias"), json_data.getString("data"), json_data.getString("hora"), json_data.getString("id_recepta"),
                        json_data.getString("titol"), json_data.getString("id_cuiner"), json_data.getInt("vist")));
                        cont_not++;
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                json_data=null;
            }
        }

        return json_data;

    }

    class asyncSelectNotificacions extends AsyncTask< String, String, String > {

        String user;
        JSONObject data=null;
        protected void onPreExecute() {//para el progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Buscant notificacions....");
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
                no_noti.setVisibility(View.VISIBLE);
                no_rebu.setVisibility(View.VISIBLE);
            }

        }

    }

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public int delnotstatus() {
        int logstatus=-1;

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("id_cuiner",g.getUserId()));

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

    class asyncDelNot extends AsyncTask< String, String, String > {

        JSONObject data=null;
        protected void onPreExecute() {
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass

            String resultat="errBD";
            //enviamos y recibimos y analizamos los datos en segundo plano.
            int status=delnotstatus();
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
                FragmentReceptesNotificacions frl = new FragmentReceptesNotificacions();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, frl)
                        .commit();
                Context context = getActivity().getApplicationContext();
                Toast toast1 = Toast.makeText(context,"Enhorabona, notificacions eliminades!", Toast.LENGTH_SHORT);
                toast1.show();
            }else{
                err_login("No existeix connexió amb la base de dades");
            }

        }

    }

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public int marcarstatus(String id_not) {
        int logstatus=-1;

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("id_not",id_not));

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_actnot);
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

    class asyncMarcar extends AsyncTask< String, String, String > {

        JSONObject data=null;
        String not;
        protected void onPreExecute() {
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass
            not=params[0];

            String resultat="errBD";
            //enviamos y recibimos y analizamos los datos en segundo plano.
            int status=marcarstatus(not);
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

            }else{
                err_login("No existeix connexió amb la base de dades");
            }

        }

    }


}
