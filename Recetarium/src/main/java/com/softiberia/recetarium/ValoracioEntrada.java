package com.softiberia.recetarium;

/**
 * Created by root on 20/11/13.
 */
public class ValoracioEntrada {
    private long nota;
    private String id, comentari, emisor, data, hora;

    public ValoracioEntrada(String id, long nota, String comentari, String emisor, String data, String hora ) {
        this.id = id;
        this.nota = nota;
        this.comentari = comentari;
        this.emisor = emisor;
        this.data = data;
        this.hora = hora;
    }

    public String get_id() { return id; }
    public long get_nota() { return nota; }
    public String get_comentari() { return comentari; }
    public String get_emisor() { return emisor; }
    public String get_data() { return data; }
    public String get_hora() { return hora; }
}