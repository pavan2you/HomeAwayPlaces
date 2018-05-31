package com.homeaway.homeawayplaces.binders;

import com.homeaway.homeawayplaces.domain.Place;
import com.homeaway.homeawayplaces.droid.R;
import com.homeaway.homeawayplaces.views.IPlaceListItemView;
import com.jvanila.core.INumberUtils;
import com.jvanila.core.locale.Resource;
import com.jvanila.mobile.mvp.DataBinder;

/**
 * Created by pavan on 27/05/18.
 *
 * The DataBinder, which is responsible to render the model state to corresponding IBindableView.
 * The results of the Binding is similar to the Observable way of reflecting data change.
 *
 * com.jvanila.mobile.util.BinderUtil.refreshItem...() methods are actually does the magic of
 * Binding.
 *
 * 1. Cross Platform Data Binding
 *      A multi-language (Android, iOS, Windows) compatible way of data binding.
 */
public class PlaceListItemDataBinder extends DataBinder<IPlaceListItemView> {

    private static final String RATING_COLOR_GOOD       = "#64DD17"; //8, 9, 10 ratings
    private static final String RATING_COLOR_AVG        = "#CCFF90"; //6, 7 ratings
    private static final String RATING_COLOR_BELOW_AVG  = "#FFAB40"; //4, 5 ratings
    private static final String RATING_COLOR_POOR       = "#DD2C00"; //1,2,3 ratings

    private static final String FAVORITE_TINT_COLOR     = "#FF4081";

    private static final int RATING_BELOW_AVG_LB    = 4;
    private static final int RATING_AVG_LB          = 6;
    private static final int RATING_GOOD_LB         = 8;

    private INumberUtils mNumberUtils;

    public PlaceListItemDataBinder(IPlaceListItemView view) {
        super(view);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNumberUtils = mMobilePlatformFactory.getNumberUtils();
    }

    @Override
    public boolean onBind(Object... objects) {
        onBindPlace((Place) objects[0]);
        return true;
    }

    private void onBindPlace(Place place) {
        /*
         *  Bind model to view
         */
        getView().addTag(place);

        getView().setPlaceLabel(place.name);

        getView().setPlaceAddress(place.address);

        String categoryLabel = "";
        String iconUrl = null;

        if (place.category != null) {
            categoryLabel = place.category;
            iconUrl = place.imageUrl;
        }

        getView().setCategoryLabel(categoryLabel);
        getView().setCategoryIcon(iconUrl);

        if (place.isFavorite) {
            /*
             * A multi-language (Android, iOS, Windows) compatible way of accessing resources
             */
            Resource resource = new Resource(R.drawable.outline_favorite_black_24,
                    "outline_favorite_black_24", "drawable");
            getView().setFavoriteIcon(resource, FAVORITE_TINT_COLOR);
        }
        else {
            /*
             * A multi-language (Android, iOS, Windows) compatible way of accessing resources
             */
            Resource resource = new Resource(R.drawable.outline_favorite_border_black_24,
                    "outline_favorite_border_black_24", "drawable");
            getView().setFavoriteIcon(resource, FAVORITE_TINT_COLOR);
        }

        if (place.distanceToInterest > 0) {
            String distanceLabel = mNumberUtils.stringifyInt(place.distanceToInterest) + "m";
            getView().setAndShowDistanceLabel(distanceLabel);
        }
        else {
            getView().hideDistanceLabel();
        }

        if (place.rating > 0) {
            String ratingLabel = mNumberUtils.stringifyFloat(place.rating);
            String ratingColor = getRatingColor((int) place.rating);
            getView().setAndShowRatingIconAndLabel(ratingLabel, ratingColor);
        }
        else {
            getView().hideRatingIconAndLabel();
        }

        if (place.showWebsite) {
            if (!mStringUtils.isNullOrEmpty(place.websiteUrl)) {
                getView().setAndShowWebsiteUrl(place.websiteUrl);
            }
            getView().hideDivider();
        }
        else {
            getView().hideWebsiteUrl();
            getView().showDivider();
        }
    }

    private String getRatingColor(int rating) {

        String color = RATING_COLOR_POOR;

        if (rating >= RATING_GOOD_LB) {
            color = RATING_COLOR_GOOD;
        }
        else if (rating >= RATING_AVG_LB) {
            color = RATING_COLOR_AVG;
        }
        else if (rating >= RATING_BELOW_AVG_LB) {
            color = RATING_COLOR_BELOW_AVG;
        }

        return color;
    }
}
