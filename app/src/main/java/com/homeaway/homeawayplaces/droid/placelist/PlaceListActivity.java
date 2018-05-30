package com.homeaway.homeawayplaces.droid.placelist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.homeaway.homeawayplaces.domain.Place;
import com.homeaway.homeawayplaces.droid.R;
import com.homeaway.homeawayplaces.droid.map.MapActivity;
import com.homeaway.homeawayplaces.droid.placedetail.PlaceDetailActivity;
import com.homeaway.homeawayplaces.droid.util.ViewCompatUtil;
import com.homeaway.homeawayplaces.presenters.PlaceListPresenter;
import com.homeaway.homeawayplaces.views.IPlaceListView;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.droid.core.VanilaAppCompatActivity;
import com.jvanila.droid.util.ViewUtils;
import com.jvanila.mobile.location.VanilaLocation;

/**
 * Created by pavan on 27/05/18.
 *
 * An android component, which implements IPlaceListView view contract.
 *
 * Because of cleaner separation of responsibilities, The Activity code is very light weight,
 * zero business logic, zero work flow logic and very minimal conditional code. The left over
 * conditional code to handle some very android specific logic. In this class, the left over
 * conditional depth is just 1.
 *
 */
public class PlaceListActivity extends VanilaAppCompatActivity implements IPlaceListView,
        View.OnClickListener {

    private PlaceListPresenter mPlaceListPresenter;

    private TextView mToolbarTitle;
    private EditText mSearchField;
    private ImageButton mCloseButton;
    private ProgressBar mProgressBar;
    private TextView mNoDataLabelView;
    private RecyclerView mPlaceListView;
    private PlaceListAdapter mListAdapter;
    private FloatingActionButton mMapFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadView();
    }

    @Override
    protected void onCreateController(String controllerClassName, Bundle savedInstanceState) {
        super.onCreateController(controllerClassName, savedInstanceState);
        mPlaceListPresenter = (PlaceListPresenter) mController;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.place_list_menu, menu);
        return true;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////  IPlaceListView  ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadView() {
        setContentView(R.layout.place_list_view);

        setupToolbar();

        mSearchField = findViewById(R.id.place_list_search_field);
        mSearchField.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mPlaceListPresenter.onSearchPlaces(s.toString());
            }
        });

        mCloseButton = findViewById(R.id.place_list_close_button);
        mCloseButton.setOnClickListener(this);

        mProgressBar = findViewById(R.id.place_list_progress);

        mNoDataLabelView = findViewById(R.id.place_list_no_data_label);

        mPlaceListView = findViewById(R.id.place_list_list);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        mPlaceListView.setLayoutManager(llm);
        mListAdapter = new PlaceListAdapter(getLayoutInflater(), this);
        mPlaceListView.setAdapter(mListAdapter);

        mMapFab = findViewById(R.id.place_list_fab_map);
        mMapFab.setOnClickListener(this);

        ViewCompatUtil.setCompoundDrawablesWithIntrinsicBounds(getApplicationContext(),
                mSearchField, R.color.colorTint, -1, -1, -1);
        ViewCompatUtil.setCompoundDrawablesWithIntrinsicBounds(getApplicationContext(),
                mNoDataLabelView, -1, R.color.colorTint, -1, -1);
    }

    private Toolbar setupToolbar() {
        Toolbar toolbar = findViewById(R.id.place_list_toolbar);

        @SuppressLint("InflateParams") View toolbarView = getLayoutInflater().inflate(
                R.layout.toolbar_with_title, null);

        mToolbarTitle = toolbarView.findViewById(R.id.toolbar_title);
        toolbar.addView(toolbarView);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setSubtitle(null);
        }

        return toolbar;
    }

    @Override
    public void setAndShowDataModel(IGenericList<Place> placeList) {
        mListAdapter.notifyDataSetChanged(placeList);
    }

    @Override
    public void hideListView() {
        mListAdapter.notifyDataSetChanged(null);
    }

    @Override
    public void showInfoMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showProgress(boolean show) {
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSearchFieldText(String text) {
        mSearchField.setText(text);
    }

    @Override
    public void setAndShowNoDataLabel(String message) {
        mNoDataLabelView.setVisibility(View.VISIBLE);
        mNoDataLabelView.setText(message);
    }

    @Override
    public void hideNoDataLabel() {
        mNoDataLabelView.setVisibility(View.GONE);
    }

    @Override
    public void hideFabIcon() {
        mMapFab.setVisibility(View.GONE);
    }

    @Override
    public void showFabIcon() {
        mMapFab.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPoiLabel(String label) {
        mSearchField.setHint(label);
    }

    @Override
    public void setTitleWith(String title) {
        mToolbarTitle.setText(title);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////  USER INTERACTION  ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.place_list_menu_change_poi) {
            mPlaceListPresenter.onChangePoiMenuItemClick();
        }
        else if (item.getItemId() == R.id.place_list_menu_my_location_poi) {
            mPlaceListPresenter.onUseMyLocationAsPoiMenuItemClick();
        }
        else if (item.getItemId() == R.id.place_list_menu_my_favs) {
            mPlaceListPresenter.onMyFavPlacesMenuItemClick();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.place_list_item_view) {
            mPlaceListPresenter.onPlaceItemClick((Place) v.getTag());
        }
        else if (v.getId() == R.id.place_list_item_fav_icon) {
            View parentParent = (View) v.getParent().getParent();
            mPlaceListPresenter.onPlaceItemFavIconClick((Place) parentParent.getTag());
        }
        else if (v.getId() == R.id.place_list_close_button) {
            mPlaceListPresenter.onCancelSearchIconClick();
        }
        else if (v.getId() == R.id.place_list_fab_map) {
            mPlaceListPresenter.onMapIconClick();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////  NAVIGATION FLOWS  ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void showPlaceDetailView(Place place, VanilaLocation areaOfInterest) {
        Intent intent = new Intent(getApplicationContext(), PlaceDetailActivity.class);
        /*
         * To avoid any quick clicks
         */
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        /*
         * supply params
         */
        intent.putExtra(PlaceDetailActivity.EXTRA_PLACE, place);
        intent.putExtra(PlaceDetailActivity.EXTRA_CENTER, areaOfInterest);

        startActivity(intent);
    }

    @Override
    public void showMapView(IGenericList<Place> placeList, VanilaLocation areaOfInterest) {
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        /*
         * To avoid any quick clicks
         */
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        /*
         * supply params
         */
        intent.putExtra(MapActivity.EXTRA_PLACE_LIST, placeList);
        intent.putExtra(PlaceDetailActivity.EXTRA_CENTER, areaOfInterest);

        startActivity(intent);
    }

    @Override
    public void exitApplication() {
        /*
         * Ideally not required, but to handle any corner scenarios, killing presenter a few millis
         * before than activity.
         */
        mController.onDestroy();

        finish();

        /*
         * a legacy way of killing app
         */
        mApplication.exitApplication();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////  VIEW WILL DIE  ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onDestroy() {
        /*
         * Free views voluntarily.
         */

        if (mListAdapter != null) {
            mListAdapter.release();
            mListAdapter = null;
        }

        ViewUtils.unbindReferences(mToolbarTitle);
        ViewUtils.unbindReferences(mSearchField);
        ViewUtils.unbindReferences(mCloseButton);
        ViewUtils.unbindReferences(mNoDataLabelView);
        ViewUtils.unbindReferences(mPlaceListView);
        ViewUtils.unbindReferences(mMapFab);

        mToolbarTitle = null;
        mSearchField = null;
        mCloseButton = null;
        mNoDataLabelView = null;
        mPlaceListView = null;
        mMapFab = null;

        mPlaceListPresenter = null;

        super.onDestroy();
    }
}
