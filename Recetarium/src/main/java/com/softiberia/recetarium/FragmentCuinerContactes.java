package com.softiberia.recetarium;

import android.app.AlertDialog;
import android.app.Fragment;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParsePush;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*PANTALLA DE BIENVENIDA*/
public class FragmentCuinerContactes extends Fragment {

    ListView lista;
    TextView nocontac;
    ArrayList<ContacteEntrada>  total_datos;
    Httppostaux post;
    ProgressDialog pDialog;
    String id_receptor;
    Global g;

    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_connect="http://"+IP_Server+"/droidlogin/addcom.php";//ruta en donde estan nuestros archivos

    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cuiner_contactes, container, false);
        super.onCreate(savedInstanceState);

        lista = (ListView) view.findViewById(R.id.ListView_contact);
        nocontac = (TextView) view.findViewById(R.id.nocontactes);
        post=new Httppostaux();
        g = (Global)getActivity().getApplication();

        lista.setAdapter(new LlistatAdaptador(getActivity(),
                R.layout.fragment_receptes_llistat_entrada,
                total_datos){
            @Override
            public void onEntrada(Object entrada, View view) {
                if (entrada != null) {

                    TextView texto_entrada = (TextView) view.findViewById(R.id.textView_titulo);
                    if (texto_entrada != null)
                        texto_entrada.setText(((ContacteEntrada) entrada).get_nom());

                    ImageView imagen_entrada = (ImageView) view.findViewById(R.id.imageView_miniatura);
                    if (imagen_entrada != null)
                        imagen_entrada.setImageBitmap(((ContacteEntrada) entrada).get_idImagen());
                }
            }
        });
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                ContacteEntrada elegido = (ContacteEntrada) pariente.getItemAtPosition(posicion);
                new asyncCrearValoracio().execute(String.valueOf("-1"), "", g.getUserId(), g.getRecId(),
                        elegido.get_id(), elegido.get_telefon(), elegido.get_nom());
                //g.setRecId(elegido.get_id());
            }
        });

        return view;
	 }


    //Omple els camps amb les dades obtingudes de la base de dades
    public void setTotalDatos(ArrayList<ContacteEntrada>  total_datos){
        this.total_datos = total_datos;

    }

    //vibra y muestra un Toast
    public void error(String strerror){
        Vibrator vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);

        Context context = getActivity().getApplicationContext();
        Toast toast1 = Toast.makeText(context,"Error: "+strerror, Toast.LENGTH_SHORT);
        toast1.show();
    }

    public int valoraciostatus( String valoracio, String comentari, String id_autor_comentari,
                                String id_recepta, String id_autor_recepta) {
        int logstatus=-1;

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("valoracio",valoracio));
        postparameters2send.add(new BasicNameValuePair("comentari",comentari));
        postparameters2send.add(new BasicNameValuePair("id_autor_comentari",id_autor_comentari));
        postparameters2send.add(new BasicNameValuePair("id_recepta",id_recepta));
        postparameters2send.add(new BasicNameValuePair("id_autor_recepta",id_autor_recepta));

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_connect);
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

    class asyncCrearValoracio extends AsyncTask< String, String, String> {

        String valoracio, comentari, id_autor_comentari, id_recepta, id_autor_recepta, telefon, nom;
        protected void onPreExecute() {
            //para el progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Enviant recepta....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass
            valoracio=params[0];
            comentari=params[1];
            id_autor_comentari=params[2];
            id_recepta=params[3];
            id_autor_recepta=params[4];
            telefon=params[5];
            nom=params[6];

            String resultat="errInd";
            int status=valoraciostatus(valoracio, comentari, id_autor_comentari, id_recepta, id_autor_recepta);
            //enviamos y recibimos y analizamos los datos en segundo plano.

            resultat=String.valueOf(status);
            if(status==0){
                resultat="ok";
            }else if(status==1){
                resultat="temps";
            }else if(status==3){
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
                Toast toastAct;
                toastAct = Toast.makeText(getActivity(), "Enhorabona, recepta enviada!", 5);
                toastAct.show();
                Log.e("ContacteEnviar =","RecAutorId:" + id_autor_recepta+"; getUserName"+g.getUserName() +"; getRecName"+ g.getRecName());
                sendParse("user_"+id_autor_recepta, g.getUserName()+", t'ha enviat la recepta: "+g.getRecName());
            }else{
                error("No existeix connexió amb la base de dades");
            }
        }

    }

    public void sendParse(String chanel, String message) {
        ParsePush push = new ParsePush();
        push.setChannel(chanel);
        push.setMessage(message);
        push.sendInBackground();

    }

}
