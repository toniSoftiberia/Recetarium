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

public class FragmentCuinerAlta extends Fragment {

    EditText alias,telefon,pass,pass2,email;
    Button bfoto,bcrear;
    ImageView imgcuiner;
    Uri imageUri;
    Httppostaux post;

    String image_str="", image_min="";
    private static int TAKE_PICTURE = 1;
    private static int SELECT_PICTURE = 2;
    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_connect="http://"+IP_Server+"/droidlogin/adduser.php";//ruta en donde estan nuestros archivos
    private ProgressDialog pDialog;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cuiner_alta, container, false);
        super.onCreate(savedInstanceState);

        post=new Httppostaux();
        alias = (EditText)view.findViewById(R.id.alta_alias);
        telefon = (EditText)view.findViewById(R.id.alta_telefon);
        email = (EditText)view.findViewById(R.id.alta_email);
        pass = (EditText)view.findViewById(R.id.alta_password);
        pass2 = (EditText)view.findViewById(R.id.alta_password2);
        bfoto = (Button)view.findViewById(R.id.Bfoto);
        bcrear = (Button)view.findViewById(R.id.BCrear);
        imgcuiner = (ImageView)view.findViewById(R.id.cuiner_img);

        imgcuiner.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                //Obtenemos el layout inflater

                builder.setTitle("Fotografia de perfil");
                builder.setMessage("Selecciona l'origen de la foto");
                builder.setPositiveButton("Càmera", new DialogInterface.OnClickListener() {
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
                String passw=pass.getText().toString();
                String passw2=pass2.getText().toString();

                //verificamos si estan en blanco
                if( checkdata( usuario ,tel ,mail ,passw ,passw2 )){
                    if( passw.equals(passw2)){
                        //si pasamos esa validacion ejecutamos el asynctask
                         new asyncCrear().execute( usuario ,tel ,mail ,passw ,passw2 );
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
    public int createstatus(String username ,String telefon ,String email ,String password) {
        int logstatus=-1;

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        postparameters2send.add(new BasicNameValuePair("usuari",username));
        postparameters2send.add(new BasicNameValuePair("telefon",telefon));
        postparameters2send.add(new BasicNameValuePair("email",email));
        postparameters2send.add(new BasicNameValuePair("pass",password));
        if(image_str != ""){
            postparameters2send.add(new BasicNameValuePair("foto", image_str));
            postparameters2send.add(new BasicNameValuePair("fotomin", image_min));
            Log.e("image_str", "ok");
        }else{
            Log.e("image_min", "null");
        }



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
    public boolean checkdata(String username ,String telefon ,String email ,String password ,String password2 ){

        if 	(username.equals("") || telefon.equals("") || email.equals("") || password.equals("") || password2.equals("")){
            Log.e("Login ui", "Los campos no pueden estar vacios");
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

    class asyncCrear extends AsyncTask< String, String, String> {

        String user, tel ,mail, pass;
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
            tel=params[1];
            mail=params[2];
            pass=params[3];

            String resultat="errInd";
            status=createstatus(user,tel,mail,pass);
            //enviamos y recibimos y analizamos los datos en segundo plano.

            resultat=String.valueOf(status);
            if(status==-5){
                resultat="userdup";
            }else if(status==-1){
                resultat="teldup";
            }else if(status==-2){
                resultat="maildup";
            }else if(status==-3){
                resultat="mailfor";
            }else if(status==-4){
                resultat="telfor";
            }else if(status>=0){
                resultat="ok";
            }else if(status==-6){
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
                Global g = (Global)getActivity().getApplication();
                g.setUserId(String.valueOf(status));
                g.setUserLog(true);
                FragmentCuinerLoged fragment  = new FragmentCuinerLoged();
                Bundle args = new Bundle();
                args.putString("user",user);
                fragment.setArguments(args);
                FragmentCuinerLoged frl = new FragmentCuinerLoged();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, frl)
                        .commit();
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
        /*getActivity().*/startActivityForResult(intent, TAKE_PICTURE);
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

                    imgcuiner.setImageBitmap(bitmap);
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
                imgcuiner.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {}
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