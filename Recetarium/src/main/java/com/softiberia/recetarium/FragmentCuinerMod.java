package com.softiberia.recetarium;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

public class FragmentCuinerMod extends Fragment {

    EditText alias,telefon,email;
    ImageView foto_cuiner;
    Button bfoto,bcrear,bbaixa;
    Httppostaux post;
    Global g;
    Uri imageUri;

    String image_str="", image_min="";
    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_connect="http://"+IP_Server+"/droidlogin/selectusermod.php";//ruta en donde estan nuestros archivos
    String URL_modificar="http://"+IP_Server+"/droidlogin/moduser.php";//ruta en donde estan nuestros archivos
    String URL_eliminar="http://"+IP_Server+"/droidlogin/deluser.php";//ruta en donde estan nuestros archivos
    private static int TAKE_PICTURE = 1;
    private static int SELECT_PICTURE = 2;
    private ProgressDialog pDialog,pDialog2;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cuiner_modificar, container, false);
        super.onCreate(savedInstanceState);

        post=new Httppostaux();
        alias = (EditText)view.findViewById(R.id.mod_alias);
        telefon = (EditText)view.findViewById(R.id.mod_telefon);
        email = (EditText)view.findViewById(R.id.mod_email);
        bfoto = (Button)view.findViewById(R.id.Bfoto);
        bcrear = (Button)view.findViewById(R.id.BMod);
        bbaixa = (Button)view.findViewById(R.id.Bbaixa);
        foto_cuiner= (ImageView) view.findViewById(R.id.cuiner_img);
        g = (Global)getActivity().getApplication();

        new asyncSelectCuiner().execute(g.getUserId());


        foto_cuiner.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                //Obtenemos el layout inflater

                builder.setTitle("Fotografia de perfil");
                builder.setMessage("Selecciona l'origen de la foto");
                builder.setPositiveButton("Camara", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Acciones a realizar al pulsar el boton Aceptar
                        takePhoto();
                    }
                });
                builder.setNeutralButton("Galeria", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        takeGalery();
                    }
                });
                builder.setNegativeButton("Cancel·lar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Acciones a realizar al pulsar el boton Cancelar
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        bcrear.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                //Extreamos datos de los EditText
                String usuario=alias.getText().toString();
                String tel=telefon.getText().toString();
                String mail=email.getText().toString();

                //verificamos si estan en blanco
                if( checkdata( usuario ,tel ,mail)){
                    //si pasamos esa validacion ejecutamos el asynctask
                     new asyncMod().execute( usuario ,tel ,mail );
                }else{
                    //si detecto un error en la primera validacion vibrar y mostrar un Toast con un mensaje de error.
                    error("Els camps no poden estar buits");
                }
            }
        });

        bbaixa.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //verificamos la eliminacion
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Estas segur?").setPositiveButton("Si", dialogConf)
                        .setNegativeButton("No", dialogConf).show();
                        //si pasamos esa validacion ejecutamos el asynctask
            }
        });
        return view;
    }


    DialogInterface.OnClickListener dialogConf = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    confirmarEliminarReceptes();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    DialogInterface.OnClickListener dialogConfRec = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    new asyncDel().execute( "1");//eliminar receptes
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    new asyncDel().execute( "0");//no eliminar receptes
                    break;
            }
        }
    };

    //muestra un cuadro de dialogo i devuelve la confirmación
    public void confirmarEliminarReceptes(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Vols eliminar les receptes també?").setPositiveButton("Sí", dialogConfRec)
                .setNegativeButton("No", dialogConfRec).show();

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
    public int updatestatus(String username ,String telefon ,String email ) {
        int logstatus=-1;

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("id_alias",g.getUserId()));
        postparameters2send.add(new BasicNameValuePair("usuari",username));
        postparameters2send.add(new BasicNameValuePair("telefon",telefon));
        postparameters2send.add(new BasicNameValuePair("email",email));
        if(image_str != ""){
            postparameters2send.add(new BasicNameValuePair("foto", image_str));
            postparameters2send.add(new BasicNameValuePair("fotomin", image_min));
            Log.e("image_str", "ok");
        }else{
            Log.e("image_min", "null");
        }

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_modificar);
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

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public int delstatus(String delrec ) {
        int logstatus=-1;

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("id_alias",g.getUserId()));
        postparameters2send.add(new BasicNameValuePair("receptes",delrec));

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

    //validamos si no hay ningun campo en blanco
    public boolean checkdata(String username ,String telefon ,String email ){

        if 	(username.equals("") || telefon.equals("") || email.equals("")){
            Log.e("Login ui", "Los campos no pueden estar vacios");
            return false;

        }else{

            return true;
        }


    }

    //Omple els camps amb les dades obtingudes de la base de dades
    public void mostrar_dades(JSONObject data){
        try {
            alias.setText(data.getString("alias"));
            telefon.setText(data.getString("telefon"));
            email.setText(data.getString("email"));
            if(data.getString("fotografia")!="null"){
                byte[] decodedString = Base64.decode(data.getString("fotografia"), Base64.NO_WRAP);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                foto_cuiner.setImageBitmap(decodedByte);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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


    class asyncDel extends AsyncTask< String, String, String > {

        String delrec;
        JSONObject data=null;
        protected void onPreExecute() {
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass
            delrec=params[0];
            Log.e("doInBackground-params0=", "" + params[0]);

            String resultat="errBD";
            //enviamos y recibimos y analizamos los datos en segundo plano.
            int status=delstatus(delrec);
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
                MainFragmentCuiner frc = new MainFragmentCuiner();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, frc)
                        .commit();
                stopParse(g.getUserId());
                g.setUserId("-1");
                g.setUserLog(false);
                Context context = getActivity().getApplicationContext();
                Toast toast1 = Toast.makeText(context,"Enhorabona, l'usuario s'ha eliminat!", Toast.LENGTH_SHORT);
                toast1.show();

            }else{
                error("No existeix connexió amb la base de dades");
            }

        }

    }
    public void stopParse(String id_cuiner){
        Parse.initialize(getActivity(), "HeStTY8etNSVU6sPZeMXkaajYCHKxlZ2VxBerJh3",
                "mM7nFEBt2ccCxkLMWiQTZEkDKwbNd8C9Nu74hVow");
        PushService.setDefaultPushCallback(getActivity(), MainActivity.class);
        PushService.getSubscriptions(getActivity());
        Set<String> set = PushService.getSubscriptions(getActivity());
        Log.e("PushsetSt", "set:" + set );
        for (String channelName : set) {
                Log.e("Pushset", "Push:" + channelName );
                PushService.unsubscribe(getActivity(), channelName);
        }
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseAnalytics.trackAppOpened(getActivity().getIntent());
    }

    class asyncSelectCuiner extends AsyncTask< String, String, String > {

        String user;
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
            user=params[0];
            Log.e("doInBackground-params0=", "" + params[0]);

            //enviamos y recibimos y analizamos los datos en segundo plano.
            data=userstatus(user);
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
                mostrar_dades(data);
            }else{
                error("No existeix connexió amb la base de dades");
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

    class asyncMod extends AsyncTask< String, String, String> {

        String user, tel ,mail;
        protected void onPreExecute() {
            //para el progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Actualizant dades....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass
            user=params[0];
            tel=params[1];
            mail=params[2];

            String resultat="errInd";
            int status=updatestatus(user, tel, mail);
            //enviamos y recibimos y analizamos los datos en segundo plano.

            resultat=String.valueOf(status);
            if(status==0){
                resultat="userdup";
            }else if(status==1){
                resultat="teldup";
            }else if(status==2){
                resultat="maildup";
            }else if(status==3){
                resultat="mailfor";
            }else if(status==4){
                resultat="telfor";
            }else if(status==5){
                resultat="ok";
            }else if(status==6){
                resultat="errBD";
            }
            return resultat;

        }

        /*Una vez terminado doInBackground segun lo que halla ocurrido
        pasamos a la sig. activity
        o mostramos error*/

        protected void onPostExecute(String resultat) {

            pDialog.dismiss();//ocultamos progess dialog.

            if(resultat.equals("userdup")){
                error("L'àlias ja existeix!");
            }else if(resultat.equals("teldup")){
                error("El telèfon ja existeix!");
            }else if(resultat.equals("maildup")){
                error("L'e-mail ja existeix!");
            }else if(resultat.equals("mailfor")){
                error("El format de l'e-mail és incorrecte!");
            }else if(resultat.equals("telfor")){
                error("El format del telefon és incorrecte!");
            }else if(resultat.equals("ok")){
                Context context = getActivity().getApplicationContext();
                Toast toast1 = Toast.makeText(context,"Enhorabona, dades actualizades!", Toast.LENGTH_SHORT);
                toast1.show();
            }else{
                error("No existeix connexió amb la base de dades");
            }
        }

    }

    public void takePhoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStorageDirectory(),  "Recetarium.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    public void takeGalery() {

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        File photo = new File(Environment.getExternalStorageDirectory(),  "Recetarium.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("requestCode", "= "+requestCode);

        Bitmap bitmap = null, bitmapmin = null;

        if (requestCode == TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = imageUri;
                getActivity().getContentResolver().notifyChange(selectedImage, null);
                ContentResolver cr = getActivity().getContentResolver();
                try {
                    bitmap = android.provider.MediaStore.Images.Media
                            .getBitmap(cr, selectedImage);

                    foto_cuiner.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Failed to load", Toast.LENGTH_SHORT)
                            .show();
                    Log.e("Camera", e.toString());
                }
            }
        } else if (requestCode == SELECT_PICTURE){
            Uri selectedImage = data.getData();
            InputStream is;
            try {
                is = getActivity().getContentResolver().openInputStream(selectedImage);
                BufferedInputStream bis = new BufferedInputStream(is);
                bitmap = BitmapFactory.decodeStream(bis);
                foto_cuiner.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {}
            catch (java.lang.OutOfMemoryError e) {
                error("Memòria insuficient");
                Log.e("OutOfMemoryError",  e.toString());

            }
        }
        if(bitmap != null){
            bitmapmin = redimensionarImagenMaximo(bitmap, 80);
            bitmap = redimensionarImagenMaximo(bitmap, 400);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            byte[] byte_arr = stream.toByteArray();
            image_str = Base64.encodeToString(byte_arr,Base64.NO_WRAP);
            Log.e("image_str", image_str);

            ByteArrayOutputStream streammin = new ByteArrayOutputStream();
            bitmapmin.compress(Bitmap.CompressFormat.JPEG, 50, streammin);
            byte[] byte_arr_min = streammin.toByteArray();
            image_min = Base64.encodeToString(byte_arr_min,Base64.NO_WRAP);
            Log.e("image_min", image_min);

        }
    }
    public Bitmap redimensionarImagenMaximo(Bitmap mBitmap, float newSize){
        //Redimensionamos
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        float sclasePerCent = ((float) newSize) / width;
        float scaleWidth = sclasePerCent;
        float scaleHeight = sclasePerCent;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        return Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, false);
    }

}