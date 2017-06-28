package com.axxezo.registerdemo;

/**
 * Created by axxezo on 08/06/2017.
 */

public class People {
    private String DNI;
    private String NAME;

    public People(String DNI, String NAME) {
        this.DNI = DNI;
        this.NAME = NAME;
    }

    public String getDNI() {
        return DNI;
    }

    public void setDNI(String DNI) {
        this.DNI = DNI;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }
}
