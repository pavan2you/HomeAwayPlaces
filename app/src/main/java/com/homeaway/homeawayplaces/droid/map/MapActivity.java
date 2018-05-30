package com.homeaway.homeawayplaces.droid.map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.homeaway.homeawayplaces.domain.MarkerModel;
import com.homeaway.homeawayplaces.domain.Place;
import com.homeaway.homeawayplaces.droid.R;
import com.homeaway.homeawayplaces.droid.placedetail.PlaceDetailActivity;
import com.homeaway.homeawayplaces.presenters.MapPresenter;
import com.homeaway.homeawayplaces.views.IMapView;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.droid.core.VanilaAppCompatActivity;
import com.jvanila.droid.location.LatLngBoundsWrapper;
import com.jvanila.droid.location.MarkerOptionsWrapper;
import com.jvanila.droid.location.MarkerWrapper;
import com.jvanila.mobile.location.ICircle;
import com.jvanila.mobile.location.ICircleOptions;
import com.jvanila.mobile.location.ILatLngBounds;
import com.jvanila.mobile.location.IMarker;
import com.jvanila.mobile.location.IMarkerOptions;
import com.jvanila.mobile.location.IPolygon;
import com.jvanila.mobile.location.IPolygonOptions;
import com.jvanila.mobile.location.IPolyline;
import com.jvanila.mobile.location.IPolylineOptions;
import com.jvanila.mobile.location.IVanilaMapCallbacks;
import com.jvanila.mobile.location.VanilaLocation;

