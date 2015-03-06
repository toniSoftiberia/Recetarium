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

public class FragmentReceptaModificar extends Fragment {

    Spinner spcatego, spcoccio, spdificu, spidioma, sptipusp;
    String stcatego, stcoccio, stdificu, stidioma, sttipusp;
    Button bcrear;
    ImageView foto_recepta;
    TextView comensal, cost, titol, tempm, temph, obser;
    int num_ing, num_ins;
    Uri imageUri;
    String image_str="", image_min="";
    ArrayList<Ingredients> colIng;
    ArrayList<Instruccions> colIns;
    Httppostaux post;
    private ProgressDialog pDialog;
    Global g;

    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_select="http://"+IP_Server+"/droidlogin/selectrecepta.php";//ruta en donde estan nuestros archivos
    String URL_connect="http://"+IP_Server+"/droidlogin/modrec.php";//ruta en donde estan nuestros archivos
    private static int TAKE_PICTURE = 1;
    private static int SELECT_PICTURE = 2;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receptes_modificar, container, false);
        super.onCreate(savedInstanceState);

        post=new Httppostaux();
        spcatego = (Spinner) view.findViewById(R.id.spinner_cat);
        spcoccio = (Spinner) view.findViewById(R.id.spinner_coc);
        spdificu = (Spinner) view.findViewById(R.id.spinner_dif);
        spidioma = (Spinner) view.findViewById(R.id.spinner_idi);
        sptipusp = (Spinner) view.findViewById(R.id.spinner_tip);
        foto_recepta= (ImageView) view.findViewById(R.id.recepta_img);
        titol = (TextView) view.findViewById(R.id.rc_titol);
        comensal = (TextView) view.findViewById(R.id.rc_comen);
        cost = (TextView) view.findViewById(R.id.rc_cost);
        tempm = (TextView) view.findViewById(R.id.rc_temm);
        temph = (TextView) view.findViewById(R.id.rc_temph);
        obser = (TextView) view.findViewById(R.id.rc_obs);
        bcrear = (Button)view.findViewById(R.id.Brcrear);
        colIng = new ArrayList<Ingredients>();
        colIns = new ArrayList<Instruccions>();
        g = (Global)getActivity().getApplication();

        new asyncSelectRecepta().execute(g.getRecId());

        loadSpinnerCategorias();
        loadSpinnerCoocio();
        loadSpinnerDificultat();
        loadSpinnerIdioma();
        loadSpinnerTipusPlat();



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
                    new asyncMod().execute( sttitol, stcomensal, stcost, String.valueOf(inttemp), stobse);
                }else{
                    //si detecto un error en la primera validacion vibrar y mostrar un Toast con un mensaje de error.
                    error(stcheck);
                }
            }
        });

        foto_recepta.setOnClickListener(new View.OnClickListener(){
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


    public void setIngredient(int index, int id_ing_rec, String id_ingredient, String quantitat, String escala, boolean modificant, boolean inNew){

        if( modificant == false){
            Ingredients ing =new Ingredients(String.valueOf(id_ing_rec),id_ingredient,quantitat,escala,true,true);
            colIng.add(ing);
        }else{
            Ingredients miIng = colIng.get(index);
            miIng.setIng(String.valueOf(id_ing_rec),id_ingredient,quantitat,escala,true,inNew);
        }
    }

    public void deleteIngredient(int num){

        Ingredients miIng = colIng.get(num);
        miIng.setNoValid();
    }

    public void setNumIns(int num){

        num_ins = num;
    }

    public void setInstruccio(int index, int id_instruccio, String descripcio, boolean modificant, boolean inNew){

        if( modificant == false){
            Instruccions ins =new Instruccions(String.valueOf(id_instruccio),descripcio,true,true);
            colIns.add(ins);
        }else{
            Instruccions miIns = colIns.get(index);
            miIns.setIns(String.valueOf(id_instruccio),descripcio,true,inNew);
        }
    }

    public void deleteInstruccio(int num){

        Instruccions miIns = colIns.get(num);
        miIns.setNoValid();
    }

    public void afegirIngredientLay(int ing_num, int max_id){

        FragmentReceptaCrearIngredient frc = new FragmentReceptaCrearIngredient();
        Bundle args = new Bundle();
        args.putInt("id_lay",ing_num);
        args.putInt("id_ing_rec",max_id);
        args.putBoolean("modificant",true);
        args.putBoolean("afegir_dades",false);
        frc.setArguments(args);
        getFragmentManager().beginTransaction()
                .add(R.id.lay_ing, frc,"ing"+ing_num)
                .commit();
    }

    public void afegirInstruccioLay(int ins_num_lay, int max_id){

        FragmentReceptaCrearInstruccio frc = new FragmentReceptaCrearInstruccio();
        Bundle args = new Bundle();
        args.putInt("id_lay",ins_num_lay);
        args.putInt("id_instruccio",max_id);
        args.putBoolean("modificant",true);
        args.putBoolean("afegir_dades",false);
        frc.setArguments(args);
        getFragmentManager().beginTransaction()
                .add(R.id.lay_ins, frc,"ins"+ins_num_lay)
                .commit();
    }

    //validamos si no hay ningun campo en blanco
    public String checkdata(String chtitol ,int chcomensal ,int chcost ,int chtemps ){
        if (chtitol.equals("")){
            return "No has introduït el títol";
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

    // Ens diu l'item que hem de seleccionar
    public int getIndexArray(String[] arr, String cad) {
        //Aqui pueden usar el método de búsqueda que necesiten.
        int index=-1;
        for(int i=0;i<arr.length;i++)
        {
            if(arr[i] == null ? cad == null : arr[i].equals(cad)){
                index=i;
                break;
            }
        }
        return index;
    }

    //Omple els camps amb les dades obtingudes de la base de dades
    public void mostrar_dades(JSONObject data_rec,JSONObject data_num_ing,JSONArray data_ing,JSONObject data_num_ins,JSONArray data_ins){
        try {
            titol.setText(data_rec.getString("titol"));
            comensal.setText(data_rec.getString("comensals"));
            cost.setText(data_rec.getString("cost"));
            int temp=data_rec.getInt("temps");
            temph.setText(String.valueOf(temp/60));
            tempm.setText(String.valueOf(temp % 60));
            obser.setText(data_rec.getString("observacio"));

            spcatego.setSelection(getIndexArray(getResources().getStringArray(R.array.categoria), data_rec.getString("categoria")));
            spcoccio.setSelection(getIndexArray(getResources().getStringArray(R.array.metode_coccio), data_rec.getString("metode_coccio")));
            spdificu.setSelection(getIndexArray(getResources().getStringArray(R.array.dificultat), data_rec.getString("dificultat")));
            spidioma.setSelection(getIndexArray(getResources().getStringArray(R.array.idioma_recepta), data_rec.getString("idioma")));
            sptipusp.setSelection(getIndexArray(getResources().getStringArray(R.array.tipus_plat), data_rec.getString("tipus_plat")));

            if(data_rec.getString("fotografia")!="null"){
                byte[] decodedString = Base64.decode(data_rec.getString("fotografia"), Base64.NO_WRAP);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                foto_recepta.setImageBitmap(decodedByte);
            }

            num_ing=data_num_ing.getInt("num_ing");
            int max_ing=0;
            int i;
            for (i =0; i<num_ing;){
                JSONObject json_ing = data_ing.getJSONObject(i);
                FragmentReceptaCrearIngredient frc = new FragmentReceptaCrearIngredient();
                max_ing = Integer.valueOf(json_ing.getString("id_ing_rec"))+1;
                Bundle args = new Bundle();
                args.putInt("id_lay", i);
                args.putInt("id_ing_rec", (json_ing.getInt("id_ing_rec")));
                args.putBoolean("modificant",true);
                args.putBoolean("afegir_dades", true);
                args.putString("id_ingredient",(json_ing.getString("id_ingredient")));
                args.putString("quantitat",(json_ing.getString("quantitat")));
                args.putString("nom",(json_ing.getString("nom")));
                args.putString("escala",(json_ing.getString("escala")));
                frc.setArguments(args);
                getFragmentManager().beginTransaction()
                        .add(R.id.lay_ing, frc,"ing"+String.valueOf(i))
                        .commit();
                Ingredients ins =new Ingredients(json_ing.getString("id_ing_rec"),json_ing.getString("id_ingredient"),json_ing.getString("quantitat"),json_ing.getString("escala"),false,false);
                colIng.add(ins);
                i++;
            }
            afegirIngredientLay(i, max_ing);

            num_ins=data_num_ins.getInt("num_ins");
            int max_ins=0;
            for (i =0; i<num_ins; ){
                Log.e("i",String.valueOf(i));
                Log.e("num_ins",String.valueOf(num_ins));
                JSONObject json_ins = data_ins.getJSONObject(i);
                FragmentReceptaCrearInstruccio frc = new FragmentReceptaCrearInstruccio();
                max_ins = Integer.valueOf(json_ins.getString("id_instruccio"))+1;
                Bundle args = new Bundle();
                args.putInt("id_lay", i);
                args.putInt("id_instruccio", Integer.valueOf(json_ins.getString("id_instruccio")));
                args.putBoolean("modificant", true);
                args.putBoolean("afegir_dades",true);
                args.putString("text",(json_ins.getString("text")));
                frc.setArguments(args);
                getFragmentManager().beginTransaction()
                        .add(R.id.lay_ins, frc,"ins"+String.valueOf(i))
                        .commit();
                Instruccions ins =new Instruccions(json_ins.getString("id_instruccio"),json_ins.getString("text"),false,false);
                colIns.add(ins);
                i++;
            }
            afegirInstruccioLay(i,max_ins);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public int modstatus(String titol ,String comensal ,String cost ,String temp, String obse) {
        int logstatus=-1;

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
        postparameters2send.add(new BasicNameValuePair("id_cuiner",g.getUserId()));
        postparameters2send.add(new BasicNameValuePair("id_recepta",g.getRecId()));
        postparameters2send.add(new BasicNameValuePair("observacio",obse));

        if(image_str != ""){
            postparameters2send.add(new BasicNameValuePair("foto", image_str));
            postparameters2send.add(new BasicNameValuePair("fotomin", image_min));
            Log.e("image_str", "ok");
        }else{
            Log.e("image_min", "null");
        }


        Log.e("num_ing",String.valueOf(num_ing));
        int i= 0, num_ing_ins=0, num_ing_up=0, num_ing_del=0;
        while (i<num_ing){
            Ingredients miIng = colIng.get(i++);
            Log.e("i",String.valueOf(i));
            if( miIng.getValid() && miIng.getModificat() && !miIng.getIsNew()){
                num_ing_up++;
                Log.e("num_ing_up",String.valueOf(num_ing_up));
                String ning_pos = "ing_pos_up"+String.valueOf(num_ing_up);
                Log.e("InsertSendIng",miIng.getNum());
                postparameters2send.add(new BasicNameValuePair(ning_pos,miIng.getNum()));
                String ning_id = "ing_id_up"+String.valueOf(num_ing_up);
                Log.e("InsertSendIng",miIng.getIdIng());
                postparameters2send.add(new BasicNameValuePair(ning_id,miIng.getIdIng()));
                String ning_quan = "ing_quan_up"+String.valueOf(num_ing_up);
                Log.e("InsertSendIng",miIng.getQuan());
                postparameters2send.add(new BasicNameValuePair(ning_quan,miIng.getQuan()));
                String ning_escala = "ing_escala_up"+String.valueOf(num_ing_up);
                Log.e("InsertSendIng",miIng.getEscala());
                postparameters2send.add(new BasicNameValuePair(ning_escala,miIng.getEscala()));
            }
            if( !miIng.getValid() && !miIng.getIsNew()){
                num_ing_del++;
                Log.e("num_ing_del",String.valueOf(num_ing_del));
                String ning_pos = "ing_pos_del"+String.valueOf(num_ing_del);
                Log.e("InsertSendIng",miIng.getNum());
                postparameters2send.add(new BasicNameValuePair(ning_pos,miIng.getNum()));
                String ning_id = "ing_id_del"+String.valueOf(num_ing_del);
                Log.e("InsertSendIng",miIng.getIdIng());
                postparameters2send.add(new BasicNameValuePair(ning_id,miIng.getIdIng()));
                String ning_quan = "ing_quan_del"+String.valueOf(num_ing_del);
                Log.e("InsertSendIng",miIng.getQuan());
                postparameters2send.add(new BasicNameValuePair(ning_quan,miIng.getQuan()));
                String ning_escala = "ing_escala_del"+String.valueOf(num_ing_del);
                Log.e("InsertSendIng",miIng.getEscala());
                postparameters2send.add(new BasicNameValuePair(ning_escala,miIng.getEscala()));
            }
            if( miIng.getValid() && miIng.getIsNew()){
                num_ing_ins++;
                Log.e("num_ing_ins",String.valueOf(num_ing_ins));
                String ning_pos = "ing_pos_ins"+String.valueOf(num_ing_ins);
                Log.e("InsertSendIng",miIng.getNum());
                postparameters2send.add(new BasicNameValuePair(ning_pos,miIng.getNum()));
                String ning_id = "ing_id_ins"+String.valueOf(num_ing_ins);
                Log.e("InsertSendIng",miIng.getIdIng());
                postparameters2send.add(new BasicNameValuePair(ning_id,miIng.getIdIng()));
                String ning_quan = "ing_quan_ins"+String.valueOf(num_ing_ins);
                Log.e("InsertSendIng",miIng.getQuan());
                postparameters2send.add(new BasicNameValuePair(ning_quan,miIng.getQuan()));
                String ning_escala = "ing_escala_ins"+String.valueOf(num_ing_ins);
                Log.e("InsertSendIng",miIng.getEscala());
                postparameters2send.add(new BasicNameValuePair(ning_escala,miIng.getEscala()));
            }
        }
        postparameters2send.add(new BasicNameValuePair("num_ing_up",String.valueOf(num_ing_up)));
        postparameters2send.add(new BasicNameValuePair("num_ing_del",String.valueOf(num_ing_del)));
        postparameters2send.add(new BasicNameValuePair("num_ing_ins",String.valueOf(num_ing_ins)));

        Log.e("num_ins",String.valueOf(num_ins));
        int num_ins_ins=0, num_ins_up=0, num_ins_del=0;
        i=0;
        while (i<num_ins){
            Instruccions miIns = colIns.get(i++);
            if( miIns.getValid() && miIns.getModificat() && !miIns.getIsNew()){
                num_ins_up++;
                String nins_id = "ins_id_up"+String.valueOf(num_ins_up);
                Log.e("InsertSendIns",miIns.getIdIns());
                postparameters2send.add(new BasicNameValuePair(nins_id,miIns.getIdIns()));
                String nins_desc = "ins_desc_up"+String.valueOf(num_ins_up);
                Log.e("InsertSendIns",miIns.getDesc());
                postparameters2send.add(new BasicNameValuePair(nins_desc,miIns.getDesc()));
            }
            if( !miIns.getValid() && !miIns.getIsNew()){
                num_ins_del++;
                String nins_id = "ins_id_del"+String.valueOf(num_ins_del);
                Log.e("InsertSendIns",miIns.getIdIns());
                postparameters2send.add(new BasicNameValuePair(nins_id,miIns.getIdIns()));
                String nins_desc = "ins_desc_del"+String.valueOf(num_ins_del);
                Log.e("InsertSendIns",miIns.getDesc());
                postparameters2send.add(new BasicNameValuePair(nins_desc,miIns.getDesc()));
            }
            if(miIns.getValid() && miIns.getIsNew()){
                num_ins_ins++;
                String nins_id = "ins_id_ins"+String.valueOf(num_ins_ins);
                Log.e("InsertSendIns",miIns.getIdIns());
                postparameters2send.add(new BasicNameValuePair(nins_id,miIns.getIdIns()));
                String nins_desc = "ins_desc_ins"+String.valueOf(num_ins_ins);
                Log.e("InsertSendIns",miIns.getDesc());
                postparameters2send.add(new BasicNameValuePair(nins_desc,miIns.getDesc()));
            }
        }
        postparameters2send.add(new BasicNameValuePair("num_ins_ins",String.valueOf(num_ins_ins)));
        postparameters2send.add(new BasicNameValuePair("num_ins_up",String.valueOf(num_ins_up)));
        postparameters2send.add(new BasicNameValuePair("num_ins_del",String.valueOf(num_ins_del)));

        Log.e("InsertSend",titol + ", " +
                cost + ", "+
                comensal + ", "+
                temp + ", "+
                stcatego + ", "+
                stcoccio + ", "+
                stdificu + ", "+
                stidioma + ", "+
                sttipusp + ", "+
                num_ing_ins + ", "+
                num_ing_up + ", "+
                num_ing_del + ", "+
                num_ins_ins + ", "+
                num_ins_up + ", "+
                num_ins_del + ", "+
                obse + ", "+
                g.getUserId() );

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
                error("No existeix conexió amb la base de dades");
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

        String sttitol, stcomensal,stcost, sttemp, stobse;
        protected void onPreExecute() {
            //para el progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Modificant recepta....");
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
            int status=modstatus(sttitol, stcomensal, stcost, sttemp, stobse);
            //enviamos y recibimos y analizamos los datos en segundo plano.


            if(status>=0){
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
                Toast toast1 = Toast.makeText(context,"Enhorabona, recepta modificada!", Toast.LENGTH_SHORT);
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
        boolean valid, modificat, isNew;

        public Instruccions(String id_ins,String desc,boolean modificat,boolean isNew){
            this.id_ins=id_ins;
            this.desc=desc;
            this.valid=true;
            this.modificat=modificat;
            this.isNew=isNew;
        }

        public void setIns(String id_ins,String desc,boolean modificat,boolean isNew){
            this.id_ins=id_ins;
            this.desc=desc;
            this.valid=true;
            this.modificat=modificat;
            this.isNew=isNew;
        }

        public String getIdIns(){ return this.id_ins; }

        public String getDesc(){
            return this.desc;
        }

        public boolean getValid(){
            return this.valid;
        }

        public boolean getModificat(){
            return this.modificat;
        }
        public boolean getIsNew(){
            return this.isNew;
        }

        public void setNoValid(){
            this.valid=false;
        }
    }

    public class Ingredients {
        private String num, id_ing, quant, escala;
        boolean valid, modificat, isNew;

        public Ingredients(String num,String id_ing,String quant,String escala,boolean modificat,boolean isNew){
            this.num=num;
            this.valid=true;
            this.modificat=modificat;
            this.id_ing=id_ing;
            this.quant=quant;
            this.escala=escala;
            this.isNew=isNew;
        }

        public void setIng(String num,String id_ing,String quant,String escala,boolean modificat,boolean isNew){
            this.num=num;
            this.valid=true;
            this.modificat=modificat;
            this.id_ing=id_ing;
            this.quant=quant;
            this.escala=escala;
            this.isNew=isNew;
        }

        public String getNum(){
            return this.num;
        }

        public boolean getValid(){
            return this.valid;
        }

        public boolean getModificat(){
            return this.modificat;
        }

        public void setNoValid(){
            this.valid=false;
        }

        public boolean getIsNew(){
            return this.isNew;
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

                    foto_recepta.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Failed to load", Toast.LENGTH_SHORT)
                            .show();
                    Log.e("Camera", e.toString());
                }
            }
        } else if (requestCode == SELECT_PICTURE){
            try {
                Uri selectedImage = data.getData();
                InputStream is;
                is = getActivity().getContentResolver().openInputStream(selectedImage);
                BufferedInputStream bis = new BufferedInputStream(is);
                bitmap = BitmapFactory.decodeStream(bis);
                foto_recepta.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {}
            catch (NullPointerException e) {}
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