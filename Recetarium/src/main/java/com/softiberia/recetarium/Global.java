package com.softiberia.recetarium;


import android.app.Application;

public class Global extends Application{
    private boolean userLog, modeProp;
    private int menuMode;
    private String userId, mode, recId, recAutorId, userName, recName, start;


    public boolean getUserLog(){ return this.userLog; }

    public void setUserLog(boolean d){ this.userLog=d; }

    public String getUserName(){
        return this.userName;
    }

    public void setUserName(String d){
        this.userName=d;
    }

    public String getUserId(){
        return this.userId;
    }

    public void setUserId(String d){
        this.userId=d;
    }

    public String getRecMode(){
        return this.mode;
    }

    public void setRecMode(String d){
        this.mode=d;
    }

    public String getRecId(){
        return this.recId;
    }

    public void setRecId(String d){
        this.recId=d;
    }

    public String getRecName(){
        return this.recName;
    }

    public void setRecName(String d){
        this.recName=d;
    }

    public String getRecAutorId(){
        return this.recAutorId;
    }

    public void setRecAutorId(String d){
        this.recAutorId=d;
    }

    public int getMenuMode(){
        return this.menuMode;
    }

    public void setMenuMode(int d){
        this.menuMode=d;
    }

    public boolean getModeProp(){ return this.modeProp; }

    public void setModeProp(boolean d){ this.modeProp=d; }

    public String getStart(){ return this.start; }

    public void setStart(String d){ this.start=d; }
}