import java.util.HashMap;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class MapActivity extends VanilaAppCompatActivity implements IMapView,
        OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener, GoogleMap.OnMapLoadedCallback,
        GoogleMap.CancelableCallback, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    public static final String EXTRA_CENTER = "EXTRA_CENTER";
    public static final String EXTRA_PLACE_LIST = "EXTRA_PLACE_LIST";

    private GoogleMap mMap;

    private MapPresenter mMapPresenter;
    private HashMap<MarkerModel, MarkerWrapper> mMarkerCache;
    private IVanilaMapCallbacks mCallback;
    private Handler mHandler;
    private Runnable mPostBoundZoomSetterRunnable;
    private CameraUpdate mCameraUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMarkerCache = new HashMap<>();
        mHandler = new Handler();
        loadView();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreateController(String controllerClassName, Bundle savedInstanceState) {
        super.onCreateController(controllerClassName, savedInstanceState);
        mMapPresenter = (MapPresenter) mController;

        VanilaLocation center = (VanilaLocation) getIntent().getSerializableExtra(EXTRA_CENTER);
        IGenericList<Place> places = (IGenericList<Place>) getIntent().getSerializableExtra(
                EXTRA_PLACE_LIST);
        mMapPresenter.loadWith(places, center);
    }

    private void loadView() {
        setContentView(R.layout.map_view);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (googleMap == null) {
            mCallback.onMapNotAvailable();
        }
        else {
            mMap = googleMap;

            mMap.setTrafficEnabled(false);

            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setOnCameraIdleListener(this);
            mMap.setOnCameraMoveStartedListener(this);
            mMap.setOnCameraMoveListener(this);
            mMap.setOnCameraMoveCanceledListener(this);
            mMap.setOnMarkerClickListener(this);
            mMap.setOnInfoWindowClickListener(this);

            float density = getResources().getDisplayMetrics().density;
            int padding = (int) (16 * density);
            mMap.setPadding(padding, padding, padding, padding);

            mCallback.onMapAvailable();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setCallbacksListener(IVanilaMapCallbacks callback) {
        mCallback = callback;
    }

    @Override
    public void setUpMapWithType(int mapType) {
        mMap.setMapType(mapType);
    }

    @Override
    public void animateCamera(VanilaLocation vanilaLocation, float zoomLevel) {
        LatLng latLng = new LatLng(vanilaLocation.latitude, vanilaLocation.longitude);
        CameraPosition newCamPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoomLevel)
                .bearing(0)
                .tilt(1)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPosition));
    }

    @Override
    public void animateCameraBounds(ILatLngBounds latLngBounds, int padding) {
        LatLngBoundsWrapper wrapper = (LatLngBoundsWrapper) latLngBounds;
        try {
            mMap.getUiSettings().setAllGesturesEnabled(false);
            mMap.getUiSettings().setRotateGesturesEnabled(false);
            mMap.getUiSettings().setTiltGesturesEnabled(false);
            mMap.getUiSettings().setZoomGesturesEnabled(false);
            CameraUpdate cameraBoundsUpdate = CameraUpdateFactory.newLatLngBounds(wrapper.mBounds,
                    padding);
            mMap.animateCamera(cameraBoundsUpdate, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void animateCameraToLocation(final VanilaLocation vanilaLocation) {
        mHandler.postDelayed(mPostBoundZoomSetterRunnable = new Runnable() {
            @Override
            public void run() {
                LatLng latLng = new LatLng(vanilaLocation.latitude, vanilaLocation.longitude);
                CameraUpdate cameraZoomUpdate = CameraUpdateFactory.newLatLngZoom(
                        latLng, mMap.getCameraPosition().zoom);
                mMap.animateCamera(cameraZoomUpdate);
                mPostBoundZoomSetterRunnable = null;
            }
        }, 1000);
    }

    @Override
    public void handleAnyNativeChangesDueToAnimationCancel() {
        mHandler.removeCallbacks(mPostBoundZoomSetterRunnable);
    }

    @Override
    public void stopAnimation() {
        if (mMap == null) {
            return;
        }
        mMap.stopAnimation();
    }

    @Override
    public void clearMap() {
        mMarkerCache.clear();

        if (mMap == null) {
            return;
        }
        mMap.setOnMapLoadedCallback(this);
    }

    @Override
    public IPolygon addPolygon(IPolygonOptions polygonOptions) {
        return null;//NA
    }

    @Override
    public IPolyline addPolyline(IPolylineOptions polylineOptions) {
        return null;//NA
    }

    @Override
    public IMarker addMarker(IMarkerOptions markerOptions) {
        return null;//NA
    }

    @Override
    public ICircle addCircle(ICircleOptions circleOptions) {
        return null;//NA
    }

    @Override
    public IMarker addNewOrCachedMarker(MarkerModel model, IMarkerOptions markerOptions) {
        markerOptions.build();

        MarkerOptionsWrapper optionsWrapper = (MarkerOptionsWrapper) markerOptions;
        Marker marker = mMap.addMarker(optionsWrapper.mOptions);
        marker.setTag(model);

        MarkerWrapper markerWrapper = new MarkerWrapper(marker, markerOptions.getParams());

        mMarkerCache.put(model, markerWrapper);

        return markerWrapper;
    }

    @Override
    public void showInfoMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void notifyModelRefreshComplete() {
        //NA
    }

    @Override
    public void onMapLoaded() {
        if (mCallback != null) {
            mCallback.onMapLoaded();
        }
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        mCallback.onCameraMoveStarted();
    }

    @Override
    public void onCameraMove() {
        ((MapPresenter) mCallback).onCameraMove();
    }

    @Override
    public void onCameraMoveCanceled() {
        if (mCallback != null) {
            mCallback.onCameraPositionChange(mMap.getCameraPosition().zoom);
        }
    }

    @Override
    public void onCameraIdle() {
        if (mCallback != null) {
            mCallback.onCameraPositionChange(mMap.getCameraPosition().zoom);
        }
    }

    @Override
    public void onCancel() {
        if (mCameraUpdate != null) {
            mMap.moveCamera(mCameraUpdate);
            mCameraUpdate = null;
        }
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mCallback.onAnimationCancel();
    }

    @Override
    public void onFinish() {
        mCameraUpdate = null;
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mCallback.onAnimationComplete();
    }

    @Override
    public void showInfoWindowFor(MarkerModel markerModel) {
        MarkerWrapper wrapper = mMarkerCache.get(markerModel);
        wrapper.marker.showInfoWindow();
    }

    @Override
    public void showCallerView() {
        finish();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMapPresenter.onMarkerClick((MarkerModel) marker.getTag());
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        mMapPresenter.onMarkerInfoWindowClick((MarkerModel) marker.getTag());
    }

    @Override
    public void showPlaceDetailView(Place place, VanilaLocation areaOfInterest) {
        Intent intent = new Intent(getApplicationContext(), PlaceDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(PlaceDetailActivity.EXTRA_PLACE, place);
        intent.putExtra(PlaceDetailActivity.EXTRA_CENTER, areaOfInterest);

        startActivity(intent);
    }
}
