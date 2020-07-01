/*
 * Copyright (c) 2011-2020 HERE Global B.V. and its affiliate(s).
 * All rights reserved.
 * The use of this software is conditional upon having a separate agreement
 * with a HERE company for the use or utilization of this software. In the
 * absence of such agreement, the use of the software is not allowed.
 */
package com.here.hellomap;

//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.here.android.mpa.common.CopyrightLogoPosition;
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.Router;
import com.here.android.mpa.routing.RoutingError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class BasicMapActivity extends FragmentActivity implements View.OnClickListener {
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Map map = null;
    private AndroidXMapFragment mapFragment = null;
    private LocationManager locationManager;
    private Dialog dialog;
    MapRoute mapRoute;
    private Fragment fragment = null;
    private LinearLayout llInfo;
    private TextView txtInfoId;
    private TextView txtInfoName;
    private TextView txtInfoBloodGroup;
    private TextView txtInfodateOfBirth;
    private MapMarker customMarker1;
    private MapMarker customMarker2;
    private boolean oriantationVertical = true;
    private Button btnZoomPositive;
    private Button btnZoomNegative;
    private Button btnMylocation;
    private Button btnLayers;
    private Button btnRoute;
    private ScrollView llLayout;
    private LinearLayout llMapNormal;
    private LinearLayout llMapTerrain;
    private LinearLayout llMapSatellite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        llInfo = findViewById(R.id.fragment_container);
        llLayout = findViewById(R.id.ll_layer);
        llMapNormal = findViewById(R.id.ll_map_normal);
        llMapTerrain = findViewById(R.id.ll_terrain);
        llMapSatellite = findViewById(R.id.ll_map_satellite);
        llMapNormal.setOnClickListener(this);
        llMapTerrain.setOnClickListener(this);
        llMapSatellite.setOnClickListener(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkPermissions();
        initCreateRouteButton();
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (120 * scale + 0.5f);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, pixels);
            layoutParams.gravity = Gravity.BOTTOM;
            llInfo.setLayoutParams(layoutParams);
        } else {
            final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (240 * scale + 0.5f);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(pixels, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.END;
            llInfo.setLayoutParams(layoutParams);
        }
    }

    private AndroidXMapFragment getMapFragment() {
        return (AndroidXMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);
    }

    private void addMarkerMyLocation(Location lct) {
        final BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_location);
        Bitmap b = bitmapdraw.getBitmap();
        int height = 140;
        int width = 140;
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        final Image image = new Image();
        image.setBitmap(smallMarker);
        final MapMarker customMarker = new MapMarker(new GeoCoordinate(lct.getLatitude(), lct.getLongitude(), 0.0), image);
        customMarker.setDescription("Şu an ki konumunuz");
        map.addMapObject(customMarker);
    }

    @SuppressWarnings("deprecation")
    private void initialize(final Location lct) {
        // Search for the map fragment to finish setup by calling init().
        mapFragment = getMapFragment();

        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(
                    final OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();
                    // Set the map center to the Vancouver region (no animation)
                    map.setCenter(new GeoCoordinate(lct.getLatitude(), lct.getLongitude(), 0.0),
                            Map.Animation.NONE);

                    final BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_location);
                    addMarkerMyLocation(lct);
                    // Set the zoom level to the average between min and max
                    map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);
                    btnZoomPositive = findViewById(R.id.btn_zoomPositive);
                    btnZoomNegative = findViewById(R.id.btn_zoomNegative);
                    mapFragment.setCopyrightLogoPosition(CopyrightLogoPosition.BOTTOM_LEFT);

                 /*   CoreRouter router = new CoreRouter();
                    RoutePlan routePlan = new RoutePlan();
                    routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(40.8747, 29.1294)));
                    routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(41.8747, 30.1294)));
                    RouteOptions routeOptions = new RouteOptions();
                    routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
                    routeOptions.setRouteType(RouteOptions.Type.FASTEST);

                    routePlan.setRouteOptions(routeOptions);
*/
                       /* map.addTransformListener(new Map.OnTransformListener() {
                            @Override
                            public void onMapTransformStart() {

                            }

                            @Override
                            public void onMapTransformEnd(MapState mapState) {

                            }
                        });*/

                    btnZoomPositive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            map.setZoomLevel(map.getZoomLevel() * 1.06);
                        }
                    });
                    btnZoomNegative.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            map.setZoomLevel(map.getZoomLevel() / 1.06);
                        }
                    });
                    btnMylocation = findViewById(R.id.btn_mylocation);
                    btnMylocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            map.removeAllMapObjects();
                            addMarkerMyLocation(lct);
                            map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 1.7);
                            map.setCenter(new GeoCoordinate(lct.getLatitude(), lct.getLongitude(), 0.0), Map.Animation.NONE);
                        }
                    });

                    btnLayers = findViewById(R.id.btn_layer);
                    btnLayers.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            List<String> schemes = map.getMapSchemes();
                            showDialog(schemes);
                        }
                    });
                    MapGesture.OnGestureListener onGestureListener = new MapGesture.OnGestureListener() {

                        @Override
                        public void onPanStart() {

                        }

                        @Override
                        public void onPanEnd() {

                        }

                        @Override
                        public void onMultiFingerManipulationStart() {

                        }

                        @Override
                        public void onMultiFingerManipulationEnd() {

                        }

                        @Override
                        public boolean onMapObjectsSelected(@NonNull List<ViewObject> list) {

                            for (ViewObject viewObject : list) {
                                if (viewObject.getBaseType() == ViewObject.Type.USER_OBJECT) {
                                    final MapObject mapObject = (MapObject) viewObject;
                                    if (mapObject.getType() == MapObject.Type.MARKER) {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                MapMarker window_marker = ((MapMarker) mapObject);
                                                final Bitmap b = bitmapdraw.getBitmap();
                                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 160, 160, false);
                                                final Image image = new Image();
                                                image.setBitmap(smallMarker);
                                                window_marker.setIcon(image);
                                                Animation slideUp = AnimationUtils.loadAnimation(BasicMapActivity.this, R.anim.slide_up);


                                                if (window_marker.getDescription().equals("Şu an ki konumunuz")) {
                                                    Toast.makeText(getApplicationContext(), "Şu an ki konumunuz", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    if (llInfo.getVisibility() == View.INVISIBLE) {
                                                        ButtonVisibility(View.INVISIBLE);
                                                        llInfo.setVisibility(View.VISIBLE);
                                                        llInfo.startAnimation(slideUp);
                                                    }
                                                    txtInfoId = findViewById(R.id.txtId);
                                                    txtInfoName = findViewById(R.id.txtName);
                                                    txtInfoBloodGroup = findViewById(R.id.txt_bloodGroup);
                                                    txtInfodateOfBirth = findViewById(R.id.txt_dateOfBirth);
                                                    String[] markerDescriptions = window_marker.getDescription().split("%");
                                                    String id = markerDescriptions[0];
                                                    String name = markerDescriptions[1];
                                                    String bloodGroup = markerDescriptions[2];
                                                    String dateOfBirth = markerDescriptions[3];

                                                    txtInfoId.setText(id);
                                                    txtInfoName.setText(name);
                                                    txtInfoBloodGroup.setText(bloodGroup);
                                                    txtInfodateOfBirth.setText(dateOfBirth);
                                                }

                                            }
                                        });
                                        return true;
                                    }
                                }
                            }
                            return true;
                        }

                        @Override
                        public boolean onTapEvent(@NonNull PointF pointF) {
                            final Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                            final Image image = new Image();
                            image.setBitmap(smallMarker);
                            final Animation slideDown = AnimationUtils.loadAnimation(BasicMapActivity.this, R.anim.slide_down);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (llInfo.getVisibility() == View.VISIBLE) {
                                        ButtonVisibility(View.VISIBLE);
                                        llInfo.setVisibility(View.INVISIBLE);
                                        llInfo.startAnimation(slideDown);
                                    }
                                    if (llLayout.getVisibility() == View.VISIBLE) {
                                        btnLayers.setVisibility(View.VISIBLE);
                                        llLayout.setVisibility(View.INVISIBLE);
                                        Animation slideRight = AnimationUtils.loadAnimation(BasicMapActivity.this, R.anim.slide_down);
                                        llLayout.setAnimation(slideRight);
                                    }

                                }
                            });
                            if (customMarker1 != null) {
                                customMarker1.setIcon(image);
                                customMarker2.setIcon(image);
                            }
                            return false;
                        }

                        @Override
                        public boolean onDoubleTapEvent(@NonNull PointF pointF) {
                            return false;
                        }

                        @Override
                        public void onPinchLocked() {

                        }

                        @Override
                        public boolean onPinchZoomEvent(float v, @NonNull PointF pointF) {
                            return false;
                        }

                        @Override
                        public void onRotateLocked() {

                        }

                        @Override
                        public boolean onRotateEvent(float v) {
                            return false;
                        }

                        @Override
                        public boolean onTiltEvent(float v) {
                            return false;
                        }

                        @Override
                        public boolean onLongPressEvent(@NonNull PointF pointF) {
                            return false;
                        }

                        @Override
                        public void onLongPressRelease() {

                        }

                        @Override
                        public boolean onTwoFingerTapEvent(@NonNull PointF pointF) {
                            return false;
                        }
                    };
                    Objects.requireNonNull(mapFragment.getMapGesture()).addOnGestureListener(onGestureListener, 0, true);
                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(BasicMapActivity.this).setMessage(
                                    "Error : " + error.name() + "\n\n" + error.getDetails())
                                    .setTitle(R.string.engine_init_error)
                                    .setNegativeButton(android.R.string.cancel,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) {
                                                    finishAffinity();
                                                }
                                            }).create().show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void showDialog(List<String> layerList) {
        llLayout.setVisibility(View.VISIBLE);
        btnLayers.setVisibility(View.INVISIBLE);
        Animation slideRight = AnimationUtils.loadAnimation(BasicMapActivity.this, R.anim.slide_right);
        llLayout.setAnimation(slideRight);
       /* dialog = new Dialog(BasicMapActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog_layers);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //RecyclerView rvLayers = dialog.findViewById(R.id.rv_layers);
        ArrayList<Layer> newData = new ArrayList<>();
        for (int i = 0; i < layerList.size(); i++) {
            newData.add(new Layer(i, layerList.get(i)));
          *//*  if(i==4){
                newData.add(new Layer(i, "Uydu Görüntüsü"));
            }*//*
        }
        *//*LayersAdapter adapter = new LayersAdapter(newData, getApplicationContext());
        rvLayers.setAdapter(adapter);
        adapter.setItemListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvLayers.setLayoutManager(linearLayoutManager);*//*
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();*/
    }

    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location lct = getLastKnownLocation();
                // all permissions were granted
                initialize(lct);
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Location lct = getLastKnownLocation();
        // all permissions were granted
        initialize(lct);
    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Location location = new Location("gps");
                return location;
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private void initCreateRouteButton() {
        btnRoute = findViewById(R.id.btn_route);

        btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Clear map if previous results are still on map,otherwise proceed to creating
                 * route
                 */
                if (map != null && mapRoute != null) {
                    map.removeMapObject(mapRoute);
                    mapRoute = null;
                } else {
                    /*
                     * The route calculation requires local map data.Unless there is pre-downloaded
                     * map data on device by utilizing MapLoader APIs, it's not recommended to
                     * trigger the route calculation immediately after the MapEngine is
                     * initialized.The INSUFFICIENT_MAP_DATA error code may be returned by
                     * CoreRouter in this case.
                     *
                     */
                    //createRoute();
                    map.removeAllMapObjects();

                    LoadRoute();
                    LoadRoute2();
                }
            }
        });

    }

    private void LoadRoute() {
        final ArrayList<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(40.792921, 39.581863));
        coordinates.add(new Coordinate(40.792596, 39.581777));
        coordinates.add(new Coordinate(40.792613, 39.581670));
        coordinates.add(new Coordinate(40.792353, 39.581584));
        coordinates.add(new Coordinate(40.792174, 39.581476));
        coordinates.add(new Coordinate(40.791930, 39.581283));

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_location);
        final Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
        final Image image = new Image();
        image.setBitmap(smallMarker);

        final Handler handler = new Handler();
        map.setCenter(new GeoCoordinate(coordinates.get(0).getLatitude(), coordinates.get(0).getLongitude(), 0.0),
                Map.Animation.NONE);
        customMarker1 = new MapMarker(new GeoCoordinate(coordinates.get(0).getLatitude(), coordinates.get(0).getLongitude(), 0.0), image);
        customMarker1.setDescription("345761%İlkcan Yılmaz%A Rh+%23/07/1996");

        map.addMapObject(customMarker1);
        TimerTask doAsynchronousTask = new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (i < coordinates.size()) {
                            customMarker1.setCoordinate(new GeoCoordinate(coordinates.get(i).getLatitude(), coordinates.get(i).getLongitude(), 0.0));
                            i++;
                        }
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(doAsynchronousTask, 10, 500);

    }

    private void LoadRoute2() {
        final ArrayList<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(40.772921, 39.571863));
        coordinates.add(new Coordinate(40.772596, 39.571777));
        coordinates.add(new Coordinate(40.772613, 39.571670));
        coordinates.add(new Coordinate(40.772353, 39.571584));
        coordinates.add(new Coordinate(40.772174, 39.571476));
        coordinates.add(new Coordinate(40.771930, 39.571283));

        final BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_location);
        final Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
        final Image image = new Image();
        image.setBitmap(smallMarker);

        final Handler handler = new Handler();
        map.setCenter(new GeoCoordinate(coordinates.get(0).getLatitude(), coordinates.get(0).getLongitude(), 0.0),
                Map.Animation.NONE);
        customMarker2 = new MapMarker(new GeoCoordinate(coordinates.get(0).getLatitude(), coordinates.get(0).getLongitude(), 0.0), image);
        customMarker2.setDescription("345761%Uğur Büyükyılmaz%AB Rh+%23/07/1996");

        map.addMapObject(customMarker2);
        TimerTask doAsynchronousTask = new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (i < coordinates.size()) {
                            customMarker2.setCoordinate(new GeoCoordinate(coordinates.get(i).getLatitude(), coordinates.get(i).getLongitude(), 0.0));
                            i++;
                        }
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(doAsynchronousTask, 10, 500);


    }

    private void ButtonVisibility(int visibility) {
        btnLayers.setVisibility(visibility);
        btnMylocation.setVisibility(visibility);
        btnRoute.setVisibility(visibility);
        btnZoomPositive.setVisibility(visibility);
        btnZoomNegative.setVisibility(visibility);
    }

    public Bitmap invert(Bitmap src) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                // get color on each channel
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // set new pixel color to output image
                bmOut.setPixel(x, y, Color.argb(A, 100, 100, 100));
            }
        }

        // return final image
        return bmOut;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int newOrientation = newConfig.orientation;

        oriantationVertical = newOrientation != Configuration.ORIENTATION_LANDSCAPE;
        if (oriantationVertical) {
            final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (120 * scale + 0.5f);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, pixels);
            layoutParams.gravity = Gravity.BOTTOM;
            llInfo.setLayoutParams(layoutParams);
        } else {
            final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (240 * scale + 0.5f);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(pixels, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.END;
            llInfo.setLayoutParams(layoutParams);
        }
    }

    private void createRoute() {
        /* Initialize a CoreRouter */
        CoreRouter coreRouter = new CoreRouter();

        /* Initialize a RoutePlan */
        RoutePlan routePlan = new RoutePlan();

        /*
         * Initialize a RouteOption. HERE Mobile SDK allow users to define their own parameters for the
         * route calculation,including transport modes,route types and route restrictions etc.Please
         * refer to API doc for full list of APIs
         */
        RouteOptions routeOptions = new RouteOptions();
        /* Other transport modes are also available e.g Pedestrian */
        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
        /* Disable highway in this route. */
        routeOptions.setHighwaysAllowed(false);
        /* Calculate the shortest route available. */
        routeOptions.setRouteType(RouteOptions.Type.SHORTEST);
        /* Calculate 1 route. */
        routeOptions.setRouteCount(1);
        /* Finally set the route option */
        routePlan.setRouteOptions(routeOptions);

        /* Define waypoints for the route */
        /* START: 4350 Still Creek Dr */
        RouteWaypoint startPoint = new RouteWaypoint(new GeoCoordinate(41.102697, 41.002697));
        /* END: Langley BC */
        RouteWaypoint destination = new RouteWaypoint(new GeoCoordinate(41.002697, 41.002697));

        /* Add both waypoints to the route plan */
        routePlan.addWaypoint(startPoint);
        routePlan.addWaypoint(destination);

        /* Trigger the route calculation,results will be called back via the listener */
        coreRouter.calculateRoute(routePlan,
                new Router.Listener<List<RouteResult>, RoutingError>() {
                    @Override
                    public void onProgress(int i) {
                        /* The calculation progress can be retrieved in this callback. */
                    }

                    @Override
                    public void onCalculateRouteFinished(List<RouteResult> routeResults,
                                                         RoutingError routingError) {
                        /* Calculation is done. Let's handle the result */
                        if (routingError == RoutingError.NONE) {
                            if (routeResults.get(0).getRoute() != null) {
                                /* Create a MapRoute so that it can be placed on the map */
                                mapRoute = new MapRoute(routeResults.get(0).getRoute());

                                /* Show the maneuver number on top of the route */
                                mapRoute.setManeuverNumberVisible(true);

                                /* Add the MapRoute to the map */
                                map.addMapObject(mapRoute);

                                /*
                                 * We may also want to make sure the map view is orientated properly
                                 * so the entire route can be easily seen.
                                 */
                                GeoBoundingBox gbb = routeResults.get(0).getRoute()
                                        .getBoundingBox();
                                map.zoomTo(gbb, Map.Animation.NONE,
                                        Map.MOVE_PRESERVE_ORIENTATION);
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Error:route results returned is not valid",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Error:route calculation returned error code: " + routingError,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_map_normal:
                map.setMapScheme(Map.Scheme.NORMAL_DAY);
                llLayout.setVisibility(View.INVISIBLE);
                btnLayers.setVisibility(View.VISIBLE);
                break;
            case R.id.ll_terrain:
                map.setMapScheme(Map.Scheme.TERRAIN_DAY);
                llLayout.setVisibility(View.INVISIBLE);
                btnLayers.setVisibility(View.VISIBLE);
                break;
            case R.id.ll_map_satellite:
                map.setMapScheme(Map.Scheme.SATELLITE_DAY);
                llLayout.setVisibility(View.INVISIBLE);
                btnLayers.setVisibility(View.VISIBLE);
                break;
        }
    }
}
