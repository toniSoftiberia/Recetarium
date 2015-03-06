package com.softiberia.recetarium;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.parse.ParsePush;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    MenuItem modificar_dades,modificar_pass,visualitzar_not,tancar_sessio, vista_rec, actualitzar_cont,
    visualitzar_rec,modificar_rec,eliminar_rec,cercar_rec,cercar_rec_prop,crear_rec,valorar_rec,compartir_rec, notificacions;
    ArrayList<ContacteEntrada> listacontactos, listagentetel, listagentebd,listacontactosbd;



    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    static Global ge;
    private Global g;
    private int inici = 0, noti_pendent;
    Httppostaux post;
    Boolean menuAct;
    ActionBar actionBar;

    // String URL_connect="http://www.scandroidtest.site90.com/acces.php";
    String IP_Server="www.softiberia.com";//IP DE NUESTRO PC
    String URL_connect="http://"+IP_Server+"/droidlogin/addval.php";//ruta en donde estan nuestros archivos
    String URL_cuiners="http://"+IP_Server+"/droidlogin/selectcuiners.php";//ruta en donde estan nuestros archivos
    String URL_actu="http://"+IP_Server+"/droidlogin/actconta.php";//ruta en donde estan nuestros archivos
    String URL_select="http://"+IP_Server+"/droidlogin/selectconta.php";//ruta en donde estan nuestros archivos
    String URL_eliminar="http://"+IP_Server+"/droidlogin/delrec.php";//ruta en donde estan nuestros archivos
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        g = (Global)getApplication();
        g.setUserId("-1");
        g.setUserLog(false);
        post=new Httppostaux();
        menuAct = false;
        noti_pendent = 0;
        ge = (Global)getApplication();
        actionBar = getActionBar();

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            g.setStart("app");
        } else {
            g.setStart("push");
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container,  PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        // Inflate the menu; this adds items to the action bar if it is present.
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();

            compartir_rec	= menu.findItem(R.id.compartir_rec);
            modificar_dades	= menu.findItem(R.id.modificar_dades);
            modificar_pass	= menu.findItem(R.id.modificar_pass);
            visualitzar_not	= menu.findItem(R.id.visualitzar_not);
            tancar_sessio	= menu.findItem(R.id.tancar_sessio);
            visualitzar_rec	= menu.findItem(R.id.visualitzar_rec);
            vista_rec = menu.findItem(R.id.vista_rec);
            modificar_rec	= menu.findItem(R.id.modificar_rec);
            eliminar_rec	= menu.findItem(R.id.eliminar_rec);
            cercar_rec	= menu.findItem(R.id.cercar_rec);
            cercar_rec_prop	= menu.findItem(R.id.cercar_rec_prop);
            crear_rec	= menu.findItem(R.id.crear_rec);
            valorar_rec	= menu.findItem(R.id.valorar_rec);
            actualitzar_cont	= menu.findItem(R.id.actualitzar_cont);
            notificacions = menu.findItem(R.id.notPen);


            desactivarOpcions();
            if(g.getUserLog()){
                notificacions.setVisible(true);
                setNumNoti(noti_pendent);
            }
            switch (g.getMenuMode()) {
                case 1:
                    activarOpcionsCuinerLog();
                    return true;
                case 2:
                    activarOpcionsReceptesLog();
                    return true;
                case 3:
                    activarOpcionsRecepta();
                    return true;
                case 4:
                    activarOpcionsReceptaPropia();
                    return true;

            }
            menuAct = true;
            //desactivarOpcions();
            Log.e("Paso ", "hola");

            return true;
        }


        return super.onCreateOptionsMenu(menu);


    }

    public void setNumNoti(int pendents){
        Log.e("Pendents: ", ""+pendents);
        noti_pendent = pendents;
        int icon = R.drawable.ic_action_0;
        if (pendents==1) icon = R.drawable.ic_action_1;
        else if (pendents==2) icon = R.drawable.ic_action_2;
        else if (pendents==3) icon = R.drawable.ic_action_3;
        else if (pendents==4) icon = R.drawable.ic_action_4;
        else if (pendents==5) icon = R.drawable.ic_action_5;
        else if (pendents==6) icon = R.drawable.ic_action_6;
        else if (pendents==7) icon = R.drawable.ic_action_7;
        else if (pendents==8) icon = R.drawable.ic_action_8;
        else if (pendents==9) icon = R.drawable.ic_action_9;
        else if (pendents==10) icon = R.drawable.ic_action_10;
        else if (pendents>10) icon = R.drawable.ic_action_100;
        notificacions.setIcon(icon);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        AlertDialog.Builder builder;
        switch (item.getItemId()) {

            case R.id.modificar_dades:
                //nos presenta el formulario para modificar datos
                FragmentCuinerMod fragmentD  = new FragmentCuinerMod();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentD)
                        .addToBackStack(null)
                        .commit();
                return true;

            case R.id.modificar_pass:
                //nos presenta el formulario para modificar contraseña
                FragmentCuinerPass fragmentP  = new FragmentCuinerPass();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentP)
                        .addToBackStack(null)
                        .commit();
                return true;

            case R.id.visualitzar_not:
                FragmentReceptesNotificacions frn = new FragmentReceptesNotificacions();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, frn)
                        .addToBackStack(null)
                        .commit();
                return true;

            case R.id.tancar_sessio:
                //'cerrar  sesion' nos regresa a la ventana anterior.
                MainFragmentCuiner fragmentC  = new MainFragmentCuiner();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentC)
                        .addToBackStack(null)
                        .commit();
                g.setUserId("-1");
                g.setUserLog(false);
                g.setMenuMode(0);
                notificacions.setVisible(false);
                SharedPreferences prefs = getSharedPreferences("config", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("autolog",false);
                editor.commit();
                return true;

            case R.id.visualitzar_rec:
                FragmentReceptesLlistat frl = new FragmentReceptesLlistat();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, frl)
                        .addToBackStack(null)
                        .commit();
                g.setRecMode("V");
                return true;

            case R.id.vista_rec:
                Toast toastVis = Toast.makeText(getApplicationContext(), "Selecciona la recepta a visualitzar", 5);
                toastVis.show();
                g.setRecMode("V");
                return true;

            case R.id.modificar_rec:
                desactivarOpcions();
                FragmentReceptaModificar fragmentM  = new FragmentReceptaModificar();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentM,"FragmentReceptaModificar")
                        .addToBackStack(null)
                        .commit();
                //Toast toastMod = Toast.makeText(getApplicationContext(), "Selecciona la recepta a modificar", 5);
                //toastMod.show();
                g.setRecMode("M");
                return true;

            case R.id.eliminar_rec:
                desactivarOpcions();
                builder = new AlertDialog.Builder(this);
                builder.setMessage("Estas segur?").setPositiveButton("Sí", dialogConf)
                        .setNegativeButton("No", dialogConf).show();
                //Toast toastDel = Toast.makeText(getApplicationContext(), "Selecciona la recepta a eliminar", 5);
                //toastDel.show();
                g.setRecMode("E");
                return true;

            case R.id.cercar_rec:
                //Cancelamos i volvemos al login
                MainFragmentReceptes fmr = new MainFragmentReceptes();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fmr)
                        .addToBackStack(null)
                        .commit();
                g.setModeProp(false);
                return true;

            case R.id.cercar_rec_prop:
                //Cancelamos i volvemos al login
                MainFragmentReceptes fmrp = new MainFragmentReceptes();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fmrp)
                        .addToBackStack(null)
                        .commit();
                g.setModeProp(true);
                return true;

            case R.id.crear_rec:
                FragmentReceptaCrear frc = new FragmentReceptaCrear();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, frc,"FragmentReceptaCrear")
                        .addToBackStack(null)
                        .commit();
                return true;

            case R.id.compartir_rec:
                getContactos(g.getUserId());
                return true;

            case R.id.actualitzar_cont:
                actualitzarContactes(getContentResolver(),g.getUserId());
                return true;

            case R.id.valorar_rec:
                builder = new AlertDialog.Builder(this);
                    //Obtenemos el layout inflater
                LayoutInflater inflater = this.getLayoutInflater();
                    // Cargamos el layout personalizado para el dialogo
                    // Pasamos null como padre pues este layout ira en el dialogo
                View layout = inflater.inflate( R.layout.fragment_receptes_valorar, null);
                    //Definimos el EditText para poder acceder a el posteriormente
                final RatingBar valoracio = ((RatingBar) layout.findViewById(R.id.valoracio));
                final TextView comentari = ((TextView) layout.findViewById(R.id.comentari));
                    //Substituimos la vista por la que acabamos de cargar.
                builder.setView(layout);
                builder.setTitle("Valorar recepta");
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Acciones a realizar al pulsar el boton Aceptar
                        float fl_val = valoracio.getRating();
                        if (fl_val==0) {
                            error("Has d'introduir alguna valoració");
                        } else {
                            String st_com = comentari.getText().toString();
                            if (st_com=="") {
                                error("Has d'introduir algun comentari");
                            }else{
                                //Procedim
                                new asyncCrearValoracio().execute( String.valueOf(fl_val) ,st_com ,g.getUserId() ,g.getRecId() ,g.getRecAutorId() );
                            }
                        }
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
                return true;

            case R.id.notPen:
                FragmentReceptesNotificacions fnp = new FragmentReceptesNotificacions();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fnp)
                        .addToBackStack(null)
                        .commit();
                g.setMenuMode(1);
                mNavigationDrawerFragment.selectItem(3);
                actionBar.setTitle("Perfil");
                return true;


        }
        return super.onOptionsItemSelected(item);
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
    //vibra y muestra un Toast
    public void error(String strerror){
        Vibrator vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);

        Context context = this.getApplicationContext();
        Toast toast1 = Toast.makeText(context,"Error: "+strerror, Toast.LENGTH_SHORT);
        toast1.show();
    }

    public void desactivarOpcions(){
        modificar_dades.setVisible(false);
        modificar_pass.setVisible(false);
        visualitzar_not.setVisible(false);
        tancar_sessio.setVisible(false);
        visualitzar_rec.setVisible(false);
        vista_rec.setVisible(false);
        modificar_rec.setVisible(false);
        eliminar_rec.setVisible(false);
        cercar_rec.setVisible(false);
        cercar_rec_prop.setVisible(false);
        crear_rec.setVisible(false);
        valorar_rec.setVisible(false);
        compartir_rec.setVisible(false);
        actualitzar_cont.setVisible(false);

    }

    public void activarOpcionsCuinerLog(){
        //1
        modificar_dades.setVisible(true);
        modificar_pass.setVisible(true);
        //visualitzar_not.setVisible(true);
        tancar_sessio.setVisible(true);
        actualitzar_cont.setVisible(true);
        notificacions.setVisible(true);
    }

    public void activarOpcionsReceptesLog(){
        //2
        cercar_rec_prop.setVisible(true);
        crear_rec.setVisible(true);
        notificacions.setVisible(true);
    }

    public void activarOpcionsRecepta(){
        //3
        valorar_rec.setVisible(true);
        compartir_rec.setVisible(true);
        notificacions.setVisible(true);
    }

    public void activarOpcionsReceptaPropia(){
        //4
        valorar_rec.setVisible(true);
        compartir_rec.setVisible(true);
        modificar_rec.setVisible(true);
        eliminar_rec.setVisible(true);
        notificacions.setVisible(true);
    }

