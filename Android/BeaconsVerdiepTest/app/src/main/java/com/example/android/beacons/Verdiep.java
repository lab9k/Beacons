package com.example.android.beacons;

/**
 * Created by lottejespers.
 */

public class Verdiep {

    private String id;
    private int verdiep;

    public Verdiep(String id, int verdiep) {
        this.id = id;
        this.verdiep = verdiep;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVerdiep() {
        return verdiep;
    }

    public void setVerdiep(int verdiep) {
        this.verdiep = verdiep;
    }
}
