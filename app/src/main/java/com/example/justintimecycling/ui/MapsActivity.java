package com.example.justintimecycling.ui;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.justintimecycling.BuildConfig;
import com.example.justintimecycling.R;
import com.example.justintimecycling.UserClient;
import com.example.justintimecycling.models.PolylineData;
import com.example.justintimecycling.models.User;
import com.example.justintimecycling.models.UserLocation;
import com.example.justintimecycling.services.LocationService;
import com.example.justintimecycling.services.ReminderBroadcast1;
import com.example.justintimecycling.services.ReminderBroadcast2;
import com.example.justintimecycling.services.ReminderBroadcast5;
import com.firebase.client.annotations.NotNull;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TransitRoutingPreference;
import com.google.maps.model.TravelMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.example.justintimecycling.R.id;
import static com.example.justintimecycling.R.layout;
import static com.example.justintimecycling.R.string;
import static com.example.justintimecycling.util.Constants.DEFAULT_ZOOM;
import static com.example.justintimecycling.util.Constants.ERROR_DIALOG_REQUEST;
import static com.example.justintimecycling.util.Constants.KEY_CAMERA_POSITION;
import static com.example.justintimecycling.util.Constants.KEY_LOCATION;
import static com.example.justintimecycling.util.Constants.M_MAX_ENTRIES;
import static com.example.justintimecycling.util.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.justintimecycling.util.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.justintimecycling.util.Constants.defaultLocation;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;
    private UserLocation userLocation;
    private PlacesClient placesClient;
    private FirebaseFirestore mDb;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;
    private boolean gpsEnabled;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Used for selecting the current place.
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;

    private FirebaseAuth fAuth;
    private GeoApiContext mGeoApiContext = null;
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();
    private Marker mSelectedMarker = null;
    private Button directBtn;
    private CardView cardView;
    private int distanceProgress, timeProgressFin;
    private ZonedDateTime timeProgress = null;
    private boolean doubleBackExit = false;

    //navigation shit
    private PolylineData selectedPolyline;
    private ArrayList<DirectionsRoute> routes = new ArrayList<>();
    private ArrayList<Marker> markers = new ArrayList<>();

    private RequestQueue mQueue;
    private ArrayList<String> placeIds = new ArrayList<>();
    private ArrayList<String> placeAddresses = new ArrayList<>();
    private ArrayList<ZonedDateTime> departureTime = new ArrayList<>();
    private HashMap<Integer, ArrayList<String>> instructions = new HashMap<>();
    private ArrayList<ZonedDateTime> arrivalTimes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(layout.activity_maps);
        directBtn = findViewById(id.directions_btn);
        directBtn.setVisibility(View.INVISIBLE);
        cardView = findViewById(id.card_view);

        mQueue = Volley.newRequestQueue(this);
        bottomNavigation();
        createNotificationChannel();

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        //directions stuff
        if(mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder().apiKey(BuildConfig.MAPS_API_KEY).build();
        }

        //firebase stuff
        fAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(id.autocomplete_fragment);

        // Specify the types of place data to return.
        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Collections.singletonList(Place.Field.ID));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + ", " + place.getId());
                directBtn.setVisibility(View.VISIBLE);
                findPlace(place.getId());
            }
            @Override
            public void onError(@NotNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void makeRequest(String address, com.google.maps.model.LatLng location) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+location+"&key="+BuildConfig.MAPS_API_KEY;
        Log.d(TAG, "request " + location);

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());
                        try {
                            boolean found = false;
                            JSONArray jsonArray = response.getJSONArray("results");
                            for(int i = 0; i < jsonArray.length(); i++) {
                                boolean bus = false;
                                JSONObject result = jsonArray.getJSONObject(i);
                                JSONArray types = result.getJSONArray("types");
                                for(int j = 0; j < types.length(); j++) {
                                    if(types.get(j).toString().contains("bus")) {
                                        bus = true;
                                    }
                                    if(types.get(j).toString().contains("station") && !bus) {
                                        if(!placeIds.contains(result.get("place_id").toString())) {
                                            found = true;
                                            placeAddresses.add(result.get("formatted_address").toString());
                                            placeIds.add(result.get("place_id").toString());
                                            Log.d(TAG, "calculate request " + result.get("formatted_address").toString() + " " + location);
                                        }
                                    }
                                }
                            }
                            if(found)
                                Thread.sleep(1000);
                            else
                                makeAnotherRequest(address);
                        } catch (JSONException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        mQueue.add(request);
    }

        private void makeAnotherRequest(String address) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address="+address+"station&region=uk&key="+BuildConfig.MAPS_API_KEY;
        Log.d(TAG, "another request " + address);

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());
                        try {
                            boolean found = false;
                            JSONArray jsonArray = response.getJSONArray("results");
                            for(int i = 0; i < jsonArray.length(); i++) {
                                boolean bus = false;
                                JSONObject result = jsonArray.getJSONObject(i);
                                JSONArray types = result.getJSONArray("types");
                                for(int j = 0; j < types.length(); j++) {
                                    if(types.get(j).toString().contains("bus")) {
                                        bus = true;
                                    }
                                    if(types.get(j).toString().contains("station") && !bus) {
                                        if(!placeIds.contains(result.get("place_id").toString())) {
                                            found = true;
                                            placeAddresses.add(result.get("formatted_address").toString());
                                            placeIds.add(result.get("place_id").toString());
                                            Log.d(TAG, "calculate request " + result.get("formatted_address").toString() + " " + address);
                                        }
                                    }
                                }
                            }
                            if(found)
                                Thread.sleep(1000);
                            else {
                                placeAddresses.add(null);
                                placeIds.add(null);
                            }

                        } catch (JSONException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        mQueue.add(request);
    }

    private void calculateTotalDirections(Marker marker) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude, marker.getPosition().longitude);
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(new com.google.maps.model.LatLng(
                userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()));

        directions.mode(TravelMode.TRANSIT);
        directions.transitMode(TransitMode.RAIL);
        directions.transitRoutingPreference(TransitRoutingPreference.FEWER_TRANSFERS);

        Log.d(TAG, "calculateTotalDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResult(DirectionsResult result) {

                for(DirectionsRoute route : result.routes) {
                    DirectionsLeg leg = route.legs[0];
                    //check if route contains any buses or trams or other non permitted transport modes
                    for (DirectionsStep step : leg.steps)
                        if (step.travelMode == TravelMode.TRANSIT) {
                            if (step.transitDetails.line.vehicle.name.contains("Train") || step.transitDetails.line.vehicle.name.contains("Subway") ||
                                    step.transitDetails.line.vehicle.name.contains("Metro") || step.transitDetails.line.vehicle.name.contains("Ferry")) {
                                //remove for alternate routes
                                if(routes.size() == 1)
                                    break;
                                if(!routes.contains(route)) {
                                    Log.d(TAG, "route added");
                                    routes.add(route);
                                }
                            }
                        }
                }

                if(routes.size() > 0) {
                    for (DirectionsRoute route : routes)
                        for (DirectionsLeg leg : route.legs)
                            for(int i = 0; i < leg.steps.length; i++) {
                                DirectionsStep step = leg.steps[i];
                                if (step.travelMode == TravelMode.TRANSIT)
                                    try {
                                        if(leg.steps[i-1].travelMode == TravelMode.WALKING && step != leg.steps[0] && leg.steps[i-1].distance.inMeters > 50)
                                        {
                                            makeRequest(step.transitDetails.departureStop.name, step.transitDetails.departureStop.location);
                                            Log.d(TAG, "request " + step.transitDetails.departureStop.name + " " + step.transitDetails.departureStop.location);
                                            Thread.sleep(2500);
                                        }
                                        if(leg.steps[i+1].travelMode == TravelMode.WALKING && step != leg.steps[leg.steps.length - 1] && leg.steps[i+1].distance.inMeters > 50)
                                        {
                                            makeRequest(step.transitDetails.arrivalStop.name, step.transitDetails.arrivalStop.location);
                                            Log.d(TAG, "request " + step.transitDetails.arrivalStop.name + " " + step.transitDetails.arrivalStop.location);
                                            Thread.sleep(2500);
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                            }
                }

                Log.d(TAG, "calculate requests ARRAY " + placeAddresses);

                int counter = 0;
                if(routes.size() > 0) {
                    for (DirectionsRoute route : routes) {
                        for (DirectionsLeg leg : route.legs) {
                            Log.d(TAG, "calculateTotalDirections: legs: " + leg + "\n");
                            for(int i = 0; i < leg.steps.length; i++) {
                                DirectionsStep step = leg.steps[i];
                                Log.d(TAG, "calculateTotalDirections: steps: " + step);
                                if (step.travelMode == TravelMode.TRANSIT) {
                                    //Log.d(TAG, "calculateTotalDirections transit dets vehicle " + step.transitDetails.line.vehicle);
                                    if (step.transitDetails.line.vehicle.name.contains("Train") || step.transitDetails.line.vehicle.name.contains("Subway") ||
                                            step.transitDetails.line.vehicle.name.contains("Rail") || step.transitDetails.line.vehicle.name.contains("Ferry")) {
                                        timeProgress = step.transitDetails.arrivalTime;
                                        departureTime.add(step.transitDetails.departureTime);
                                        Log.d(TAG, "transit dets " + step.transitDetails.departureStop + " " + step.transitDetails.arrivalStop);
                                        addStepPolylinesToMap(step);
                                        ArrayList<String> stepInstructions = new ArrayList<>();
                                        stepInstructions.add("Train");
                                        stepInstructions.add(step.transitDetails.line.vehicle.name+" from "+step.transitDetails.departureStop.name+" to "+step.transitDetails.arrivalStop.name);
                                        stepInstructions.add("Board the " + " " + step.transitDetails.line.name + " " +
                                                step.transitDetails.line.vehicle.name + " at " + step.transitDetails.departureStop.name + " at " +
                                                step.transitDetails.departureTime.toLocalTime());
                                        stepInstructions.add("Stay on for " + step.duration + " and "+ step.transitDetails.numStops + " stop(s). Get off at " +
                                                step.transitDetails.arrivalStop.name + " at " + step.transitDetails.arrivalTime.toLocalTime());
                                        if(step.transitDetails.headway != 0)
                                            stepInstructions.add("If you miss the " + step.transitDetails.line.vehicle.name + " you will have to wait for another " +
                                                step.transitDetails.headway + " for the next " + step.transitDetails.line.vehicle.name);
                                        instructions.put(i, stepInstructions);
                                    } else if (step.transitDetails.line.vehicle.name.contains("Bus")) {
                                        if(placeIds.get(0) != null && step.distance.inMeters > 50)
                                            calculateDirections(step.startLocation, step.endLocation, null, null, i);
                                    }
                                } else if (step.travelMode == TravelMode.WALKING) {
                                    if(step == leg.steps[0]) {
                                        counter++;
                                        Log.d(TAG, "calculate total step 0 cycling place dets end " + placeIds.get(0) + " " + placeAddresses.get(0));
                                        if(placeIds.get(0) != null && step.distance.inMeters > 50)
                                            calculateDirections(step.startLocation, null, null, placeIds.get(0), i);
                                    } else if(step == leg.steps[leg.steps.length - 1]) {
                                        Log.d(TAG, "calculate total last step cycling place dets start " + placeIds.get(placeIds.size() - 1) + " " + placeAddresses.get(placeAddresses.size() - 1));
                                        if(placeIds.get(placeIds.size() - 1) != null && step.distance.inMeters > 50)
                                            calculateDirections(null, step.endLocation, placeIds.get(placeIds.size() - 1), null, i);
                                    }
                                    else {
                                        Log.d(TAG, "calculate total middle step cycling place dets end " + placeIds.get(counter+1) + " " + placeAddresses.get(counter+1));
                                        Log.d(TAG, "calculate total middle step cycling place dets start " + placeIds.get(counter) + " " + placeAddresses.get(counter));
                                        if(placeIds.get(counter) != null && placeIds.get(counter+1) != null && step.distance.inMeters > 50)
                                            calculateDirections(null, null, placeIds.get(counter), placeIds.get(counter+1), i);
                                        counter += 2;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateTotalDirections: Failed to get total directions: " + e.getMessage() );

            }
        });
        try{
            Thread.sleep(3000);
            if(routes.size() == 0) {
                calculateDirections(new com.google.maps.model.LatLng(
                                userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()),
                        new com.google.maps.model.LatLng(
                                marker.getPosition().latitude, marker.getPosition().longitude), null, null, 0);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void calculateDirections(com.google.maps.model.LatLng origin, com.google.maps.model.LatLng dest, String originId, String destinationId, int instructionId){
        Log.d(TAG, "calculateStepDirections: CALCULATING STEP DIRECTIONS.");
        Log.d(TAG, "calculate step FMMMMM    " + origin + "    " + dest + "    " + originId + "    " + destinationId);
        com.google.maps.model.LatLng destination;

        //initialize directions
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
        directions.mode(TravelMode.BICYCLING);

        if(origin != null)
            directions.origin(new com.google.maps.model.LatLng(origin.lat, origin.lng));
        else
            directions.originPlaceId(originId);

        if(dest != null) {
            destination = new com.google.maps.model.LatLng(dest.lat, dest.lng);

            directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onResult(DirectionsResult result) {
                    addPolylinesToMap(result);
                    for(DirectionsRoute route : result.routes) {
                        for (DirectionsLeg leg : route.legs) {
                            Log.d(TAG, "calculateStepDirections: legs: " + leg + "\n");
                            distanceProgress += leg.distance.inMeters;
                            timeProgressFin = (int) leg.duration.inSeconds;
                            arrivalTimes.add(leg.arrivalTime);
                            ArrayList<String> instructionsStep = new ArrayList<>();
                            instructionsStep.add("Cycle");
                            instructionsStep.add("Cycle from " + leg.startAddress + " to " + leg.endAddress);
                            instructionsStep.add("Step duration: " + leg.duration.humanReadable);
                            instructionsStep.add("Step distance: " + leg.distance.humanReadable);
                            for (DirectionsStep step : leg.steps) {
                                String str = step.htmlInstructions.replaceAll("\\<.*?\\>", "");
                                if(str.contains("Walk your bicycle"))
                                    str = str.replace("Walk your bicycle", ". Walk your bicycle");
                                instructionsStep.add(str);
                            }
                            instructionsStep.add("Arrive at: " + LocalTime.now().minusHours(2).plusMinutes(leg.duration.inSeconds/60));
                            instructions.put(instructionId, instructionsStep);

                        }
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    Log.e(TAG, "calculateDirections: Failed to get step directions: " + e.getMessage() );
                }
            });
        } else {
            directions.destinationPlaceId(destinationId).setCallback(new PendingResult.Callback<DirectionsResult>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onResult(DirectionsResult result) {
                    addPolylinesToMap(result);
                    for(DirectionsRoute route : result.routes) {
                        for (DirectionsLeg leg : route.legs) {
                            distanceProgress += leg.distance.inMeters;
                            Log.d(TAG, "calculateStepDirections: legs: " + leg + "\n");
                            ArrayList<String> instructionsStep = new ArrayList<>();
                            instructionsStep.add("Cycle");
                            instructionsStep.add("Cycle from " + leg.startAddress + " to " + leg.endAddress);
                            instructionsStep.add("Step duration: " + leg.duration.humanReadable);
                            instructionsStep.add("Step distance: " + leg.distance.humanReadable);
                            for (DirectionsStep step : leg.steps) {
                                String str = step.htmlInstructions.replaceAll("\\<.*?\\>", "");
                                if(str.contains("Walk your bicycle"))
                                    str = str.replace("Walk your bicycle", ". Walk your bicycle");
                                if(str.contains("Destination will be"))
                                    str = str.replace("Destination will be", ". Destination will be");
                                instructionsStep.add(str);
                            }
                            instructionsStep.add("Arrive at: " + LocalTime.now().minusHours(2).plusMinutes(leg.duration.inSeconds/60));
                            instructions.put(instructionId, instructionsStep);
                        }
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );
                }
            });
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        for (PolylineData polylineData : mPolyLinesData) {
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if (polyline.getId().equals(polylineData.getPolyline().getId())) {
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                polylineData.getPolyline().setZIndex(1);

            } else {
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                double duration = 999999;
                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = map.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    List<PatternItem> pattern = Arrays.asList(
                            new Dot(), new Gap(20), new Dash(30), new Gap(20));
                    polyline.setPattern(pattern);
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGrey));
                    polyline.setClickable(true);
                    mPolyLinesData.add(new PolylineData(polyline));

                    double tempDuration = route.legs[0].duration.inSeconds;
                    if(tempDuration < duration){
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }

                    LatLng location = new LatLng(route.legs[0].startLocation.lat, route.legs[0].startLocation.lng);

                    Marker marker = map.addMarker(new MarkerOptions()
                            .title(route.legs[0].startAddress)
                            .position(location)
                            .snippet("Cycle from here"));

                    markers.add(marker);
                }
            }
        });
    }

    private void addStepPolylinesToMap(final DirectionsStep step){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(step.polyline.getEncodedPath());
                List<LatLng> newDecodedPath = new ArrayList<>();

                // This loops through all the LatLng coordinates of ONE polyline.
                for(com.google.maps.model.LatLng latLng: decodedPath){

                    newDecodedPath.add(new LatLng(
                            latLng.lat,
                            latLng.lng
                    ));
                }
                Polyline polyline = map.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGrey));
                polyline.setClickable(true);
                mPolyLinesData.add(new PolylineData(polyline));

                LatLng location = new LatLng(step.transitDetails.departureStop.location.lat, step.transitDetails.departureStop.location.lng);

                Marker marker = map.addMarker(new MarkerOptions()
                        .title(step.transitDetails.departureStop.name)
                        .position(location)
                        .snippet("Board " + step.transitDetails.line.name + " here"));

                markers.add(marker);
            }
        });
    }

    public void findPlace(String placeId) {
        // Specify the fields to return.
        final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Construct a request object, passing the place ID and fields array.
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getLatLng() + ", " + place.getAddress());

            //get place dets
            String markerSnippet = place.getAddress();
            LatLng location = place.getLatLng();
            // Add a marker for the selected place, with an info window
            // showing information about that place.
            Marker marker = map.addMarker(new MarkerOptions()
                    .title(place.getName())
                    .position(location)
                    .snippet(markerSnippet));

            //removePrevTrip();

            directBtn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    if(directBtn.getText().equals("Calculate route")) {
                        assert marker != null;
                        calculateTotalDirections(marker);
                        map.setPadding(0, 0, 0, 250);
                        directBtn.setText(string.show_trip_dets);
                    } else if(directBtn.getText().equals("Show trip details")) {
                        LocalTime arrivalTime;
                        if(timeProgress != null)
                            arrivalTime = timeProgress.plusSeconds(timeProgressFin).toLocalTime();
                        else
                            arrivalTime = LocalTime.now().minusHours(2).plusSeconds(timeProgressFin);
                        int totalTime = (int) LocalTime.now().minusHours(2).until(arrivalTime, ChronoUnit.SECONDS);

                        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("HH:mm");

                        Intent intent = new Intent(getApplicationContext(), NavigationInstructions.class);
                        intent.putExtra("instructions", instructions);
                        if(departureTime.size() > 0)
                            intent.putExtra("departureTime", departureTime.get(0).toLocalTime().toString());
                        else
                            intent.putExtra("departureTime", "");
                        intent.putExtra("totalTime", totalTime);
                        intent.putExtra("distanceProgress", distanceProgress);
                        intent.putExtra("arrivalTime", arrivalTime.format(myFormatObj));
                        startActivity(intent);

                        Log.d(TAG, "ARRAY " + placeAddresses);
                        directBtn.setVisibility(View.INVISIBLE);
                        directBtn.setText(string.directions);
                    }
                    mSelectedMarker = marker;

                    cardView.setVisibility(View.GONE);
                }
            });

            // Position the map's camera at the location of the marker.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                // TODO: Handle error with given status code.
            }
        });
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        map.setOnPolylineClickListener(this);

        map.setOnInfoWindowClickListener(this);

        map.setOnMapLongClickListener(this);

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        this.map.setPadding(0, 200, 0, 150);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Marker marker = map.addMarker(new MarkerOptions()
                .position(latLng));
        markers.add(marker);

        directBtn.setVisibility(View.VISIBLE);
        directBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (directBtn.getText().equals("Calculate route")) {
                    assert marker != null;
                    calculateTotalDirections(marker);
                    map.setPadding(0, 0, 0, 250);
                    directBtn.setText(string.show_trip_dets);
                }
            }
        });
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation");
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted && gpsEnabled) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                GeoPoint geoPoint = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                userLocation.setGeo_point(geoPoint);
                                userLocation.setTimestamp(null);
                                saveUserLocation();
                                startLocationService();
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    //check everytime the app is started if gps is enabled and permissions are granted
    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(!locationPermissionGranted){
                Log.d(TAG, "on resume get loc per");
                getLocationPermission();
            } else {
                Log.d(TAG, "on resume get user dets");
                getUserDetails();
                updateLocationUI();
            }
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //check google services and gps
    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    //prompt the user to enable GPS
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(!locationPermissionGranted){
                    getLocationPermission();
                } else {
                    getUserDetails();
                }
            }
        }
    }

    //check if gps is enabled
    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            gpsEnabled = false;
            return false;
        }
        gpsEnabled = true;
        return true;
    }

    //check if google services work on the device
    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, ERROR_DIALOG_REQUEST);
            assert dialog != null;
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    //Handles the result of the request for location permissions.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                MapsActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(" com.example.justintimecycling.testservices.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (map == null) {
            return;
        }

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressLint("MissingPermission") Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener (new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();

                        // Set the count, handling cases where less than 5 entries are returned.
                        int count;
                        if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                            count = likelyPlaces.getPlaceLikelihoods().size();
                        } else {
                            count = M_MAX_ENTRIES;
                        }

                        int i = 0;
                        likelyPlaceNames = new String[count];
                        likelyPlaceAddresses = new String[count];
                        likelyPlaceAttributions = new List[count];
                        likelyPlaceLatLngs = new LatLng[count];

                        for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                            // Build a list of likely places to show the user.
                            likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                            likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                            likelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                    .getAttributions();
                            likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                            i++;
                            if (i > (count - 1)) {
                                break;
                            }
                        }

                        // Show a dialog offering the user the list of likely places, and add a
                        // marker at the selected place.
                        MapsActivity.this.openPlacesDialog();
                    }
                    else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            map.addMarker(new MarkerOptions()
                    .title(getString(string.default_info_title))
                    .position(defaultLocation)
                    .snippet(getString(string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = likelyPlaceLatLngs[which];
                String markerSnippet = likelyPlaceAddresses[which];
                if (likelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + likelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                Marker marker = map.addMarker(new MarkerOptions()
                        .title(likelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                markers.add(marker);

                // Position the map's camera at the location of the marker.
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(string.pick_place)
                .setItems(likelyPlaceNames, listener)
                .show();
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        }
        return true;
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getUserDetails() {
        if (userLocation == null) {
            userLocation = new UserLocation();
            DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                    .document(fAuth.getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: successfully set the user client.");
                        User user = task.getResult().toObject(User.class);
                        userLocation.setUser(user);
                        ((UserClient)getApplicationContext()).setUser(user);
                        getDeviceLocation();
                    }
                }
            });
        } else {
            getDeviceLocation();
        }
    }

    private void saveUserLocation(){
        Log.d(TAG, "saveUserLocation: \ninserted user location into database.");
        if(userLocation != null){
            DocumentReference locationRef = mDb
                    .collection(getString(R.string.collection_user_locations))
                    .document(fAuth.getUid());

            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                                "\n latitude: " + userLocation.getGeo_point().getLatitude() +
                                "\n longitude: " + userLocation.getGeo_point().getLongitude());
                    }
                }
            });
        }
    }

    public void bottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(id.nav_map);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case id.nav_chat:
                    startActivity(new Intent(getApplicationContext()
                            , MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case id.nav_map:
                    return true;
            }
            return false;
        });
    }

    private void removePrevTrip() {
        if(mPolyLinesData.size() > 0) {
            for(PolylineData polylineData : mPolyLinesData) {
                polylineData.getPolyline().remove();
            }
            mPolyLinesData.clear();
            mPolyLinesData = new ArrayList<>();
        }

        if(markers.size() > 0)
            for(Marker marker : markers)
                marker.remove();

        if(mSelectedMarker != null)
            mSelectedMarker.remove();

        markers.clear();
        routes.clear();
        placeIds.clear();
        placeAddresses.clear();
        markers = new ArrayList<>();
        routes = new ArrayList<>();
        placeAddresses = new ArrayList<>();
        placeIds = new ArrayList<>();
        departureTime.clear();
        departureTime = new ArrayList<>();

        directBtn.setVisibility(View.INVISIBLE);
        directBtn.setText(string.directions);
        map.setPadding(0, 200, 0, 150);
    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (map == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    @Override
    public void onBackPressed() {
        if (doubleBackExit) {
            super.onBackPressed();
            return;
        } else {
            removePrevTrip();
            cardView.setVisibility(View.VISIBLE);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        }

        this.doubleBackExit = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackExit = false, 2000);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Navigate to " + marker.getTitle() + " ?")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, id) -> {
                    //notification
                    if(departureTime.size() > 0) {
                        for(int i=0; i<departureTime.size(); i++) {
                            if(LocalTime.now().minusHours(2).until(departureTime.get(i), ChronoUnit.MINUTES) > 10) {
                                Intent notificationIntent1 = new Intent(MapsActivity.this, ReminderBroadcast1.class);
                                PendingIntent pendingIntent1 = PendingIntent.getBroadcast(MapsActivity.this, 0, notificationIntent1, 0);
                                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                long timeAtButtonClick = System.currentTimeMillis();
                                long delay = (int) LocalTime.now().minusHours(2).until(departureTime.get(0), ChronoUnit.MILLIS) - 600000;
                                alarmManager.set(AlarmManager.RTC_WAKEUP, timeAtButtonClick + 30000, pendingIntent1);
                            }
                            if(LocalTime.now().minusHours(2).until(departureTime.get(i), ChronoUnit.MINUTES) > 5) {
                                Intent notificationIntent5 = new Intent(MapsActivity.this, ReminderBroadcast5.class);
                                PendingIntent pendingIntent5 = PendingIntent.getBroadcast(MapsActivity.this, 0, notificationIntent5, 0);
                                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                long timeAtButtonClick = System.currentTimeMillis();
                                long delay = (int) LocalTime.now().minusHours(2).until(departureTime.get(0), ChronoUnit.MILLIS) - 600000;
                                alarmManager.set(AlarmManager.RTC_WAKEUP, timeAtButtonClick + 20000, pendingIntent5);
                            }
                            if(LocalTime.now().minusHours(2).until(departureTime.get(i), ChronoUnit.MINUTES) > 2) {
                                Intent notificationIntent2 = new Intent(MapsActivity.this, ReminderBroadcast2.class);
                                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(MapsActivity.this, 0, notificationIntent2, 0);
                                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                long timeAtButtonClick = System.currentTimeMillis();
                                long delay = (int) LocalTime.now().minusHours(2).until(departureTime.get(0), ChronoUnit.MILLIS) - 600000;
                                alarmManager.set(AlarmManager.RTC_WAKEUP, timeAtButtonClick + 10000, pendingIntent2);
                            }
                        }
                    }

                    //navigation
                    Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                    intent.putExtra("originLat", userLocation.getGeo_point().getLatitude());
                    intent.putExtra("originLng", userLocation.getGeo_point().getLongitude());
                    intent.putExtra("destinationLat", marker.getPosition().latitude);
                    intent.putExtra("destinationLng", marker.getPosition().longitude);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "JustInTimeCyclingChannel";
            String description = "App train departure reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("notifyUser", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

