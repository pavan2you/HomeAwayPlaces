package com.homeaway.homeawayplaces.droid;

import android.support.annotation.NonNull;

import com.jvanila.core.BuildInfo;
import com.jvanila.droid.core.VanilaApplication;
import com.jvanila.mobile.MobileBuildInfo;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class HomeAwayPlacesApplication extends VanilaApplication {

    @Override
    public synchronized boolean init() {
        try {
            super.init();

            SetupApis setupApis = new SetupApis();
            setupApis.setupOnAppInit(mMobilePlatformFactory);
        }
        catch (Exception e) {
            e.printStackTrace();
            return true;
        }

        return true;
    }

    @Override
    protected void prepareBuildInfo() {
        MobileBuildInfo mobileBuildInfo = mMobilePlatformFactory.getBuildInfo();

        //build identifiers
        mobileBuildInfo.buildType = BuildConfig.DEBUG ?
                BuildInfo.BUILD_TYPE_DEBUG : BuildInfo.BUILD_TYPE_RELEASE;
        mobileBuildInfo.appVersion = BuildConfig.VERSION_NAME;
        mobileBuildInfo.currentBuildId = String.valueOf(BuildConfig.BUILD_ID);
        mobileBuildInfo.currentBuildName = BuildConfig.BUILD_NAME;

        //database
        mobileBuildInfo.currentAppDataVersion = BuildConfig.CURRENT_APP_DATA_VERSION;
        mobileBuildInfo.previousAppDataVersion = mobileBuildInfo.currentAppDataVersion - 1;

        //network
        mobileBuildInfo.serverBaseUrl = BuildConfig.HTTP_SERVER_BASE_URL;
        mobileBuildInfo.serverApiBaseUrl = BuildConfig.HTTP_SERVER_BASE_URL;
    }

    @NonNull
    @Override
    protected SQLiteInfo loadSqliteDatabaseInfo() {
        SQLiteInfo sqLiteInfo = new SQLiteInfo();
        sqLiteInfo.name = "homeaway_places_database.db";
        sqLiteInfo.createSchemaList = mMobilePlatformFactory.newList();
        sqLiteInfo.createSchemaList.add("create_schema.sql");
        sqLiteInfo.deleteSchemaList = mMobilePlatformFactory.newList();
        sqLiteInfo.deleteSchemaList.add("delete_schema.sql");

        MobileBuildInfo mobileBuildInfo = mMobilePlatformFactory.getBuildInfo();
        sqLiteInfo.version = mobileBuildInfo.currentAppDataVersion;

        return sqLiteInfo;
    }
}
