package com.axxezo.registerdemo;

/**
 * Created by cristtopher on 6/27/17.
 */

public class Register {
    int id;
    long date;
    String person;
    int sync;
    String pda;
    int allow;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public int getSync() {
        return sync;
    }

    public void setSync(int sync) {
        this.sync = sync;
    }

    public int getAllow() {
        return allow;
    }

    public void setAllow(int allow) {
        this.allow = allow;
    }

    public String getPda() {
        return pda;
    }

    public void setPda(String pda) {
        this.pda = pda;
    }
}
