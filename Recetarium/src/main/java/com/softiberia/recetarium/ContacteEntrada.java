package com.softiberia.recetarium;

import android.graphics.Bitmap;

/**
 * Created by root on 20/11/13.
 */
public class ContacteEntrada {
    private Bitmap idImagen;
    private String nom, tel, id;

    public ContacteEntrada(Bitmap idImagen, String nom, String tel, String id) {
        this.idImagen = idImagen;
        this.nom = nom;
        this.tel = tel;
        this.id = id;
    }

    public String get_nom() {
        return nom;
    }

    public String get_id() { return id; }

    public Bitmap get_idImagen() {
        return idImagen;
    }

    public String get_telefon() { return tel; }

}