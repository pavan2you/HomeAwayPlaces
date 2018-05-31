package com.homeaway.foursqureplaces.sync.dtos;

import com.jvanila.core.IStringUtils;
import com.jvanila.core.PlatformFactoryLocator;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class FSLocationDTO extends DataObject {

    public static final String CLASS_NAME = FSLocationDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public String address;
    public String crossStreet;
    public double lat;
    public double lng;
    public int distance;
    public int postalCode;
    public String cc;
    public String city;
    public String state;
    public String country;
    public IGenericList<String> formattedAddress;

    public String entity_uuid;

    public FSLocationDTO() {
        CRUDOperation = "U";
    }

    public void setCSVRepresentationOfformattedAddress(
            String csvformattedAddress, String delimiter) {
        if (csvformattedAddress != null) {
            IStringUtils stringUtils = PlatformFactoryLocator.getPlatformFactory().getStringUtils();
            formattedAddress = stringUtils.splitXWithY(csvformattedAddress,
                    delimiter);
        }
    }

    public String getCSVRepresentationOfformattedAddress(String delimiter) {
        IStringUtils stringUtils = PlatformFactoryLocator.getPlatformFactory().getStringUtils();
        return stringUtils.toCSVString(formattedAddress, delimiter);
    }
}
