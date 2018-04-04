package com.mars.googlemapsample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mars.googlemapsample.util.CircleImageView;
import com.mars.googlemapsample.util.DirectionsJSONParser;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityMain extends FragmentActivity {

    private GoogleMap mMap;
    private View doctor_marker, user_marker;
    private double latitude = 0;

    private double longitude = 0;

    private TextView txtdistance, txtTime, txtTimeCar, txtTimeWalk;

    private LatLng doctorlocation, userlocation;

    private Activity activity;

    private Context context;

    //defeult mode
    private String modeMap = "driving";

    private LinearLayout lyCar, lyWalk;

    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99,
            PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 98;

    private SupportMapFragment mapFragment;

    @Override
    public void onRequestPermissionsResult ( int requestCode,
                                             String permissions[], int[] grantResults ) {

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    if ( ContextCompat.checkSelfPermission ( this,
                            Manifest.permission.ACCESS_FINE_LOCATION )
                            == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission ( this,
                            Manifest.permission.ACCESS_COARSE_LOCATION )
                            == PackageManager.PERMISSION_GRANTED ) {
                        mapFragment.getMapAsync ( onMapReadyCallback );

                        String url = getDirectionsUrl ( modeMap );
                        loadRoad ( url );
                    }
                } else {

                }
                return;
            }
        }
    }

    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );

        context = this;

        mapFragment = ( SupportMapFragment ) getSupportFragmentManager ( ).findFragmentById ( R.id.map );

        ImageButton btnWalk = findViewById ( R.id.btnWalk );
        ImageButton btnCar = findViewById ( R.id.btnCar );

        lyCar = findViewById ( R.id.lyCar );
        lyWalk = findViewById ( R.id.lyWalk );


        txtTimeCar = findViewById ( R.id.txtTimeCar );
        txtTimeWalk = findViewById ( R.id.txtTimeWalk );


        OnClickListener onClickListenerCar = view -> {
            if ( ActivityCompat.checkSelfPermission ( ActivityMain.this, Manifest.permission.ACCESS_FINE_LOCATION )
                    != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions ( ActivityMain.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION );
                return;
            }

            if ( ActivityCompat.checkSelfPermission ( ActivityMain.this, Manifest.permission.ACCESS_COARSE_LOCATION )
                    != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions ( ActivityMain.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION );
                return;
            }


            if ( !IsGpsEnabled ( ) ) {
                showSettingsAlert ( );
                return;
            } else {
                modeMap = "driving";
                String url = getDirectionsUrl ( modeMap );
                loadRoad ( url );
            }

            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                lyCar.setBackground ( getDrawable ( R.drawable.card_edge_white_map ) );
            } else if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
                lyCar.setBackground ( ContextCompat.getDrawable ( context, R.drawable.card_edge_white_map ) );
            }

            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                lyWalk.setBackground ( getDrawable ( R.drawable.card_edge_blue_map ) );
            } else if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
                lyWalk.setBackground ( ContextCompat.getDrawable ( context, R.drawable.card_edge_blue_map ) );
            }

            txtTimeCar.setTextColor ( Color.parseColor ( "#4fc4c7" ) );
            txtTimeWalk.setTextColor ( Color.parseColor ( "#FFFFFF" ) );

            btnCar.setImageResource ( R.drawable.ic_directions_car_blue );
            btnWalk.setImageResource ( R.drawable.ic_directions_walk_white );

        };

        OnClickListener onClickListenerWalk = view -> {

            if ( ActivityCompat.checkSelfPermission ( ActivityMain.this, Manifest.permission.ACCESS_FINE_LOCATION )
                    != PackageManager.PERMISSION_GRANTED ) {

                ActivityCompat.requestPermissions ( ActivityMain.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION );
                return;
            }

            if ( ActivityCompat.checkSelfPermission ( ActivityMain.this, Manifest.permission.ACCESS_COARSE_LOCATION )
                    != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions ( ActivityMain.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION );
                return;
            }


            if ( !IsGpsEnabled ( ) ) {
                showSettingsAlert ( );
                return;
            } else {
                modeMap = "walking";
                String url = getDirectionsUrl ( modeMap );
                loadRoad ( url );
            }

            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                lyWalk.setBackground ( getDrawable ( R.drawable.card_edge_white_map ) );
            } else if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
                lyWalk.setBackground ( ContextCompat.getDrawable ( context, R.drawable.card_edge_white_map ) );
            }

            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                lyCar.setBackground ( getDrawable ( R.drawable.card_edge_blue_map ) );
            } else if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
                lyCar.setBackground ( ContextCompat.getDrawable ( context, R.drawable.card_edge_blue_map ) );
            }

            txtTimeCar.setTextColor ( Color.parseColor ( "#FFFFFF" ) );
            txtTimeWalk.setTextColor ( Color.parseColor ( "#4fc4c7" ) );

            btnCar.setImageResource ( R.drawable.ic_directions_car_white );
            btnWalk.setImageResource ( R.drawable.ic_directions_walk_blue );


        };

        lyCar.setOnClickListener ( onClickListenerCar );
        btnCar.setOnClickListener ( onClickListenerCar );
        txtTimeCar.setOnClickListener ( onClickListenerCar );

        lyWalk.setOnClickListener ( onClickListenerWalk );
        btnWalk.setOnClickListener ( onClickListenerWalk );
        txtTimeWalk.setOnClickListener ( onClickListenerWalk );

        activity = this;

        latitude = 35.7860105;
        longitude = 51.3819877;

        doctorlocation = new LatLng ( latitude, longitude );

        ImageButton btnback = ( ImageButton ) findViewById ( R.id.btnBack );
        btnback.setOnClickListener ( new OnClickListener ( ) {
            @Override
            public void onClick ( View v ) {
                finish ( );
            }
        } );

        if ( ActivityCompat.checkSelfPermission ( ActivityMain.this, Manifest.permission.ACCESS_FINE_LOCATION )
                != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions ( ActivityMain.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION );
            return;
        }

        if ( ActivityCompat.checkSelfPermission ( ActivityMain.this, Manifest.permission.ACCESS_COARSE_LOCATION )
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions ( ActivityMain.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION );
            return;
        }


        mapFragment.getMapAsync ( onMapReadyCallback );

    }

    public void initDistanceCar ( String url ) {
        JsonObjectRequest request = new JsonObjectRequest (
                url,
                null,
                jsonobject -> {

                    DirectionsJSONParser parser = new DirectionsJSONParser ( );
                    List <List <HashMap <String, String>>> route = parser.parse ( jsonobject );
                    if ( parser.getTime ( ) != null ) {
                        String time = parser.getTime ( ).replace ( "mins", "دقیقه" )
                                            .replace ( "hours", "ساعت" );

                        String distance = parser.getDistance ( ).replace ( "km", "کیلومتر" );
//                        txtdeteil.setText ( time + " - " + distance );

                        txtTimeCar.setText ( time );
                        txtTimeCar.setVisibility ( View.VISIBLE );

                    } else {
//                        btnRefreshedPoint.setVisibility ( View.VISIBLE );
//                        txtdeteil.setText ( " امکان دسترسی به مکان یاب وجود ندارد" );
                    }

                }, error -> {
            //int a = 01;
        } );

        request.setRetryPolicy ( new DefaultRetryPolicy ( 6000, 1, 1 ) );
        RequestManager.getRequestQueue ( ).add ( request );

    }


    public void initDistanceWalk ( String url ) {


        JsonObjectRequest request = new JsonObjectRequest (
                url,
                null,
                jsonobject -> {

                    DirectionsJSONParser parser = new DirectionsJSONParser ( );
                    List <List <HashMap <String, String>>> route = parser.parse ( jsonobject );
                    if ( parser.getTime ( ) != null ) {
                        String time = parser.getTime ( ).replace ( "mins", "دقیقه" )
                                            .replace ( "hours", "ساعت" );
                        String distance = parser.getDistance ( ).replace ( "km", "کیلومتر" );
//                        txtdeteil.setText ( time + " - " + distance );

                        txtTimeWalk.setText ( time );
                        txtTimeWalk.setVisibility ( View.VISIBLE );

                    } else {
//
                    }

                }, error -> {
            //int a = 01;
        } );

        request.setRetryPolicy ( new DefaultRetryPolicy ( 6000, 1, 1 ) );
        RequestManager.getRequestQueue ( ).add ( request );

    }


    public void loadRoad ( String url ) {
        JsonObjectRequest request = new JsonObjectRequest (
                url,
                null,
                jsonobject -> {

                    DirectionsJSONParser parser = new DirectionsJSONParser ( );
                    List <List <HashMap <String, String>>> route = parser.parse ( jsonobject );
                    if ( parser.getTime ( ) != null ) {
                        String time = parser.getTime ( ).replace ( "mins", "دقیقه" )
                                            .replace ( "hours", "ساعت" );
                        String distance = parser.getDistance ( ).replace ( "km", "کیلومتر" );
//                        txtdeteil.setText ( time + " - " + distance );

                        txtdistance.setText ( "فاصله: " + distance );
                        txtTime.setText ( "زمان: " + time );

                    } else {
//
                    }

                    ArrayList points = null;
                    PolylineOptions lineOptions = null;

                    for ( int i = 0; i < route.size ( ); i++ ) {
                        points = new ArrayList ( );
                        lineOptions = new PolylineOptions ( );

                        List <HashMap <String, String>> path = route.get ( i );

                        for ( int j = 0; j < path.size ( ); j++ ) {
                            HashMap <String, String> point = path.get ( j );

                            double lat = Double.parseDouble ( point.get ( "lat" ) );
                            double lng = Double.parseDouble ( point.get ( "lng" ) );
                            LatLng position = new LatLng ( lat, lng );
                            points.add ( position );
                        }

                        mMap.clear ( );
                        mMap.addMarker ( new MarkerOptions ( )
                                .position ( doctorlocation )
                                .title ( "alan turing :)" )
                                .snippet ( "computer scientist" )
                                .icon ( BitmapDescriptorFactory.fromBitmap ( createDrawableFromView ( doctor_marker ) ) ) );

                        mMap.addMarker ( new MarkerOptions ( )
                                .position ( userlocation )
                                .icon ( BitmapDescriptorFactory.fromBitmap ( createDrawableFromView ( user_marker ) ) ) );

                        lineOptions.addAll ( points );
                        lineOptions.width ( 8 );
                        lineOptions.color ( getResources ( ).getColor ( R.color.colorPrimary ) );
                        lineOptions.geodesic ( true );
                    }

                    if ( lineOptions != null )
                        mMap.addPolyline ( lineOptions );
                }, error -> {
        } );

        request.setRetryPolicy ( new DefaultRetryPolicy ( 6000, 1, 1 ) );
        RequestManager.getRequestQueue ( ).add ( request );

    }


    public Bitmap createDrawableFromView ( View view ) {
        view.setDrawingCacheEnabled ( true );

        DisplayMetrics displayMetrics = new DisplayMetrics ( );
//        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        activity.getWindowManager ( ).getDefaultDisplay ( ).getMetrics ( displayMetrics );
        view.setLayoutParams ( new ViewGroup.LayoutParams ( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
        view.measure ( displayMetrics.widthPixels, displayMetrics.heightPixels );
        view.layout ( 0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels );
        view.buildDrawingCache ( );
        Bitmap bitmap = Bitmap.createBitmap ( view.getMeasuredWidth ( ), view.getMeasuredHeight ( ), Bitmap.Config.ARGB_8888 );
        Canvas canvas = new Canvas ( bitmap );
        view.draw ( canvas );
        view.setDrawingCacheEnabled ( false );

        return bitmap;
    }

    private void getUserLocation ( ) {
        double mylatitude = 0;
        double mylongitude = 0;

        Location location = getLastKnownLocation ( );

        if ( location != null ) {
            mylatitude = location.getLatitude ( );
            mylongitude = location.getLongitude ( );
        }

        userlocation = new LatLng ( mylatitude, mylongitude );
    }

    private String getDirectionsUrl ( String modeMap ) {

        if ( userlocation == null )
            getUserLocation ( );

        String str_origin = "origin=" + doctorlocation.latitude + "," + doctorlocation.longitude;
        String str_dest = "destination=" + userlocation.latitude + "," + userlocation.longitude;
        String sensor = "sensor=false";
        String mode = "mode=" + modeMap;
        String language = "language=fa";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + language;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;

    }

    OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback ( ) {


        @Override
        public void onMapReady ( GoogleMap googleMap ) {
            mMap = googleMap;

            if ( ActivityCompat.checkSelfPermission ( getApplicationContext ( ), Manifest.permission.ACCESS_FINE_LOCATION )
                    != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions ( ActivityMain.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION );
                return;
            }

            if ( ActivityCompat.checkSelfPermission ( getApplicationContext ( ), Manifest.permission.ACCESS_COARSE_LOCATION )
                    != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions ( ActivityMain.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION );
                return;
            }

            mMap.setMyLocationEnabled ( true );
            mMap.setOnMyLocationButtonClickListener ( ( ) -> {

                if ( !IsGpsEnabled ( ) )
                    showSettingsAlert ( );
                else {
                    String url = getDirectionsUrl ( modeMap );
                    loadRoad ( url );
                }

                CameraUpdate cameraUpdate3 = CameraUpdateFactory.newLatLngZoom ( userlocation, 18.5f );
                mMap.animateCamera ( cameraUpdate3 );

                return true;
            } );

            mMap.getUiSettings ( ).setMapToolbarEnabled ( false );
            doctor_marker = ( ( LayoutInflater ) getSystemService ( Context.LAYOUT_INFLATER_SERVICE ) ).inflate ( R.layout.custom_marker_destination, null );
            CircleImageView profileimage = doctor_marker.findViewById ( R.id.profileimage );

            user_marker = ( ( LayoutInflater ) getSystemService ( Context.LAYOUT_INFLATER_SERVICE ) ).inflate ( R.layout.custom_marker_user, null );

            txtdistance = user_marker.findViewById ( R.id.txtdistance );
            txtTime = user_marker.findViewById ( R.id.txtTime );

            getUserLocation ( );

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom ( userlocation, 13.5f );
            mMap.animateCamera ( cameraUpdate );

            Picasso.get ( )
                   .load ( "http://www.rutherfordjournal.org/images/TAHC_Turing_1948.jpg" )
                   .error ( R.drawable.ic_person_svg )
                   .into ( profileimage, new Callback ( ) {
                       @Override
                       public void onSuccess ( ) {

                           mMap.clear ( );
                           mMap.addMarker ( new MarkerOptions ( )
                                   .position ( doctorlocation )
                                   .title ( "alan turing :)" )
                                   .snippet ( "computer scientist" )
                                   .icon ( BitmapDescriptorFactory.fromBitmap ( createDrawableFromView ( doctor_marker ) ) ) );


                           if ( userlocation != null ) {
                               mMap.addMarker ( new MarkerOptions ( )
                                       .position ( userlocation )
                                       .icon ( BitmapDescriptorFactory.fromBitmap ( createDrawableFromView ( user_marker ) ) ) );
                           }

                           CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom ( doctorlocation, 13.5f );
                           mMap.animateCamera ( cameraUpdate );
                           mMap.moveCamera ( cameraUpdate );

                           String url = getDirectionsUrl ( modeMap );
                           loadRoad ( url );

                       }

                       @Override
                       public void onError ( Exception e ) {
                       }
                   } );

            String url = getDirectionsUrl ( modeMap );
            loadRoad ( url );


            if ( ! IsGpsEnabled ( ) )
                showSettingsAlert ( );
            else {
                String urlWalk = getDirectionsUrl ( "walking" );
                initDistanceWalk ( urlWalk );

                String urlCar = getDirectionsUrl ( "driving" );
                initDistanceCar ( urlCar );
            }

            mMap.setOnMarkerClickListener ( new GoogleMap.OnMarkerClickListener ( ) {
                @Override
                public boolean onMarkerClick ( Marker marker ) {
                    return false;
                }
            } );

        }
    };


    LocationManager mLocationManager;

    private Location getLastKnownLocation ( ) {
        mLocationManager = ( LocationManager ) getApplicationContext ( ).getSystemService ( LOCATION_SERVICE );
        List <String> providers = mLocationManager.getProviders ( true );
        Location bestLocation = null;
        for ( String provider : providers ) {
            if ( ActivityCompat.checkSelfPermission ( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission ( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                return null;
            }
            Location l = mLocationManager.getLastKnownLocation ( provider );
            if ( l == null ) {
                continue;
            }
            if ( bestLocation == null || l.getAccuracy ( ) < bestLocation.getAccuracy ( ) ) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public boolean IsGpsEnabled ( ) {
        LocationManager locationManager = ( LocationManager ) context.getSystemService ( LOCATION_SERVICE );
        return locationManager.isProviderEnabled ( LocationManager.GPS_PROVIDER );
    }

    public boolean IsNetWorkEnabled ( ) {
        LocationManager locationManager = ( LocationManager ) context.getSystemService ( LOCATION_SERVICE );
        return locationManager.isProviderEnabled ( LocationManager.GPS_PROVIDER );
    }

    public void showSettingsAlert ( ) {

        final DialogPlus dialog = DialogPlus.newDialog ( context )
                                            .setContentHolder ( new ViewHolder ( R.layout.dialog_permission_location ) )
                                            .setContentHeight ( ViewGroup.LayoutParams.WRAP_CONTENT )
                                            .setContentWidth ( ViewGroup.LayoutParams.MATCH_PARENT )
                                            .setMargin ( 15, 40, 15, 40 )
                                            .setInAnimation ( R.anim.fade_in_center )
                                            .setOutAnimation ( R.anim.fade_out_center )
                                            .setGravity ( Gravity.CENTER )
                                            .create ( );

        View dialogHolderView = dialog.getHolderView ( );
        Button btnno = dialogHolderView.findViewById ( R.id.btn_no );
        btnno.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick ( View v ) {
                dialog.dismiss ( );
            }
        } );
        Button btnyes = dialogHolderView.findViewById ( R.id.btn_yesexit );
        btnyes.setOnClickListener ( v -> {

            Intent intent = new Intent ( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
            startActivity ( intent );
            dialog.dismiss ( );

        } );
        dialog.show ( );

    }


}
