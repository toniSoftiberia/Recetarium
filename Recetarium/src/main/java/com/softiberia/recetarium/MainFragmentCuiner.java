package com.softiberia.recetarium;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainFragmentCuiner extends Fragment {

    EditText user;
    EditText pass;
    CheckBox rPass, aLog;
    Button blogin;
    TextView registrar, fPass;
    Httppostaux post;
    RConfig config;
    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_connect="http://"+IP_Server+"/droidlogin/selectuser.php";//ruta en donde estan nuestros archivos
    String URL_sendmail="http://"+IP_Server+"/droidlogin/sendmail.php";//ruta en donde estan nuestros archivos

    Global g;
    private ProgressDialog pDialog;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cuiner, container, false);
        super.onCreate(savedInstanceState);

        post=new Httppostaux();
        config=new RConfig();
        user = (EditText)view.findViewById(R.id.edusuario);
        pass = (EditText)view.findViewById(R.id.edpassword);
        rPass = (CheckBox)view.findViewById(R.id.checkPass);
        aLog = (CheckBox)view.findViewById(R.id.autoLog);
        blogin = (Button)view.findViewById(R.id.Blogin);
        registrar = (TextView)view.findViewById(R.id.link_to_register);
        fPass = (TextView)view.findViewById(R.id.forgotpassw);

        //((MainActivity)getActivity()).desactivarOpcions();
        g = (Global)getActivity().getApplication();
        g.setMenuMode(0);
        config.loadConfig();

        //Login button action
        blogin.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                //Extreamos datos de los EditText
                String usuario=user.getText().toString();
                String passw=pass.getText().toString();

                //verificamos si estan en blanco
                if( checklogindata( usuario , passw )){

                    //si pasamos esa validacion ejecutamos el asynctask pasando el usuario y clave como parametros

                     new asynclogin().execute(usuario,passw);


                }else{
                    //si detecto un error en la primera validacion vibrar y mostrar un Toast con un mensaje de error.
                    err_login("Els camps no poden estar buits");
                }
            }
        });

        registrar.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                FragmentCuinerAlta fragment  = new FragmentCuinerAlta();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        fPass.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Recuperar la contrasenya");
                alert.setMessage("Introdueix l'e-mail:");

                // Set an EditText view to get user input
                final EditText input = new EditText(getActivity());
                alert.setView(input);
                alert.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        if( checkdata( value )){

                            //si pasamos esa validacion ejecutamos el asynctask pasando el usuario y clave como parametros

                            new asyncSendMail().execute(value);

                        }else{
                            //si detecto un error en la primera validacion vibrar y mostrar un Toast con un mensaje de error.
                            err_login("Els camps no poden estar buits");
                        }
                        return;
                    }
                });

                alert.setNegativeButton("Cancel·lar", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        return;
                    }
                });
                alert.show();
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
 	    toast1.show();
    }

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public int loginstatus(String username ,String password ) {
        int logstatus=-1;

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("usuario",username));
        postparameters2send.add(new BasicNameValuePair("password",password));

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_connect);

        //si lo que obtuvimos no es null
        if (jdata!=null && jdata.length() > 0){

            JSONObject json_data; //creamos un objeto JSON
            try {
                json_data = jdata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
                logstatus=json_data.getInt("id_cuiner");//accedemos al valor
                //Log.e("loginstatus","logstatus= "+logstatus);//muestro por log que obtuvimos
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logstatus = -2;
            }
        }
        return logstatus;

    }

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public int sendmailstatus(String email) {
        int logstatus=-1;

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("email",email));

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_sendmail);

        //si lo que obtuvimos no es null
        if (jdata!=null && jdata.length() > 0){

            JSONObject json_data; //creamos un objeto JSON
            try {
                json_data = jdata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
                logstatus=json_data.getInt("logstatus");//accedemos al valor
                //Log.e("loginstatus","logstatus= "+logstatus);//muestro por log que obtuvimos
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return logstatus;

    }

    //validamos si no hay ningun campo en blanco
    public boolean checklogindata(String username ,String password ){

        if 	(username.equals("") || password.equals("")){
            Log.e("Login ui", "Los campos no pueden estar vacios");
            return false;

        }else{

            return true;
        }
    }

    //validamos si no hay ningun campo en blanco
    public boolean checkdata(String mail){

        if 	(mail.equals("")){
            Log.e("Login ui", "El campo no puede estar vacio");
            return false;

        }else{

            return true;
        }
    }


/*		CLASE ASYNCTASK
 *
 * usaremos esta para poder mostrar el dialogo de progreso mientras enviamos y obtenemos los datos
 * podria hacerse lo mismo sin usar esto pero si el tiempo de respuesta es demasiado lo que podria ocurrir
 * si la conexion es lenta o el servidor tarda en responder la aplicacion sera inestable.
 * ademas observariamos el mensaje de que la app no responde.
 */

    class asyncSendMail extends AsyncTask< String, String, String > {

        String mail;
        protected void onPreExecute() {
            //para el progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Comprovant....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass
            mail=params[0];

            //enviamos y recibimos y analizamos los datos en segundo plano.
            int status=sendmailstatus(mail);
            if (status==0){
                return "ok"; //login valido
            }else{
                if (status==1){
                    return "errBD"; //error conexion
                }else if (status==2){
                    return "mailfor"; //error formato mail
                }else{
                    return "nomail"; //no existe
                }
            }
        }

        /*Una vez terminado doInBackground segun lo que halla ocurrido
        pasamos a la sig. activity
        o mostramos error*/

        protected void onPostExecute(String result) {

            pDialog.dismiss();//ocultamos progess dialog.
            Log.e("onPostExecute=",""+result);
            if (result.equals("ok")){
                Context context = getActivity().getApplicationContext();
                Toast toast1 = Toast.makeText(context,"La contrasenya se ha enviado", Toast.LENGTH_SHORT);
                toast1.show();

            }else if (result.equals("mailfor")){
                err_login("El format de l'e-mail és incorrecte");
            }else if (result.equals("nomail")){
                err_login("L'e-mail no existeix");
            }else{
                err_login("No existeix connexió amb la base de dades");
            }

        }

    }


    /*		CLASE ASYNCTASK
 *
 * usaremos esta para poder mostrar el dialogo de progreso mientras enviamos y obtenemos los datos
 * podria hacerse lo mismo sin usar esto pero si el tiempo de respuesta es demasiado lo que podria ocurrir
 * si la conexion es lenta o el servidor tarda en responder la aplicacion sera inestable.
 * ademas observariamos el mensaje de que la app no responde.
 */

    class asynclogin extends AsyncTask< String, String, String > {

        String user,pass;
        int status;
        protected void onPreExecute() {
            //para el progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Comprovant....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass
            user=params[0];
            pass=params[1];

            //enviamos y recibimos y analizamos los datos en segundo plano.
            status=loginstatus(user,pass);
            if (status>=0){
                return "ok"; //login valido
            }else{
                if (status==-2){
                    return "err"; //login valido
                }else{
                    return "errBD"; //login invalido
                }
            }
        }

        /*Una vez terminado doInBackground segun lo que halla ocurrido
        pasamos a la sig. activity
        o mostramos error*/

        protected void onPostExecute(String result) {

            pDialog.dismiss();//ocultamos progess dialog.
            Log.e("onPostExecute=",""+result);
            if (result.equals("ok")){
                config.saveConfig();
                g.setUserId(String.valueOf(status));
                g.setUserName(user);
                g.setUserLog(true);

                FragmentReceptesNotificacions fragment  = new FragmentReceptesNotificacions();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();

            }else if (result.equals("err")){
                err_login("Nom d'usuari o contrasenya incorectes");
            }else{
                err_login("No existeix connexió amb la base de dades");
            }

        }

    }

  /*		CLASE RConfig
 *
 * usaremos esta para cargar i guardar los datos predefinidos
 */
    protected class RConfig {

        //guardar configuraci�n aplicaci�n Android usando SharedPreferences
        protected void saveConfig()
        {

            SharedPreferences prefs = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user", user.getText().toString());
            editor.putString("pass", pass.getText().toString());
            editor.putBoolean("check",rPass.isChecked());
            editor.putBoolean("autolog",aLog.isChecked());
            editor.commit();
        }




        //cargar configuraci�n aplicaci�n Android usando SharedPreferences
        protected void loadConfig()
        {

            SharedPreferences prefs = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
            rPass.setChecked(prefs.getBoolean("check",false));
            aLog.setChecked(prefs.getBoolean("autolog",false));
            if(rPass.isChecked()){
                String usuari = prefs.getString("user", "");
                String passw = prefs.getString("pass", "");
                user.setText(usuari);
                pass.setText(passw);
                if(aLog.isChecked()){
                    //verificamos si estan en blanco
                    if( checklogindata( usuari , passw )){
                        //si pasamos esa validacion ejecutamos el asynctask pasando el usuario y clave como parametros
                        new asynclogin().execute(usuari,passw);

                    }
                }
            }
        }
    }

}