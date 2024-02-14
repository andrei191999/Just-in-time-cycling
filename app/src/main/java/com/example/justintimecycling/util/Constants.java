package com.example.justintimecycling.util;

import com.google.android.gms.maps.model.LatLng;

public class Constants {

    // Keys for storing activity state.
    public static final String KEY_CAMERA_POSITION = "camera_position";
    public static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    public static final int M_MAX_ENTRIES = 5;

    // A default location (UoL, Liverpool, UK) and default zoom to use when location permission is
    // not granted.
    public static final LatLng defaultLocation = new LatLng(53.406083, -2.965444);
    public static final int DEFAULT_ZOOM = 15;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9003;
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

}
