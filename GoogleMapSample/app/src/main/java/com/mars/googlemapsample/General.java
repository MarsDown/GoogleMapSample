package com.mars.googlemapsample;

import android.app.Application;

/**
 * Created by mars on 4/4/2018.
 */
public class General extends Application {

    @Override
    public void onCreate ( ) {
        super.onCreate ( );



        RequestManager.init ( this );

    }

}
