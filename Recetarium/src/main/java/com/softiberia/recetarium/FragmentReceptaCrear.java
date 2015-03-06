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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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

public class FragmentReceptaCrear extends Fragment {

    Spinner spcatego, spcoccio, spdificu, spidioma, sptipusp;
    String stcatego, stcoccio, stdificu, stidioma, sttipusp;
    Button bcrear;
    TextView comensal, cost, titol, tempm, temph, obser;
    ImageView imgrecepta;
    Uri imageUri;
    int num_ing, num_ins;
    Httppostaux post;
    private ProgressDialog pDialog;
    ArrayList<Ingredients> colIng;
    ArrayList<Instruccions> colIns;
    String image_str="", image_min="";
    private static int TAKE_PICTURE = 1;
    private static int SELECT_PICTURE = 2;

    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_connect="http://"+IP_Server+"/droidlogin/addrec.php";//ruta en donde estan nuestros archivos
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receptes_crear, container, false);
        super.onCreate(savedInstanceState);

        post=new Httppostaux();
        spcatego = (Spinner) view.findViewById(R.id.spinner_cat);
        spcoccio = (Spinner) view.findViewById(R.id.spinner_coc);
        spdificu = (Spinner) view.findViewById(R.id.spinner_dif);
        spidioma = (Spinner) view.findViewById(R.id.spinner_idi);
        sptipusp = (Spinner) view.findViewById(R.id.spinner_tip);
        imgrecepta = (ImageView)view.findViewById(R.id.recepta_img);
        titol = (TextView) view.findViewById(R.id.rc_titol);
        comensal = (TextView) view.findViewById(R.id.rc_comen);
        cost = (TextView) view.findViewById(R.id.rc_cost);
        tempm = (TextView) view.findViewById(R.id.rc_temm);
        temph = (TextView) view.findViewById(R.id.rc_temph);
        obser = (TextView) view.findViewById(R.id.cr_obs);
        bcrear = (Button)view.findViewById(R.id.Brcrear);
        colIng = new ArrayList<Ingredients>();
        colIns = new ArrayList<Instruccions>();

        loadSpinnerCategorias();
        loadSpinnerCoocio();
        loadSpinnerDificultat();
        loadSpinnerIdioma();
        loadSpinnerTipusPlat();

        afegirIngredientLay();
        afegirInstruccioLay();

        bcrear.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                String sttitol=titol.getText().toString();
                String stcomensal=comensal.getText().toString();
                String stcost=cost.getText().toString();
                String sttemh=temph.getText().toString();
                String sttemm=tempm.getText().toString();
                String stobse=obser.getText().toString();

                if(stcomensal.equals("")) stcomensal="0";
                if(stcost.equals("")) stcost="0";
                if(sttemh.equals("")) sttemh="0";
                if(sttemm.equals("")) sttemm="0";

                int intcomensal=Integer.valueOf(stcomensal);
                int intcost=Integer.valueOf(stcost);
                int inttemp=((Integer.valueOf(sttemh)*60) + Integer.valueOf(sttemm));

                String stcheck = checkdata( sttitol, intcomensal, intcost, inttemp);
                //verificamos si estan en blanco
                if( stcheck.equals("ok")){
                    //si pasamos esa validacion ejecutamos el asynctask
                    new asyncCrear().execute( sttitol, stcomensal, stcost, String.valueOf(inttemp), stobse);
                }else{
                    //si detecto un error en la primera validacion vibrar y mostrar un Toast con un mensaje de error.
                    error(stcheck);
                }
            }
        });

        imgrecepta.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                //Obtenemos el layout inflater

                builder.setTitle("Fotografia de recepta");
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

        return view;
    }

    public void setNumIng(int num){

        num_ing = num;
    }


    public void setIngredient(int num, String id_ingredient, String quantitat, String escala, boolean modificant){

        if( modificant == false){
            Ingredients ing =new Ingredients(String.valueOf(num),id_ingredient,quantitat,escala);
            colIng.add(ing);
        }else{
            Ingredients miIng = colIng.get(num);
            miIng.setIng(String.valueOf(num),id_ingredient,quantitat,escala);
        }
    }

    public void deleteIngredient(int num){

        Ingredients miIng = colIng.get(num);
        miIng.setNoValid();
    }

    public void setNumIns(int num){

        num_ins = num;
    }

    public void setInstruccio(int num, String descripcio, boolean modificant){

        if( modificant == false){
            Instruccions ins =new Instruccions(String.valueOf(num),descripcio);
            colIns.add(ins);
        }else{
            Instruccions miIns = colIns.get(num);
            miIns.setIns(String.valueOf(num),descripcio);
        }
    }

    public void deleteInstruccio(int num){

        Instruccions miIns = colIns.get(num);
        miIns.setNoValid();
    }

    public void afegirIngredientLay(){

        FragmentReceptaCrearIngredient frc = new FragmentReceptaCrearIngredient();
        Bundle args = new Bundle();
        args.putInt("id_lay",0);
        args.putInt("id_ing_rec",0);
        args.putBoolean("modificant",false);
        args.putBoolean("afegir_dades",false);
        frc.setArguments(args);
        getFragmentManager().beginTransaction()
                .add(R.id.lay_ing, frc,"ing0")
                .commit();
    }

    public void afegirInstruccioLay(){

        FragmentReceptaCrearInstruccio frc = new FragmentReceptaCrearInstruccio();
        Bundle args = new Bundle();
        args.putInt("id_lay",0);
        args.putInt("id_instruccio",0);
        args.putBoolean("modificant",false);
        args.putBoolean("afegir_dades",false);
        frc.setArguments(args);
        getFragmentManager().beginTransaction()
                .add(R.id.lay_ins, frc,"ins0")
                .commit();
    }

    //validamos si no hay ningun campo en blanco
    public String checkdata(String chtitol ,int chcomensal ,int chcost ,int chtemps ){
        if (chtitol.equals("")){
            return "No has introduït el titol";
        }else if (chcomensal <= 0){
            return "No has introduït nombre de comensals";}
        else if (chcost <= 0){
            return "No has introduït el cost aproximat";
        }else if (chtemps <= 0){
            return "No has introduït el temps de cocció";
        }else if (stcatego.equals("Tria una opció")){
            return "No has introduït la categoria";
        }else if (stcoccio.equals("Tria una opció")){
            return "No has introduït el metode de cocció";
        }else if (stdificu.equals("Tria una opció")){
            return "No has introduït la dificultat";
        }else if (stidioma.equals("Tria una opció")){
            return "No has introduït l'idioma";
        }else if (num_ing <= 0){
            return "No has introduït cap ingredient";
        }else if (num_ins <= 0){
            return "No has introduït cap instrucció";
        }else if (sttipusp.equals("Tria una opció")){
            return "No has introduït el tipus de plat";
        }else{
            return "ok";
        }
    }

    //vibra y muestra un Toast
    public void error(String strerror){
        Vibrator vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);

        Context context = getActivity().getApplicationContext();
        Toast toast1 = Toast.makeText(context,"Error: "+strerror, Toast.LENGTH_SHORT);
        toast1.show();
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

            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
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

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public int createstatus(String titol ,String comensal ,String cost ,String temp, String obse) {
        int id_recepta=-1;

    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
        Global g = (Global)getActivity().getApplication();

        postparameters2send.add(new BasicNameValuePair("titol",titol));
        postparameters2send.add(new BasicNameValuePair("comensal",comensal));
        postparameters2send.add(new BasicNameValuePair("cost",cost));
        postparameters2send.add(new BasicNameValuePair("temp",temp));
        postparameters2send.add(new BasicNameValuePair("categoria",stcatego));
        postparameters2send.add(new BasicNameValuePair("coccio",stcoccio));
        postparameters2send.add(new BasicNameValuePair("dificultat",stdificu));
        postparameters2send.add(new BasicNameValuePair("idioma",stidioma));
        postparameters2send.add(new BasicNameValuePair("tipus",sttipusp));
        postparameters2send.add(new BasicNameValuePair("id_alias",g.getUserId()));
        postparameters2send.add(new BasicNameValuePair("observacio",obse));


        if(image_str != ""){
            postparameters2send.add(new BasicNameValuePair("foto", image_str));
            postparameters2send.add(new BasicNameValuePair("fotomin", image_min));
            Log.e("image_str", "ok");
        }else{
            Log.e("image_min", "null");
        }

        int ingValids = 0;
        for (int i =0; i<num_ing; i++){
            Ingredients miIng = colIng.get(i);
            if( miIng.getValid()){
                ingValids++;
                String ning_pos = "ing_pos"+String.valueOf(ingValids);
                Log.e("InsertSendIng",miIng.getNum());
                postparameters2send.add(new BasicNameValuePair(ning_pos,miIng.getNum()));
                String ning_id = "ing_id"+String.valueOf(ingValids);
                Log.e("InsertSendIng",miIng.getIdIng());
                postparameters2send.add(new BasicNameValuePair(ning_id,miIng.getIdIng()));
                String ning_quan = "ing_quan"+String.valueOf(ingValids);
                Log.e("InsertSendIng",miIng.getQuan());
                postparameters2send.add(new BasicNameValuePair(ning_quan,miIng.getQuan()));
                String ning_escala = "ing_escala"+String.valueOf(ingValids);
                Log.e("InsertSendIng",miIng.getEscala());
                postparameters2send.add(new BasicNameValuePair(ning_escala,miIng.getEscala()));
            }
        }
        postparameters2send.add(new BasicNameValuePair("num_ing",String.valueOf(ingValids)));
        int insValids = 0;
        for (int i =0; i<num_ins; i++){
            Instruccions miIns = colIns.get(i);
            if( miIns.getValid()){
                insValids++;
                String nins_id = "ins_id"+String.valueOf(insValids);
                Log.e("InsertSendIns",miIns.getIdIns());
                postparameters2send.add(new BasicNameValuePair(nins_id,miIns.getIdIns()));
                String nins_desc = "ins_desc"+String.valueOf(insValids);
                Log.e("InsertSendIns",miIns.getDesc());
                postparameters2send.add(new BasicNameValuePair(nins_desc,miIns.getDesc()));
            }
        }
        postparameters2send.add(new BasicNameValuePair("num_ins",String.valueOf(insValids)));
        Log.e("InsertSend",titol + ", " +
                cost + ", "+
                comensal + ", "+
                temp + ", "+
                stcatego + ", "+
                stcoccio + ", "+
                stdificu + ", "+
                stidioma + ", "+
                sttipusp + ", "+
                ingValids + ", "+
                obse + ", "+
                g.getUserId() );

        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_connect);
        //si lo que obtuvimos no es null
        if (jdata!=null && jdata.length() > 0){
            id_recepta=-2;
            JSONObject json_data; //creamos un objeto JSON
            try {
                json_data = jdata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
                id_recepta=json_data.getInt("id_recepta");//accedemos al valor
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return id_recepta;
    }

    /*		CLASE ASYNCTASK
 *
 * usaremos esta para poder mostrar el dialogo de progreso mientras enviamos y obtenemos los datos
 * podria hacerse lo mismo sin usar esto pero si el tiempo de respuesta es demasiado lo que podria ocurrir
 * si la conexion es lenta o el servidor tarda en responder la aplicacion sera inestable.
 * ademas observariamos el mensaje de que la app no responde.
 */

    class asyncCrear extends AsyncTask< String, String, String> {

        String sttitol, stcomensal,stcost, sttemp, stobse;
        protected void onPreExecute() {
            //para el progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Creant recepta....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            //obtnemos usr y pass
            sttitol=params[0];
            stcomensal=params[1];
            stcost=params[2];
            sttemp=params[3];
            stobse=params[4];

            String resultat="errInd";
            int id_recepta=createstatus(sttitol,stcomensal,stcost,sttemp, stobse);
            //enviamos y recibimos y analizamos los datos en segundo plano.


            if(id_recepta>=0){
                resultat="ok";
            }else{
                resultat="err";
            }
            return resultat;

        }

        /*Una vez terminado doInBackground segun lo que halla ocurrido
        pasamos a la sig. activity
        o mostramos error*/

        protected void onPostExecute(String resultat) {

            pDialog.dismiss();//ocultamos progess dialog.

            if(resultat.equals("ok")){
                Context context = getActivity().getApplicationContext();
                Toast toast1 = Toast.makeText(context,"Enhorabona, recepta afegida!", Toast.LENGTH_SHORT);
                toast1.show();
                FragmentReceptesLlistat fragment  = new FragmentReceptesLlistat();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
            }else{
                error("No existeix connexió amb la base de dades");
            }
        }

    }

    public class Instruccions {
        private String desc, id_ins;
        boolean valid;

        public Instruccions(String id_ins,String desc){
            this.id_ins=id_ins;
            this.desc=desc;
            this.valid=true;
        }

        public void setIns(String id_ins,String desc){
            this.id_ins=id_ins;
            this.desc=desc;
            this.valid=true;
        }

        public String getIdIns(){
            return this.id_ins;
        }

        public String getDesc(){
            return this.desc;
        }

        public boolean getValid(){
            return this.valid;
        }

        public void setNoValid(){
            this.valid=false;
        }
    }

    public class Ingredients {
        private String num, id_ing, quant, escala;
        boolean valid;

        public Ingredients(String num,String id_ing,String quant,String escala){
            this.num=num;
            this.valid=true;
            this.id_ing=id_ing;
            this.quant=quant;
            this.escala=escala;
        }

        public void setIng(String num,String id_ing,String quant,String escala){
            this.num=num;
            this.valid=true;
            this.id_ing=id_ing;
            this.quant=quant;
            this.escala=escala;
        }

        public String getNum(){
            return this.num;
        }

        public boolean getValid(){
            return this.valid;
        }

        public void setNoValid(){
            this.valid=false;
        }

        public String getIdIng(){
            return this.id_ing;
        }

        public String getQuan(){
            return this.quant;
        }

        public String getEscala(){
            return this.escala;
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

                    imgrecepta.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Failed to load", Toast.LENGTH_SHORT)
                            .show();
                    Log.e("Camera", e.toString());
                }
            }
        } else if (requestCode == SELECT_PICTURE){
            InputStream is;
            Uri selectedImage;
            try {
                selectedImage = data.getData();
                is = getActivity().getContentResolver().openInputStream(selectedImage);
                BufferedInputStream bis = new BufferedInputStream(is);
                bitmap = BitmapFactory.decodeStream(bis);
                imgrecepta.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {}
            catch (java.lang.NullPointerException e) {}
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
            image_str = Base64.encodeToString(byte_arr, Base64.NO_WRAP);
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