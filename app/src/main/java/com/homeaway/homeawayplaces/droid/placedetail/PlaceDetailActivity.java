package com.homeaway.homeawayplaces.droid.placedetail;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.homeaway.homeawayplaces.domain.Place;
import com.homeaway.homeawayplaces.droid.R;
import com.homeaway.homeawayplaces.droid.placelist.PlaceListItemViewHolder;
import com.homeaway.homeawayplaces.presenters.PlaceDetailPresenter;
import com.homeaway.homeawayplaces.views.IPlaceDetailView;
import com.homeaway.homeawayplaces.views.IPlaceListItemView;
import com.jvanila.droid.collections.GenericList;
import com.jvanila.droid.core.VanilaAppCompatActivity;
import com.jvanila.droid.util.BitmapUtils;
import com.jvanila.droid.util.ViewUtils;
import com.jvanila.mobile.location.VanilaLocation;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class PlaceDetailActivity extends VanilaAppCompatActivity implements IPlaceDetailView,
        View.OnClickListener {

    public static final String EXTRA_PLACE = "EXTRA_PLACE";
    public static final String EXTRA_CENTER = "EXTRA_CENTER";

    private ImageView mStaticMapView;
    private PlaceListItemViewHolder mReusablePlaceDetailItemView;

    private PlaceDetailPresenter mPlaceDetailPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadView();
    }

    @Override
    protected void onCreateController(String controllerClassName, Bundle savedInstanceState) {
        super.onCreateController(controllerClassName, savedInstanceState);

        Place place = (Place) getIntent().getSerializableExtra(EXTRA_PLACE);
        /*
         * Fix for binding. A DataObject can refresh N views, but as the property is transient,
         * we need to recreate that property. Ideally doing this in respective call is a good fix,
         * but jvanila-ios-conversion tool yet to support respective conversion pattern for iOS.
         * So till then this need to live with this patch fix as and where view binding is required.
         */
        place.tagList = new GenericList<>();

        VanilaLocation center = (VanilaLocation) getIntent().getSerializableExtra(EXTRA_CENTER);

        mPlaceDetailPresenter = (PlaceDetailPresenter) mController;
        mPlaceDetailPresenter.loadWith(place, center);
    }

    private void loadView() {
        setContentView(R.layout.place_detail_view);

        setupToolbar();

        mStaticMapView = findViewById(R.id.place_detail_static_map);

        mReusablePlaceDetailItemView = new PlaceListItemViewHolder(
                findViewById(R.id.place_list_item_view), this);
    }

    private Toolbar setupToolbar() {
        Toolbar toolbar = findViewById(R.id.place_detail_toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        return toolbar;
    }

    @Override
    public void setTitleWith(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void setStaticMapHeight(int height) {
        View collapsingToolbar = findViewById(R.id.place_detail_collapsing_toolbar);

        AppBarLayout.LayoutParams parameters = (AppBarLayout.LayoutParams)
                collapsingToolbar.getLayoutParams();
        parameters.height = height;
        collapsingToolbar.setLayoutParams(parameters);
    }

    @Override
    public IPlaceListItemView getPlaceListItemView() {
        return mReusablePlaceDetailItemView;
    }

    @Override
    public void setStaticMap(byte[] mapRawData) {
        if (mapRawData != null && mapRawData.length > 0) {
            Bitmap bm = BitmapUtils.toBitmap(mapRawData);
            mStaticMapView.setImageBitmap(bm);
        }
        else {
            Toast.makeText(getApplicationContext(), "Failed to set static map",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showBrowserView(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(browserIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            mPlaceDetailPresenter.onBackPressed();
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.place_list_item_fav_icon) {
            View parentParent = (View) v.getParent().getParent();
            mPlaceDetailPresenter.onPlaceItemFavIconClick((Place) parentParent.getTag());
        }
        if (v.getId() == R.id.place_list_item_website) {
            View parentParent = (View) v.getParent().getParent();
            mPlaceDetailPresenter.onPlaceItemUrlClick((Place) parentParent.getTag());
        }
    }

    @Override
    public void showCallerView() {
        finish();
    }

    @Override
    protected void onDestroy() {
        ViewUtils.unbindReferences(mStaticMapView);

        if (mReusablePlaceDetailItemView != null) {
            mReusablePlaceDetailItemView.onDestroy();
        }

        mPlaceDetailPresenter = null;

        super.onDestroy();
    }
}
