package com.homeaway.homeawayplaces.views;

import com.jvanila.mobile.mvp.IView;

/**
 * Created by pavan on 27/05/18.
 *
 */
public interface IPlaceDetailView extends IView {

    void setTitleWith(String title);

    void setStaticMapHeight(int height);

    void setStaticMap(byte[] mapRawData);

    IPlaceListItemView getPlaceListItemView();

    void showBrowserView(String url);

    void showCallerView();

    void showProgress(boolean show);
}
