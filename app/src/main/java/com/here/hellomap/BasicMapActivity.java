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
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.here.android.mpa.common.CopyrightLogoPosition;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BasicMapActivity extends FragmentActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static boolean[] checkSelected;

    private Map map = null;
    private AndroidXMapFragment mapFragment = null;
    private LocationManager locationManager;
    private Dialog dialog;
    private MapRoute mapRoute;
    private Fragment fragment = null;
    private AutoCompleteTextView txtSearch;

    private TextView txtInfoId;
    private TextView txtInfoName;
    private TextView txtInfoBloodGroup;
    private TextView txtInfodateOfBirth;

    private Button btnZoomPositive;
    private Button btnZoomNegative;
    private Button btnMylocation;
    private Button btnLayers;
    private Button btnRoute;
    private Button btnSearch;
    private Button btnRightpanel;
    private Button btnPerson;
    private Button btnEquipment;
    private Button btnSignal;
    private Button btnReader;
    private Button btnCluster;
    private Button btnDepartments;
    private Button btnTag;
    private Button btnMaintenance;

    private ScrollView llInfo;
    private ScrollView llLayout;
    private ScrollView llRightpanel;

    private LinearLayout llMapNormal;
    private LinearLayout llMapTerrain;
    private LinearLayout llMapSatellite;
    private LinearLayout llinfoView;

    private ArrayList<MapMarker> markers;
    private ArrayList<UserModal> user;
    private PopupWindow pw;


