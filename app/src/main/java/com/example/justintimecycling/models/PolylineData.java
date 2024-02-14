package com.example.justintimecycling.models;

import com.google.android.gms.maps.model.Polyline;
import com.google.maps.model.DirectionsLeg;

public class PolylineData {

    private Polyline polyline;

    public PolylineData(Polyline polyline) {
        this.polyline = polyline;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    @Override
    public String toString() {
        return "PolylineData{" +
                "polyline=" + polyline + '}';
    }
}
