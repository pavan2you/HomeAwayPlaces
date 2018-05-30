package com.homeaway.homeawayplaces.sync.gateways;

import android.os.AsyncTask;

import com.homeaway.homeawayplaces.droid.BuildConfig;
import com.homeaway.homeawayplaces.sync.dtos.StaticMapDTO;
import com.homeaway.homeawayplaces.sync.dtos.StaticMapMarkerDTO;
import com.homeaway.homeawayplaces.sync.dtos.StaticMapRequestDTO;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.droid.util.IOUtils;
import com.jvanila.mobile.MobilePlatformFactoryLocator;
import com.jvanila.mobile.json.JSONUtils;
import com.jvanila.mobile.sync.INetRequestQueue;
import com.jvanila.mobile.sync.IPeriodicPollingPool;
import com.jvanila.mobile.sync.SyncGateway;
import com.jvanila.mobile.sync.dtos.FailureResponseDTO;
import com.jvanila.mobile.sync.dtos.NetResponseDTO;
import com.jvanila.mobile.sync.events.NetResponseProcessingCompletedEvent;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class StaticMapGateway extends SyncGateway {

    public static final String CLASS_NAME = StaticMapGateway.class.getName();
    private static final String TAG = "StaticMapGateway";

    private static final String STATIC_MAP_BASE_URL =
            "http://maps.googleapis.com/maps/api/staticmap?";

    private LoadStaticMapAsyncTask mLoadStaticMapAsyncTask;
    private String entityName;

    public StaticMapGateway(INetRequestQueue netRequestQueue,
            IPeriodicPollingPool periodicPollingPool) {

        super(netRequestQueue, periodicPollingPool);
    }

    public void fireLoadStaticMapRequest(StaticMapRequestDTO dto) {

        if (mLoadStaticMapAsyncTask == null || mLoadStaticMapAsyncTask.isCancelled()
                || mLoadStaticMapAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)) {

            mLoadStaticMapAsyncTask = new LoadStaticMapAsyncTask(new WeakReference<>(this));

            try {
                String url = getEncodedUrl(dto);
                entityName = dto.entityName;
                mLoadStaticMapAsyncTask.execute(url);
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private String getEncodedUrl(StaticMapRequestDTO dto)
            throws UnsupportedEncodingException {

        StringBuilder builder = new StringBuilder(STATIC_MAP_BASE_URL);

        //size
        builder.append("size=").append(dto.width).append("x").append(dto.height);

        //scale
        builder.append("&scale=2");

//        zoom
        if (dto.zoomLevel > 0) {
            builder.append("&zoom=").append(dto.zoomLevel);
        }

        //center
        builder.append("&center=");

        String centerBuilder = dto.centerLatitude + "," + dto.centerLongitude;
        String encodedCenter = URLEncoder.encode(centerBuilder, "UTF-8");
        builder.append(encodedCenter);

        for (StaticMapMarkerDTO marker : dto.markerList) {

            builder.append("&markers=");

            String markerBuilder =
                    "color:" + marker.color + "|" +
                    "label:" + marker.label + "|" +
                    marker.latitude + "," +
                    marker.longitude;

            String encodedMarker = URLEncoder.encode(markerBuilder, "UTF-8");
            builder.append(encodedMarker);
        }

        String urlValue = builder.toString();

        if (BuildConfig.DEBUG) {
            System.out.println(TAG + ": STATIC_MAP_URL --> " + urlValue);
        }

        return urlValue;
    }

    private void onLoadStaticMapSyncSuccess(NetResponseDTO netResponseDTO) {
        StaticMapDTO staticMapDTO = new StaticMapDTO();
        staticMapDTO.mapRawData = netResponseDTO.Response;

        NetResponseProcessingCompletedEvent event = new NetResponseProcessingCompletedEvent();
        event.mRequestType = entityName;
        event.mContainedObjectUniqueId = StaticMapDTO.CLASS_NAME;
        event.mProcessedResultDTO = staticMapDTO;
        PlatformFactoryLocator.getPlatformFactory().getEventBus().publish(event);
    }

    private void onLoadStaticMapFailure(NetResponseDTO netResponseDTO) {
        try {
            FailureResponseDTO failureResponseDTO = (FailureResponseDTO) JSONUtils.toDataObject(
                    netResponseDTO.Error, FailureResponseDTO.CLASS_NAME);
            failureResponseDTO.RequestType = entityName;
            PlatformFactoryLocator.getPlatformFactory().getEventBus().publish(
                    failureResponseDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////  TEMPORARY FIX  ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static class LoadStaticMapAsyncTask extends
            AsyncTask<String, Void, NetResponseDTO> {

        private WeakReference<StaticMapGateway> mGatewayWeakRef;

        LoadStaticMapAsyncTask(WeakReference<StaticMapGateway> gatewayWeakRef) {
            mGatewayWeakRef = gatewayWeakRef;
        }

        @Override
        protected NetResponseDTO doInBackground(String... params) {
            if (mGatewayWeakRef.get() != null) {
                return mGatewayWeakRef.get().processStaticMapRequest(params[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(NetResponseDTO netResponseDTO) {
            if (netResponseDTO != null && mGatewayWeakRef.get() != null) {
                mGatewayWeakRef.get().onResponse(netResponseDTO);
            }
        }
    }

    private NetResponseDTO processStaticMapRequest(String urlString) {
        NetResponseDTO netResponseDTO = new NetResponseDTO();

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.setUseCaches(false);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                StringBuilder exceptionBuilder = new StringBuilder();
                byte[] bytes = IOUtils.toByteArrayUsingOutputStream(connection.getInputStream(),
                        exceptionBuilder);

                if (exceptionBuilder.length() == 0) {
                    netResponseDTO.Response = bytes;
                }
                else {
                    FailureResponseDTO failureResponseDTO = new FailureResponseDTO();
                    failureResponseDTO.type = FailureResponseDTO.ERROR_TYPE_API_EXCEPTION;
                    failureResponseDTO.message = exceptionBuilder.toString();
                    netResponseDTO.Error = JSONUtils.toJson(failureResponseDTO).getBytes();
                }
            }
            else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER
                    || responseCode == 307) {
                //ignore... for now
                if (MobilePlatformFactoryLocator.getMobilePlatformFactory().getBuildInfo()
                        .isDebugBuild()) {
                    System.out.println(TAG + ": Ignoring intermediate paths");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();

            FailureResponseDTO failureResponseDTO = new FailureResponseDTO();
            failureResponseDTO.type = FailureResponseDTO.ERROR_TYPE_API_EXCEPTION;
            failureResponseDTO.message = e.getMessage();
            failureResponseDTO.RequestType = StaticMapDTO.CLASS_NAME;
            failureResponseDTO.ContainedObjectUniqueId = StaticMapDTO.CLASS_NAME;
            netResponseDTO.Error = JSONUtils.toJson(failureResponseDTO).getBytes();
        }

        return netResponseDTO;
    }

    private void onResponse(NetResponseDTO netResponseDTO) {
        if (netResponseDTO.Error == null) {
            onLoadStaticMapSyncSuccess(netResponseDTO);
        }
        else {
            onLoadStaticMapFailure(netResponseDTO);
        }
    }
}
