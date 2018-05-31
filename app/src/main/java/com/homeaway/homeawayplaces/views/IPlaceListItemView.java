package com.homeaway.homeawayplaces.views;

import com.homeaway.homeawayplaces.domain.Place;
import com.jvanila.core.locale.Resource;
import com.jvanila.mobile.mvp.IBindableView;

/**
 * Created by pavan on 27/05/18.
 *
 */
public interface IPlaceListItemView extends IBindableView<Place> {

    void setCategoryIcon(String iconUrl);

    void setPlaceLabel(String name);

    void setPlaceAddress(String address);

    void setCategoryLabel(String categoryLabel);

    void setFavoriteIcon(Resource resource, String tint);

    void setAndShowDistanceLabel(String distanceLabel);

    void setAndShowRatingIconAndLabel(String ratingLabel, String ratingColor);

    void hideDistanceLabel();

    void setAndShowWebsiteUrl(String websiteUrl);

    void hideWebsiteUrl();

    void hideRatingIconAndLabel();

    void showDivider();

    void hideDivider();
}
