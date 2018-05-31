package com.homeaway.foursqureplaces.sync.gateways;

import com.homeaway.foursqureplaces.sync.dtos.FSRequestDTO;
import com.homeaway.foursqureplaces.sync.dtos.FSResultDTO;
import com.homeaway.foursqureplaces.sync.dtos.FSVenueDTO;
import com.homeaway.foursqureplaces.sync.dtos.FSVenueListDTO;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.collection.IGenericMap;
import com.jvanila.core.datatype.IStringBuilder;
import com.jvanila.core.eventbus.IEvent;
import com.jvanila.mobile.MobilePlatformFactoryLocator;
import com.jvanila.mobile.job.BgAsyncJob;
import com.jvanila.mobile.json.JSONUtils;
import com.jvanila.mobile.sync.INetRequestQueue;
import com.jvanila.mobile.sync.IPeriodicPollingPool;
import com.jvanila.mobile.sync.SyncGateway;
import com.jvanila.mobile.sync.dtos.FailureResponseDTO;
import com.jvanila.mobile.sync.dtos.NetRequestDTO;
import com.jvanila.mobile.sync.dtos.NetResponseDTO;
import com.jvanila.mobile.sync.events.NetResponseProcessingCompletedEvent;

import java.util.UUID;

/**
 * Created by pavan on 27/05/18.
 *
 * A NetworkGateway or Service, which is responsible to fetch content from Foursquare venues
 * end point.
 *
 */
public class FSVenueSearchGateway extends SyncGateway {

    public static final String CLASS_NAME = FSVenueSearchGateway.class.getName();

    private static final String READ_VENUE_LIST_URL =
            generateEndPoint("/v2/venues/search");

    public FSVenueSearchGateway(INetRequestQueue netRequestQueue,
            IPeriodicPollingPool periodicPollingPool) {

        super(netRequestQueue, periodicPollingPool);
    }

    public void fireReadVenueListRequest(FSRequestDTO dto) {

        NetRequestDTO netRequest = new NetRequestDTO();

        netRequest.Stats.createdAt = System.currentTimeMillis();

        netRequest.UniqueId = UUID.randomUUID().toString();
        netRequest.CreatedDateTime = System.currentTimeMillis();
        netRequest.DelegateClassName = this.getClass().getName();
        netRequest.CRUDType = NetRequestDTO.CRUD_OPERATION_READ;
        netRequest.ClearCacheFlag = NetRequestDTO.CLEAR_CACHE_NOT_REQUIRED;
        netRequest.NetworkOperationType = NetRequestDTO.NETWORK_OPERATION_HTTP_GET;

        netRequest.RequestString = null;

        netRequest.RequestType = FSVenueDTO.CLASS_NAME;
        netRequest.ContainedObjectUniqueId = dto.params.get("query");
        netRequest.ResponseType = FSResultDTO.CLASS_NAME;

        netRequest.Url = READ_VENUE_LIST_URL + "?" + generateParams(dto.params);

        //Either this
        netRequest.ServeMode = NetRequestDTO.SERVE_MODE_IMMEDIATE;

        //Or this
        /*
         *this will be having an impact of Queuing for AutoComplete requests where latency matters
         * a lot, better to avoid the jVanila NetRequestQ. But still the last serving order
         * managed by request processing logic internally inside framework.
         */
/*        netRequest.IsPersistable = false;
        netRequest.IsSynchronousRequest = 0;
        netRequest.Priority = 1;
        netRequest.RetryLimitOnFailure = 5;*/

        netRequest.setCSVRepresentationOfSubscribedFailureTypeNotifyingList(
                "1,2,3,4,5", ",");
        netRequest.TreatAnyKindOfResponseAsSuccess = 1;

        netRequest.Stats.preparedAt = System.currentTimeMillis();

        MobilePlatformFactoryLocator.getMobilePlatformFactory().getSyncAdapter()
                .fireNetRequest(netRequest);
    }

    @Override
    public void onSuccess(NetResponseDTO netResponseDTO) {
        NetRequestDTO requestDTO = netResponseDTO.NetRequest;
        char crudType = requestDTO.CRUDType;
        switch (crudType) {
        case NetRequestDTO.CRUD_OPERATION_READ:
            onReadVenueEventListSuccess(netResponseDTO);
            break;

        case NetRequestDTO.CRUD_OPERATION_CREATE:
        case NetRequestDTO.CRUD_OPERATION_UPDATE:
        case NetRequestDTO.CRUD_OPERATION_DELETE:
            super.onSuccess(netResponseDTO);
            //NA
            break;
        }
    }

