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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
public class FragmentCuinerLoged extends Fragment {
	String user;
	TextView txt_usr, txt_ali, txt_tel, txt_mail;
    ImageView foto_cuiner;
    Httppostaux post;
    Global g;
    private ProgressDialog pDialog;
    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_connect="http://"+IP_Server+"/droidlogin/selectusermod.php";//ruta en donde estan nuestros archivos
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cuiner_loged, container, false);
        super.onCreate(savedInstanceState);

        post=new Httppostaux();
        txt_usr= (TextView) view.findViewById(R.id.usr_name);
        txt_ali= (TextView) view.findViewById(R.id.cuiner_alias);
        txt_tel= (TextView) view.findViewById(R.id.cuiner_telefon);
        txt_mail= (TextView) view.findViewById(R.id.cuiner_email);
        foto_cuiner= (ImageView) view.findViewById(R.id.cuiner_img);

        g = (Global)getActivity().getApplication();
        g.setMenuMode(1);
        ((MainActivity)getActivity()).activarOpcionsCuinerLog();

        Bundle bundle=getArguments();
        //Obtenemos datos enviados en el intent.
        if (bundle != null) {
           user  = bundle.getString("user");//usuario
        }else{
           user="error";
        }


        txt_usr.setText("Benvingut "+user);//cambiamos texto al nombre del usuario logueado
        new asyncSelectCuiner().execute(g.getUserId());


        view.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    // no hacemos nada.
                    return true;
                }

                return getActivity().onKeyDown(keyCode, event);
            }
        });

           return view;
	 }

    //vibra y muestra un Toast
    public void err_login(String strerror){
        Vibrator vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);

        Context context = getActivity().getApplicationContext();
        Toast toast1 = Toast.makeText(context,"Error: "+strerror, Toast.LENGTH_SHORT);
    }

    //Omple els camps amb les dades obtingudes de la base de dades
    public void mostrar_dades(JSONObject data){
        try {
            txt_ali.setText(data.getString("alias"));
            txt_tel.setText(data.getString("telefon"));
            txt_mail.setText(data.getString("email"));
            txt_usr.setText("Benvingut " + data.getString("alias"));//cambiamos texto al nombre del usuario logueado
            if(data.getString("fotografia")!="null"){
                byte[] decodedString = Base64.decode(data.getString("fotografia"), Base64.NO_WRAP);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                foto_cuiner.setImageBitmap(decodedByte);
            }
            g.setUserName(data.getString("alias"));
            g.setUserId(data.getString("id_cuiner"));
            startParse(data.getString("id_cuiner"));

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public JSONObject userstatus(String username ) {
        JSONObject json_data=null; //creamos un objeto JSON

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("alias",username));

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_connect);

        //si lo que obtuvimos no es null
        if (jdata!=null && jdata.length() > 0){
            try {
                json_data = jdata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return json_data;

    }

    class asyncSelectCuiner extends AsyncTask< String, String, String > {

        String user;
        JSONObject data=null;
        protected void onPreExecute() {
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Carregant dades....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass
            user=params[0];
            Log.e("doInBackground-params0=", "" + params[0]);

            //enviamos y recibimos y analizamos los datos en segundo plano.
            data=userstatus(user);
            Log.e("doInBackground-user=", "" + user);
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
                mostrar_dades(data);
            }else{
                err_login("No existeix connexió amb la base de dades");
            }

        }

    }

}
