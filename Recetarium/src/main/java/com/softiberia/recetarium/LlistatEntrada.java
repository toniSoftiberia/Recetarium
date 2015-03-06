package com.softiberia.recetarium;

import android.graphics.Bitmap;

/**
 * Created by root on 20/11/13.
 */
public class LlistatEntrada {
    private Bitmap idImagen;
    private String texto, id, idAutor;

    public LlistatEntrada(Bitmap idImagen, String texto, String id) {
        this.idImagen = idImagen;
        this.texto = texto;
        this.id = id;
    }

    public String get_texto() {
        return texto;
    }

    public String get_id() { return id; }

    public Bitmap get_idImagen() {
        return idImagen;
    }

    public String get_idAutor() { return idAutor; }

    public void set_idAutor(String idAutor){
        this.idAutor = idAutor;
    }
}