//    private List<Model> mModelList;
//    private RecyclerView mRecyclerView;
//    private RecyclerView.Adapter mAdapter;

    boolean isPlay = false;
    private boolean expanded;
    private boolean oriantationVertical = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        llInfo = findViewById(R.id.fragment_container);
        llLayout = findViewById(R.id.ll_layer);
        llRightpanel = findViewById(R.id.ll_rightpanel);
        llMapNormal = findViewById(R.id.ll_map_normal);
        llMapTerrain = findViewById(R.id.ll_terrain);
        llMapSatellite = findViewById(R.id.ll_map_satellite);
        llMapNormal.setOnClickListener(this);
        llMapTerrain.setOnClickListener(this);
        llMapSatellite.setOnClickListener(this);
        llinfoView = findViewById(R.id.ll_infoView);
        txtSearch = findViewById(R.id.txt_search);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkPermissions();


        String[] country = {"Ankara", "İstanbul", "Trabzon", "İzmir", "Sakarya",};

        Spinner spin = (Spinner) findViewById(R.id.spinnerregion);
        spin.setOnItemSelectedListener(this);

        ArrayAdapter a = new ArrayAdapter(this, android.R.layout.simple_spinner_item, country);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin.setAdapter(a);




        String[] label = {"test 1", "test 2", "test 3", "test 4",};

        Spinner spin3 = (Spinner)findViewById(R.id.spinnerlabel);
        spin3.setOnItemSelectedListener(this);

        ArrayAdapter b = new ArrayAdapter(BasicMapActivity.this, android.R.layout.simple_spinner_item, label);
        b.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin3.setAdapter(b);






        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (180 * scale + 0.5f);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, pixels);
            layoutParams.gravity = Gravity.BOTTOM;
            llInfo.setLayoutParams(layoutParams);
        } else {
            final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (240 * scale + 0.5f);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(pixels, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.END;
            llInfo.setLayoutParams(layoutParams);
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(pixels, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams2.gravity = Gravity.CENTER;
            llinfoView.setLayoutParams(layoutParams2);
        }

        user = new ArrayList<>();
        user.add(new UserModal(10, "İlkcan Yılmaz", "A Rh+", "23/07/1996", 40.792921, 39.581863));
        user.add(new UserModal(20, "Ugur Büyükyılmaz", "AB Rh+", "05/10/1996", 40.892921, 39.481863));
        user.add(new UserModal(30, "Mehmet Koşar", "A Rh", "20/06/1994", 40.692921, 39.381863));
        user.add(new UserModal(40, "Müdür Mehmet", "0 Rh+", "16/08/1996", 40.592921, 39.281863));
        user.add(new UserModal(50, "Mustafa Arslan", "0 Rh-", "02/01/1985", 40.492921, 39.181863));
        user.add(new UserModal(60, "Ahmet Temur", "AB Rh-", "25/06/1999", 40.392921, 39.681863));
        user.add(new UserModal(70, "Ugur Ugur", "0 Rh+", "11/12/1975", 40.292921, 39.781863));

    }



    private AndroidXMapFragment getMapFragment() {
        return (AndroidXMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);
    }

    private void addMarkerMyLocation(Location lct) {
        final BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_location);
        Bitmap b = bitmapdraw.getBitmap();
        int height = 100;
        int width = 100;
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        final Image image = new Image();
        image.setBitmap(smallMarker);
        final MapMarker customMarker = new MapMarker(new GeoCoordinate(lct.getLatitude(), lct.getLongitude(), 0.0), image);
        customMarker.setDescription("Şu an ki konumunuz");
        map.addMapObject(customMarker);
    }

    @SuppressWarnings("deprecation")
    private void initialize(final Location lct) {
        mapFragment = getMapFragment();

        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(
                    final OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {

                    map = mapFragment.getMap();
                    map.setCenter(new GeoCoordinate(lct.getLatitude(), lct.getLongitude(), 0.0), Map.Animation.NONE);
                    final BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_location);

                    addMarkerMyLocation(lct);

                    map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);

                    btnZoomPositive = findViewById(R.id.btn_zoomPositive);
                    btnZoomNegative = findViewById(R.id.btn_zoomNegative);
                    btnMylocation = findViewById(R.id.btn_mylocation);
                    btnLayers = findViewById(R.id.btn_layer);
                    btnSearch = findViewById(R.id.btn_search);
                    btnRoute = findViewById(R.id.btn_route);
                    btnRightpanel = findViewById(R.id.btn_rightpanel);
                    btnDepartments = findViewById(R.id.btn_department);
                    btnTag = findViewById(R.id.btn_tag);
                    btnMaintenance = findViewById(R.id.btn_maintenancedate);

                    btnPerson = findViewById(R.id.btn_person);
                    btnPerson.setBackgroundResource(R.mipmap.ic_personinactive);

                    btnEquipment = findViewById(R.id.btn_equipment);
                    btnEquipment.setBackgroundResource(R.mipmap.ic_equipmentinactive);

                    btnSignal = findViewById(R.id.btn_signal);
                    btnSignal.setBackgroundResource(R.mipmap.ic_signalinactive);

                    btnReader = findViewById(R.id.btn_reader);
                    btnReader.setBackgroundResource(R.mipmap.ic_readerinactive);

                    btnCluster = findViewById(R.id.btn_cluster);
                    btnCluster.setBackgroundResource(R.mipmap.ic_clusterinactive);


                    mapFragment.setCopyrightLogoPosition(CopyrightLogoPosition.BOTTOM_LEFT);

                    createUserMarker(user);

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
                    btnMylocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            map.removeAllMapObjects();
                            addMarkerMyLocation(lct);
                            map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 1.7);
                            map.setCenter(new GeoCoordinate(lct.getLatitude(), lct.getLongitude(), 0.0), Map.Animation.NONE);
                        }
                    });
                    btnLayers.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            List<String> schemes = map.getMapSchemes();
                            showDialog(schemes);
                        }
                    });
                    btnRoute.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (map != null && mapRoute != null) {
                                map.removeMapObject(mapRoute);
                                mapRoute = null;
                            } else {
                                map.removeAllMapObjects();

                                infoUser(user);

                            }
                        }
                    });
                    btnSearch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            showSearchBar();

                        }
                    });
                    btnRightpanel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            showPanel();

                        }
                    });
                    btnPerson.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isPlay) {
                                v.setBackgroundResource(R.mipmap.ic_personinactive);
                            } else {
                                v.setBackgroundResource(R.mipmap.ic_personactive);
                            }
                            isPlay = !isPlay;
                        }
                    });
                    btnEquipment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isPlay) {
                                v.setBackgroundResource(R.mipmap.ic_equipmentinactive);
                            } else {
                                v.setBackgroundResource(R.mipmap.ic_equipmentactive);
                            }
                            isPlay = !isPlay;
                        }
                    });
                    btnSignal.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isPlay) {
                                v.setBackgroundResource(R.mipmap.ic_signalinactive);
                            } else {
                                v.setBackgroundResource(R.mipmap.ic_signalactive);
                            }
                            isPlay = !isPlay;
                        }
                    });
                    btnReader.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isPlay) {
                                v.setBackgroundResource(R.mipmap.ic_readerinactive);
                            } else {
                                v.setBackgroundResource(R.mipmap.ic_readeractive);
                            }
                            isPlay = !isPlay;
                        }
                    });
                    btnCluster.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isPlay) {
                                v.setBackgroundResource(R.mipmap.ic_clusterinactive);
                            } else {
                                v.setBackgroundResource(R.mipmap.ic_clusteractive);
                            }
                            isPlay = !isPlay;
                        }
                    });
                    btnDepartments.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            showDepartmantsDialog();

                        }
                    });
                    btnTag.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            showTagDialog();

                        }
                    });
                    btnMaintenance.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            showMaintenanceDialog();

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
                                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
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
                                        ButtonVisibility(View.VISIBLE);
                                        llLayout.setVisibility(View.INVISIBLE);
                                        Animation slideRight = AnimationUtils.loadAnimation(BasicMapActivity.this, R.anim.slide_down);
                                        llLayout.setAnimation(slideRight);
                                    }
                                    if (txtSearch.getVisibility() == View.VISIBLE) {
                                        btnSearch.setVisibility(View.VISIBLE);
                                        btnRightpanel.setVisibility(View.VISIBLE);
                                        txtSearch.setVisibility(View.INVISIBLE);
                                        Animation slideRight = AnimationUtils.loadAnimation(BasicMapActivity.this, R.anim.slide_down);
                                        txtSearch.setAnimation(slideRight);

                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(txtSearch.getWindowToken(), 0);
                                    }
                                    if (llRightpanel.getVisibility() == View.VISIBLE) {
                                        ButtonVisibility(View.VISIBLE);
                                        llRightpanel.setVisibility(View.INVISIBLE);
                                        Animation slideRight = AnimationUtils.loadAnimation(BasicMapActivity.this, R.anim.slide_down);
                                        llRightpanel.setAnimation(slideRight);
                                    }
                                }
                            });
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

        final ArrayList<String> items = new ArrayList<String>();
        items.add("Item 1");
        items.add("Item 2");
        items.add("Item 3");
        items.add("Item 4");
        items.add("Item 5");

        checkSelected = new boolean[items.size()];
        //initialize all values of list to 'unselected' initially
        for (int i = 0; i < checkSelected.length; i++) {
            checkSelected[i] = false;
        }

        final TextView tv = (TextView) findViewById(R.id.SelectBox);
        tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (!expanded) {
                    //display all selected values
                    String selected = "";
                    int flag = 0;
                    for (int i = 0; i < items.size(); i++) {
                        if (checkSelected[i] == true) {
                            selected += items.get(i);
                            selected += ", ";
                            flag = 1;
                        }
                    }
                    if (flag == 1)
                        tv.setText(selected);
                    expanded = true;
                } else {
                    //display shortened representation of selected values
                    tv.setText(MainListAdapter.getSelected());
                    expanded = false;
                }
            }
        });

        Button createButton = (Button) findViewById(R.id.create);
        createButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub

                showMainCategoryDialog(items, tv);
            }
        });

        final ArrayList<String> subitems = new ArrayList<String>();
        subitems.add("Item 1");
        subitems.add("Item 2");
        subitems.add("Item 3");
        subitems.add("Item 4");
        subitems.add("Item 5");
        subitems.add("Item 6");
        subitems.add("Item 7");

        checkSelected = new boolean[subitems.size()];
        //initialize all values of list to 'unselected' initially
        for (int i = 0; i < checkSelected.length; i++) {
            checkSelected[i] = false;
        }

        final TextView tvv = findViewById(R.id.SelectBox2);
        tvv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (!expanded) {
                    //display all selected values
                    String selected = "";
                    int flag = 0;
                    for (int i = 0; i < subitems.size(); i++) {
                        if (checkSelected[i] == true) {
                            selected += subitems.get(i);
                            selected += ", ";
                            flag = 1;
                        }
                    }
                    if (flag == 1)
                        tvv.setText(selected);
                    expanded = true;
                } else {
                    //display shortened representation of selected values
                    tvv.setText(SubListAdapter.getSelected());
                    expanded = false;
                }
            }
        });

        Button subcategoryButton = findViewById(R.id.subcategory);
        subcategoryButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                showSubCategoryDialog(subitems, tvv);
            }
        });

    }

    public void createUserMarker(final ArrayList<UserModal> user) {

        BitmapDrawable bitmapdraw2 = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_location);
        final Bitmap b = bitmapdraw2.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
        final Image image = new Image();
        image.setBitmap(smallMarker);

        markers = new ArrayList<>();

        for (int i = 0; i < user.size(); i++) {
            markers.add(new MapMarker(new GeoCoordinate(user.get(i).getLatitude(), user.get(i).getLongitude(), 0.0), image));
            markers.get(i).setDescription(user.get(i).getId() + "%" + user.get(i).getName() + "%" + user.get(i).getBloodGroup() + "%" + user.get(i).getDateOfBirth() + "%" + user.get(i).getLatitude() + "%" + user.get(i).getLongitude());
            map.addMapObject(markers.get(i));
        }

        AutoCompleteAdapter adapter = new AutoCompleteAdapter(BasicMapActivity.this, R.layout.item_autocompletetextview, user);

        txtSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; user.size() > i; i++) {
                    if (user.get(i).getId() == id) {
                        map.setCenter(new GeoCoordinate(user.get(i).getLatitude(), user.get(i).getLongitude(), 0.0), Map.Animation.NONE);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(txtSearch.getWindowToken(), 0);
                    }
                }
            }
        });
        txtSearch.setAdapter(adapter);
    }

    private void infoUser(final ArrayList<UserModal> user) {

        createUserMarker(user);

        map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2.0);
        map.setCenter(new GeoCoordinate(user.get(0).getLatitude(), user.get(0).getLongitude(), 0.0), Map.Animation.NONE);

        ArrayList<String> userName = new ArrayList<>();

        for (int i = 0; i < user.size(); i++) {
            userName.add(user.get(i).getName());
        }
    }

    public void showSearchBar() {
        txtSearch.setVisibility(View.VISIBLE);
        btnSearch.setVisibility(View.INVISIBLE);
        btnRightpanel.setVisibility((View.INVISIBLE));

        Animation slideRight = AnimationUtils.loadAnimation(BasicMapActivity.this, R.anim.slide_right);
        txtSearch.setAnimation(slideRight);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void showMaintenanceDialog() {

        dialog = new Dialog(BasicMapActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog_maintenancedate);

        String[] maintenance = {"Ankara", "İstanbul", "Trabzon", "İzmir",};

        Spinner spin2 = (Spinner)dialog.findViewById(R.id.spinnermaintenance);
        spin2.setOnItemSelectedListener(this);

        ArrayAdapter aa = new ArrayAdapter(BasicMapActivity.this, android.R.layout.simple_spinner_item, maintenance);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin2.setAdapter(aa);


        dialog.show();

    }

    public void showDepartmantsDialog() {

        dialog = new Dialog(BasicMapActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog_departmants);
        dialog.show();

    }

    public void showTagDialog() {

        dialog = new Dialog(BasicMapActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog_tag);
        dialog.show();

    }

    public void showMainCategoryDialog(ArrayList<String> items, TextView tv) {

        dialog = new Dialog(BasicMapActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.pop_up_window);
        dialog.show();

        final ListView list = dialog.findViewById(R.id.dropDownList);
        MainListAdapter adapter = new MainListAdapter(this, items, tv);
        list.setAdapter(adapter);

    }

    public void showSubCategoryDialog(ArrayList<String> subitems, TextView tvv) {

        dialog = new Dialog(BasicMapActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.sub_category);
        dialog.show();

        final ListView list = dialog.findViewById(R.id.subCategoryList);
        SubListAdapter adapter = new SubListAdapter(this, subitems, tvv);
        list.setAdapter(adapter);

    }

    public void showDialog(List<String> layerList) {
        llLayout.setVisibility(View.VISIBLE);
        btnLayers.setVisibility(View.INVISIBLE);
        btnRightpanel.setVisibility(View.INVISIBLE);
        btnPerson.setVisibility(View.INVISIBLE);
        btnEquipment.setVisibility(View.INVISIBLE);
        btnSignal.setVisibility(View.INVISIBLE);
        btnReader.setVisibility(View.INVISIBLE);
        btnCluster.setVisibility(View.INVISIBLE);
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

    public void showPanel() {

        llRightpanel.setVisibility(View.VISIBLE);
        btnLayers.setVisibility(View.INVISIBLE);
        btnPerson.setVisibility(View.INVISIBLE);
        btnEquipment.setVisibility(View.INVISIBLE);
        btnSignal.setVisibility(View.INVISIBLE);
        btnReader.setVisibility(View.INVISIBLE);
        btnCluster.setVisibility(View.INVISIBLE);
        btnRightpanel.setVisibility(View.INVISIBLE);
        Animation slideRight = AnimationUtils.loadAnimation(BasicMapActivity.this, R.anim.slide_right);
        llRightpanel.setAnimation(slideRight);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Location lct = getLastKnownLocation();
        initialize(lct);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int newOrientation = newConfig.orientation;

        oriantationVertical = newOrientation != Configuration.ORIENTATION_LANDSCAPE;

        if (oriantationVertical) {
            final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (180 * scale + 0.5f);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, pixels);
            layoutParams.gravity = Gravity.BOTTOM;
            llInfo.setLayoutParams(layoutParams);
            llinfoView.setLayoutParams(layoutParams);
        } else {
            final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (240 * scale + 0.5f);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(pixels, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.END;
            llInfo.setLayoutParams(layoutParams);
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(pixels, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams2.gravity = Gravity.CENTER;
            llinfoView.setLayoutParams(layoutParams2);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_map_normal:
                map.setMapScheme(Map.Scheme.NORMAL_DAY);
                llLayout.setVisibility(View.INVISIBLE);
                btnLayers.setVisibility(View.VISIBLE);
                btnRightpanel.setVisibility((View.VISIBLE));
                btnPerson.setVisibility((View.VISIBLE));
                btnEquipment.setVisibility((View.VISIBLE));
                btnSignal.setVisibility((View.VISIBLE));
                btnReader.setVisibility((View.VISIBLE));
                btnCluster.setVisibility((View.VISIBLE));
                break;
            case R.id.ll_terrain:
                map.setMapScheme(Map.Scheme.TERRAIN_DAY);
                llLayout.setVisibility(View.INVISIBLE);
                btnLayers.setVisibility(View.VISIBLE);
                btnRightpanel.setVisibility((View.VISIBLE));
                btnPerson.setVisibility((View.VISIBLE));
                btnEquipment.setVisibility((View.VISIBLE));
                btnSignal.setVisibility((View.VISIBLE));
                btnReader.setVisibility((View.VISIBLE));
                btnCluster.setVisibility((View.VISIBLE));
                break;
            case R.id.ll_map_satellite:
                map.setMapScheme(Map.Scheme.SATELLITE_DAY);
                llLayout.setVisibility(View.INVISIBLE);
                btnLayers.setVisibility(View.VISIBLE);
                btnRightpanel.setVisibility((View.VISIBLE));
                btnPerson.setVisibility((View.VISIBLE));
                btnEquipment.setVisibility((View.VISIBLE));
                btnSignal.setVisibility((View.VISIBLE));
                btnReader.setVisibility((View.VISIBLE));
                btnCluster.setVisibility((View.VISIBLE));
                break;
        }
    }

    private void ButtonVisibility(int visibility) {
        btnLayers.setVisibility(visibility);
        btnMylocation.setVisibility(visibility);
        btnRoute.setVisibility(visibility);
        btnZoomPositive.setVisibility(visibility);
        btnZoomNegative.setVisibility(visibility);
        btnSearch.setVisibility(visibility);
        btnRightpanel.setVisibility(visibility);
        btnPerson.setVisibility(visibility);
        btnEquipment.setVisibility(visibility);
        btnSignal.setVisibility(visibility);
        btnReader.setVisibility(visibility);
        btnCluster.setVisibility(visibility);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
