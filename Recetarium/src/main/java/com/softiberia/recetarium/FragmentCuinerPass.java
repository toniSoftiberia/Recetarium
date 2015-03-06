package com.softiberia.recetarium;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentCuinerPass extends Fragment {

    EditText oldpass,pass,pass2;
    Button bmpass;
    Httppostaux post;
    Global g;

    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_connect="http://"+IP_Server+"/droidlogin/modpass.php";//ruta en donde estan nuestros archivos
    private ProgressDialog pDialog;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cuiner_pass, container, false);
        super.onCreate(savedInstanceState);

        post=new Httppostaux();
        oldpass = (EditText)view.findViewById(R.id.old_password);
        pass = (EditText)view.findViewById(R.id.new_password);
        pass2 = (EditText)view.findViewById(R.id.new_password2);
        bmpass = (Button)view.findViewById(R.id.BModPass);
        g = (Global)getActivity().getApplication();

        bmpass.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                //Extreamos datos de los EditText
                String oldpassw=oldpass.getText().toString();
                String passw=pass.getText().toString();
                String passw2=pass2.getText().toString();

                //verificamos si estan en blanco
                if( checkdata( oldpassw ,passw ,passw2 )){
                    if( passw.equals(passw2)){
                        //si pasamos esa validacion ejecutamos el asynctask
                         new asyncCrear().execute( oldpassw ,passw ,passw2 );
                    }else{
                        //si detecto un error en la primera validacion vibrar y mostrar un Toast con un mensaje de error.
                        error("Les contrasenyes no coincideixen");
                    }
                }else{
                    //si detecto un error en la primera validacion vibrar y mostrar un Toast con un mensaje de error.
                    error("Els camps no poden estar buits");
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

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public int createstatus(String oldpassword ,String password1 ,String password2) {
        int logstatus=-1;

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("id_alias",g.getUserId()));
        postparameters2send.add(new BasicNameValuePair("oldpass",oldpassword));
        postparameters2send.add(new BasicNameValuePair("newpass",password1));

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

    //validamos si no hay ningun campo en blanco
    public boolean checkdata(String oldpass ,String newpass ,String newpass2){

        if 	(oldpass.equals("") || newpass.equals("") || newpass2.equals("")){
            Log.e("Login ui", "Los campos no pueden estar vacios");
            return false;

        }else{

            return true;
        }


    }
    //Neteja els camps despres de l'actualització
    public void netejarCamps(){
        oldpass.setText("");
        pass.setText("");
        pass2.setText("");
    }

/*		CLASE ASYNCTASK
 *
 * usaremos esta para poder mostrar el dialogo de progreso mientras enviamos y obtenemos los datos
 * podria hacerse lo mismo sin usar esto pero si el tiempo de respuesta es demasiado lo que podria ocurrir
 * si la conexion es lenta o el servidor tarda en responder la aplicacion sera inestable.
 * ademas observariamos el mensaje de que la app no responde.
 */

    class asyncCrear extends AsyncTask< String, String, String> {

        String opass, pass, pass2;
        protected void onPreExecute() {
            //para el progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Actualizant....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass
            opass=params[0];
            pass=params[1];
            pass2=params[2];

            String resultat="errInd";
            int status=createstatus(opass, pass, pass2);
            //enviamos y recibimos y analizamos los datos en segundo plano.

            if(status==0){
                resultat="ok";
            }else if(status==1){
                resultat="errPass";
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

            error(resultat);
            error(URL_connect);
            if(resultat.equals("ok")){
                Context context = getActivity().getApplicationContext();
                Toast toast1 = Toast.makeText(context,"Enhorabona, contrasenya modificada!", Toast.LENGTH_SHORT);
                toast1.show();
                netejarCamps();
            }else if (resultat.equals("errPass")){
                error("La contrasenya és incorrecta");
            }else{
                error("No existeix connexió amb la base de dades");
            }
        }

    }

}