/*
valoració
 */


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

        String valoracio, comentari, id_autor_comentari, id_recepta, id_autor_recepta;
        protected void onPreExecute() {
            //para el progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Enviant comentari....");
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

            String resultat="errInd";
            int status=valoraciostatus(valoracio, comentari, id_autor_comentari, id_recepta, id_autor_recepta);
            //enviamos y recibimos y analizamos los datos en segundo plano.

            resultat=String.valueOf(status);
            if(status==0){
                resultat="ok";
            }else if(status==1){
                resultat="temps";
            }else if(status==2){
                resultat="errActVal";
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
                Toast toast = Toast.makeText(getApplicationContext(), "Enhorabona, recepta valorada!", 5);
                toast.show();
                sendParse("user_"+g.getRecAutorId(), g.getUserName()+", ha comentat la recepta: "+g.getRecName());
            }else if(resultat.equals("temps")){
                error("Has d'esperar una semana para tornar a valorar-la!");
            }else if(resultat.equals("errActVal")){
                error("No s'ha pogut actualizar la valoració!");
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
    /*
    Contactos
     */

    public void actualitzarContactes(ContentResolver contentresolver,String id_cuiner){

        listagentetel = consultaAgendaTelefono(contentresolver);
        listagentebd = new ArrayList<ContacteEntrada>();
        //obtenir contactes bd
        new asyncSelectContactes().execute(id_cuiner);
    }

    private ArrayList<ContacteEntrada> match(ArrayList<ContacteEntrada> ltel, ArrayList<ContacteEntrada> lbd){
        ArrayList<ContacteEntrada> res = new ArrayList<ContacteEntrada>();
        for(int i=0; i<ltel.size(); i++){
            for(int j=0; j<lbd.size(); j++){
                if( (ltel.get(i).get_telefon()).equals(lbd.get(j).get_telefon())
                        || (ltel.get(i).get_telefon()).equals("+34"+lbd.get(j).get_telefon()) ){
                    res.add(new ContacteEntrada(lbd.get(j).get_idImagen(),lbd.get(j).get_nom(),
                            lbd.get(j).get_telefon(), lbd.get(j).get_id()));

                    Log.e("Match=", ltel.get(i).get_nom() +":" + ltel.get(i).get_telefon());
                }
            }
        }
        return res;
    }

    private ArrayList<ContacteEntrada> consultaAgendaTelefono(ContentResolver contentresolver){

        ArrayList<ContacteEntrada> datos = new ArrayList<ContacteEntrada>();

        // Query: contacts with phone shorted by name
        //lanzamos una query al Content provider por medio del "contentresolver"
        //y guardamos la tabla de los resultados que nos devuelve con un Cursor
        //para iterar despues en las filas con el objeto de clase Cursor.
        //(Es como una tabla de filas y columnas)
        Cursor mCursor = contentresolver.query(
                ContactsContract.Data.CONTENT_URI,
                new String[] { ContactsContract.Data._ID, ContactsContract.Data.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE },
                ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        + "' AND "
                        + ContactsContract.CommonDataKinds.Phone.NUMBER + " IS NOT NULL", null,
                ContactsContract.Data.DISPLAY_NAME + " ASC");

        // Esto asocia el ciclo de vida del cursor al ciclo de vida de la Activity. Si
        // la Activity para, el sistema libera el Cursor. No quedan recursos bloqueados.
        startManagingCursor(mCursor);

        // Iteramos sobre la lista de resultados a través del cursor.

        int nameIndex = mCursor.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME);
        int numberIndex = mCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);
        if (mCursor.moveToFirst()) {
            do {
                String name = mCursor.getString(nameIndex);
                String number = mCursor.getString(numberIndex);
                Bitmap foto = BitmapFactory.decodeResource(getResources(), R.drawable.cuiner);
                datos.add(new ContacteEntrada(foto, name, number, "0"));
                Log.e("Cursor=", name +":" + number);

            } while (mCursor.moveToNext());
        }
        return datos;
    }

    public void getContactos(String id_cuiner){
        new asyncSelect().execute();
    }

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public JSONObject conselect() {
        JSONObject json_data=null;
        listacontactosbd = new ArrayList<ContacteEntrada>();
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
        Log.e("con_id_cuiner",g.getUserId());
        postparameters2send.add(new BasicNameValuePair("id_cuiner", g.getUserId()));
        JSONArray jdata=post.getserverdata(postparameters2send, URL_select);
        //si lo que obtuvimos no es null
        int i=0;
        while (jdata!=null && jdata.length() > 0 && i<jdata.length()){
            try {
                json_data = jdata.getJSONObject(i++); //leemos el primer segmento en nuestro caso el unico

                Bitmap foto = BitmapFactory.decodeResource(getResources(), R.drawable.cuiner);
                if(json_data.getString("foto_min")!="null"){
                    byte[] decodedString = Base64.decode(json_data.getString("foto_min"), Base64.NO_WRAP);
                    foto = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                }
                listacontactosbd.add(new ContacteEntrada(foto, json_data.getString("alias"), json_data.getString("telefon"), json_data.getString("id_cuiner")));
                Log.e("i = ", (i - 1) + " alias = " + json_data.getString("alias"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                json_data=null;
            }
        }
        return json_data;
    }

    class asyncSelect extends AsyncTask< String, String, String > {
        JSONObject data=null;

        protected String doInBackground(String... params) {
            //enviamos y recibimos y analizamos los datos en segundo plano.
            data=conselect();
            Log.e("doInBackgroundsc-status=", "" + data);
            if (data!=null){
                return "ok";
            }else{
                return "errBD";
            }
        }

        protected void onPostExecute(String result) {
            Log.e("onPostExecutesc=", "" + result);
            if (result.equals("ok")){
                FragmentCuinerContactes fragment = new FragmentCuinerContactes();
                fragment.setTotalDatos(listacontactosbd);
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }else{
                error("Contactes no trobats");
            }
        }
    }

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public JSONObject constatus() {
        JSONObject json_data=null;
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
        JSONArray jdata=post.getserverdata(postparameters2send, URL_cuiners);
        //si lo que obtuvimos no es null
        int i=0;
        while (jdata!=null && jdata.length() > 0 && i<jdata.length()){
            try {
                json_data = jdata.getJSONObject(i++); //leemos el primer segmento en nuestro caso el unico
                Bitmap foto = BitmapFactory.decodeResource(getResources(), R.drawable.cuiner);
                if(json_data.getString("foto_min")!="null"){
                    byte[] decodedString = Base64.decode(json_data.getString("foto_min"), Base64.NO_WRAP);
                    foto = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                }
                listagentebd.add(new ContacteEntrada(foto, json_data.getString("alias"), json_data.getString("telefon"), json_data.getString("id_cuiner")));
                Log.e("i = ", (i - 1) + " alias = " + json_data.getString("alias"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                json_data=null;
            }
        }
        return json_data;
    }

    class asyncSelectContactes extends AsyncTask< String, String, String > {

        JSONObject data=null;

        protected String doInBackground(String... params) {
            //enviamos y recibimos y analizamos los datos en segundo plano.
            data=constatus();
            Log.e("doInBackgroundsc-status=", "" + data);
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
            Log.e("onPostExecutesc=", "" + result);
            if (result.equals("ok")){
                listacontactos = match(listagentetel, listagentebd);
                new asyncActContactes().execute();
            }else{
                error("L'operació no s'ha pogut dur a terme");
            }

        }

    }

    /*Valida el estado del logueo solamente necesita como parametros el usuario y passw*/
    public int conact() {
        JSONObject json_data=null; //creamos un objeto JSON
        int status;
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
        Log.e("con_id_cuiner",g.getUserId());
        postparameters2send.add(new BasicNameValuePair("con_id_cuiner", "" + g.getUserId()));
        //Log.e("listacontactos.size()",""+listacontactos.size());
        int numCont = 0;
        for (int i =0; i<listacontactos.size(); i++){
            numCont++;
            listacontactos.get(i).get_nom();
            ContacteEntrada contacte = listacontactos.get(i);
            String ning_pos = "con_id_cont"+String.valueOf(numCont);
            Log.e(ning_pos+" = ",listacontactos.get(i).get_id());
            postparameters2send.add(new BasicNameValuePair(ning_pos,listacontactos.get(i).get_id()));
        }
        postparameters2send.add(new BasicNameValuePair("num_con",""+numCont));


        //realizamos una peticion y como respuesta obtenes un array JSON
        JSONArray jdata=post.getserverdata(postparameters2send, URL_actu);

        //si lo que obtuvimos no es null
        status=-1;
        if (jdata!=null && jdata.length() > 0){
            try {
                json_data = jdata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
                status=json_data.getInt("status");//accedemos al valor
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return status;

    }

    class asyncActContactes extends AsyncTask< String, String, String > {

        int status;

        protected String doInBackground(String... params) {
            //enviamos y recibimos y analizamos los datos en segundo plano.
            status=conact();
            Log.e("doInBackgroundac-status=", "" + status);
            if (status==0){
                return "ok";
            }else{
                return "errBD";
            }
        }

        /*Una vez terminado doInBackground segun lo que halla ocurrido
        pasamos a la sig. activity
        o mostramos error*/

        protected void onPostExecute(String result) {
            Log.e("onPostExecuteac=", "" + result);
            Toast toastAct;
            if (result.equals("ok")){
                toastAct = Toast.makeText(getApplication().getApplicationContext(), "Contactes actualitzats", 5);
            }else{
                toastAct = Toast.makeText(getApplication().getApplicationContext(), "Contactes no actualitzats", 5);
            }
            toastAct.show();

        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int position = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView=null;
            switch (position) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_inici, container, false);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_receptes_busqueda, container, false);
                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.fragment_receptes_llistat, container, false);
                    break;
                case 4:
                    if(ge.getUserLog()){
                        rootView = inflater.inflate(R.layout.fragment_receptes_notificacions, container, false);
                    }else{
                        rootView = inflater.inflate(R.layout.fragment_cuiner, container, false);
                    }
                    break;
            }
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
        }
    }
    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
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
                Context context = getApplicationContext();
                Toast toast1 = Toast.makeText(context,"Enhorabona, la recepta a s'ha eliminat!", Toast.LENGTH_SHORT);
                toast1.show();
            }else{
                error("No existeix connexió amb la base de dades");
            }

        }

    }

}