    @Override
    public void onFailure(NetResponseDTO netResponseDTO) {
        super.onFailure(netResponseDTO);
    }

    private void onReadVenueEventListSuccess(NetResponseDTO netResponseDTO) {

        netResponseDTO.NetRequest.Stats.dataProcessingStartedAt = System.currentTimeMillis();

        try {
            /*String data = new String(netResponseDTO.Response);
            if (BuildConfig.DEBUG) {
                System.out.println("VenueDebug : response : " + data);
            }*/

            netResponseDTO.NetRequest.Stats.parseTriggeredAt = System.currentTimeMillis();

            FSResultDTO _FSResultDTO = (FSResultDTO) JSONUtils
                    .toDataObject(netResponseDTO.Response, FSResultDTO.class.getName());

            netResponseDTO.NetRequest.Stats.parsedAt = System.currentTimeMillis();

            super.onSuccess(netResponseDTO);

            onDataContractSatisfied(netResponseDTO, _FSResultDTO);
        }
        catch (Exception e) {
            e.printStackTrace();
            onDataContractViolation(netResponseDTO);
        }
    }

    private void onDataContractSatisfied(NetResponseDTO netResponseDTO,
            FSResultDTO _FSResultDTO) {

        new FSVenueSearchGatewayToDaoStorageAsyncJob(this, netResponseDTO,
                _FSResultDTO).post();
    }

    private void onDataContractSatisfiedInternal(NetResponseDTO netResponseDTO,
            FSResultDTO _FSResultDTO) {

        IEvent result;

        if (_FSResultDTO == null ||
                (_FSResultDTO.meta != null && _FSResultDTO.meta.code != 200) ||
                (_FSResultDTO.response == null)) {

            FailureResponseDTO failureResponse = new FailureResponseDTO();

            NetRequestDTO netRequest = netResponseDTO.NetRequest;
            failureResponse.RequestType = netRequest.RequestType;
            failureResponse.ContainedObjectUniqueId = netRequest.ContainedObjectUniqueId;
            failureResponse.dataObject = netRequest;

            failureResponse.type = FailureResponseDTO.ERROR_TYPE_BUSINESS_EXCEPTION;
            failureResponse.message = "Failed to process : " + netRequest.RequestString;

            failureResponse.processingStats = netResponseDTO.NetRequest.Stats;

            result = failureResponse;
        }
        else {

            FSVenueListDTO _FSVenueListDTO = new FSVenueListDTO();
            _FSVenueListDTO.venus = _FSResultDTO.response.venues;

            NetResponseProcessingCompletedEvent event = new NetResponseProcessingCompletedEvent();
            event.mRequestType = netResponseDTO.NetRequest.RequestType;
            event.mContainedObjectUniqueId = netResponseDTO.NetRequest.ContainedObjectUniqueId;
            event.mProcessedResultDTO = _FSVenueListDTO;
            event.processingStats = netResponseDTO.NetRequest.Stats;

            result = event;
        }

        netResponseDTO.NetRequest.Stats.dataProcessingCompletedAt = System.currentTimeMillis();
        netResponseDTO.NetRequest.Stats.publishedAt = System.currentTimeMillis();
        PlatformFactoryLocator.getPlatformFactory().getEventBus().publish(result);
    }

    /**
     *
     * Process response as an async job, so that ISyncAdapter is freed to handle other requests.
     *
     * Execute as an ordered-concurrent background job. Any available background thread can execute
     * this, because the database transactions are disabled.
     */
    private class FSVenueSearchGatewayToDaoStorageAsyncJob extends BgAsyncJob {

        private FSVenueSearchGateway gateway;
        private NetResponseDTO response;
        private FSResultDTO result;

        FSVenueSearchGatewayToDaoStorageAsyncJob(FSVenueSearchGateway gateway,
                NetResponseDTO response, FSResultDTO result) {

            this.gateway = gateway;
            this.response = response;
            this.result = result;
        }

        @Override
        public void execute() {
            try {
                gateway.onDataContractSatisfiedInternal(response, result);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////  GET PARAM BUILDER  ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private String generateParams(IGenericMap<String, String> params) {
        IGenericList<String> keys = params.getKeys();
        IStringBuilder builder = PlatformFactoryLocator.getPlatformFactory().newStringBuilder();
        int count = keys.size();
        int lastIndex = count - 1;
        int atIndex = 0;

        for(String key : keys) {
            builder.appendString(key).appendString("=").appendString(params.get(key));
            if (atIndex++ < lastIndex) {
                builder.appendString("&");
            }
        }

        return builder.stringify();
    }
}
