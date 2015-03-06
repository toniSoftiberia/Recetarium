package com.softiberia.recetarium;

/**
 * Created by root on 20/11/13.
 */
public class NotificacioEntrada {
    private long nota;
    private String id, comentari, emisor, data, hora, id_recepta, recepta, autor_recepta;
    private int vist;

    public NotificacioEntrada(String id, long nota, String comentari, String emisor, String data,
                              String hora, String id_recepta, String recepta, String autor_recepta, int vist) {
        this.id = id;
        this.nota = nota;
        this.comentari = comentari;
        this.emisor = emisor;
        this.data = data;
        this.hora = hora;
        this.id_recepta = id_recepta;
        this.recepta = recepta;
        this.autor_recepta = autor_recepta;
        this.vist = vist;
    }

    public String get_id() { return id; }
    public long get_nota() { return nota; }
    public String get_comentari() { return comentari; }
    public String get_emisor() { return emisor; }
    public String get_data() { return data; }
    public String get_hora() { return hora; }
    public String get_id_recepta() { return id_recepta; }
    public String get_recepta() { return recepta; }
    public String get_autor_recepta() { return autor_recepta; }
    public int get_vist() { return vist; }
    public void set_vist(int vist) { this.vist=vist; }
}