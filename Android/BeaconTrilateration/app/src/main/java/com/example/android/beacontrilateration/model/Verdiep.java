package com.example.android.beacontrilateration.model;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lottejespers.
 */

public class Verdiep {

    private String naam;
    private List<Beacon> beacons = new ArrayList<>();
    private int image;

    public Verdiep(String naam, int image) {
        this.naam = naam;
        this.image = image;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public List<Beacon> getBeacons() {
        return beacons;
    }

    public void setBeacons(List<Beacon> beacons) {
        this.beacons = beacons;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
