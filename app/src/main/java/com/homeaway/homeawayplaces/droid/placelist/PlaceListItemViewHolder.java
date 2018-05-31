package com.homeaway.homeawayplaces.droid.placelist;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.homeaway.homeawayplaces.domain.Place;
import com.homeaway.homeawayplaces.binders.PlaceListItemDataBinder;
import com.homeaway.homeawayplaces.droid.R;
import com.homeaway.homeawayplaces.droid.util.ViewCompatUtil;
import com.homeaway.homeawayplaces.views.IPlaceListItemView;
import com.jvanila.core.locale.Resource;
import com.jvanila.droid.core.VanilaRecyclerBindableViewHolder;

/**
 * Created by pavan on 27/05/18.
 *
 * A Bindable View for Place List Item. The results of the Binding is similar to the Observable way
 * of reflecting data change. Due to the clear separation of logic even at cell level, the class
 * meets zero business-logic, zero work-flow logic and zero conditional code.
 *
 * This class is shared both in IPlaceListView and IPlaceDetailView
 */
public class PlaceListItemViewHolder extends VanilaRecyclerBindableViewHolder<Place>
        implements IPlaceListItemView {

    private ImageView mIconView;
    private TextView mNameView;
    private TextView mAddressView;
    private TextView mCategoryView;
    private ImageView mFavouriteIconView;
    private TextView mDistanceView;
    private TextView mRatingView;
    private TextView mWebsiteView;
    private View mDividerView;

    public PlaceListItemViewHolder(View view, View.OnClickListener clickListener) {
        super(view, PlaceListItemDataBinder.class.getName());
        loadView(clickListener);
    }

    private void loadView(View.OnClickListener clickListener) {
        mIconView = mView.findViewById(R.id.place_list_item_icon);
        mNameView = mView.findViewById(R.id.place_list_item_name);
        mAddressView = mView.findViewById(R.id.place_list_item_address);
        mCategoryView = mView.findViewById(R.id.place_list_item_category);
        mFavouriteIconView = mView.findViewById(R.id.place_list_item_fav_icon);
        mDistanceView = mView.findViewById(R.id.place_list_item_distance);
        mRatingView = mView.findViewById(R.id.place_list_item_rating);
        mWebsiteView = mView.findViewById(R.id.place_list_item_website);
        mDividerView = mView.findViewById(R.id.place_list_item_divider);
        mWebsiteView.setOnClickListener(clickListener);

        mFavouriteIconView.setOnClickListener(clickListener);
    }

    @Override
    public void setCategoryIcon(String imageUrl) {
        try {
            Glide.with(mIconView.getContext()).load(imageUrl).thumbnail(0.1f)
                    .placeholder(R.drawable.place_placeholder).into(mIconView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPlaceLabel(String name) {
        mNameView.setText(name);
    }

    @Override
    public void setPlaceAddress(String address) {
        mAddressView.setText(address);
    }

    @Override
    public void setCategoryLabel(String categoryLabel) {
        mCategoryView.setText(categoryLabel);
    }

    @Override
    public void setFavoriteIcon(Resource resource, String tint) {
        mFavouriteIconView.setImageResource(resource.resId);
        ViewCompatUtil.setDrawableTint(mFavouriteIconView.getDrawable(), Color.parseColor(tint));
    }

    @Override
    public void setAndShowDistanceLabel(String distanceLabel) {
        mDistanceView.setVisibility(View.VISIBLE);
        mDistanceView.setText(distanceLabel);
    }

    @Override
    public void setAndShowRatingIconAndLabel(String ratingLabel, String ratingColor) {
        mRatingView.setVisibility(View.VISIBLE);

        mRatingView.setText(ratingLabel);

        int color = Color.parseColor(ratingColor);
        mRatingView.setTextColor(color);
        ViewCompatUtil.setCompoundDrawablesWithIntrinsicBounds(mRatingView, color, -1,
                -1, -1);
    }

    @Override
    public void hideDistanceLabel() {
        mDistanceView.setVisibility(View.GONE);
    }

    @Override
    public void setAndShowWebsiteUrl(String websiteUrl) {
        mWebsiteView.setVisibility(View.VISIBLE);
        SpannableString content = new SpannableString(websiteUrl);
        content.setSpan(new UnderlineSpan(), 0, websiteUrl.length(), 0);
        mWebsiteView.setText(content);
    }

    @Override
    public void hideWebsiteUrl() {
        mWebsiteView.setVisibility(View.GONE);
    }

    @Override
    public void hideRatingIconAndLabel() {
        mRatingView.setVisibility(View.GONE);
    }

    @Override
    public void showDivider() {
        mDividerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideDivider() {
        mDividerView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        mFavouriteIconView.setOnClickListener(null);
        itemView.setOnClickListener(null);
        super.onDestroy();
    }
}
