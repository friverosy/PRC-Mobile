package com.axxezo.registerdemo;

/**
 * Created by axxezo on 08/06/2017.
 */

public class People {
    private String DNI;
    private String name;
    private String datetime;

    public People(String DNI, String name, String datetime) {
        this.DNI = DNI;
        this.name = name;
        this.datetime = datetime;
    }

    public String getDNI() {
        return DNI;
    }

    public void setDNI(String DNI) {
        this.DNI = DNI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
