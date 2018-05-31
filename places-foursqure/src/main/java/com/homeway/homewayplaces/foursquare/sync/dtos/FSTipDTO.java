package com.homeaway.foursqureplaces.sync.dtos;

import com.jvanila.core.io.DataObject;

/**
 *
 * Created by pavan on 27/05/18.
 *
 * Sample json
 * {
 *  "createdAt": 1468075940,
 *    "text": "The iced coffee is no longer an iced red eye, it's cold brew. If you want the red eye ask for it; it's the same price.",
 *    "type": "user",
 *    "canonicalUrl": "https://foursquare.com/item/57810fa4498e1542398875fa",
 *    "likes": {
 *    "count": 1,
 *    "groups": [],
 *     "summary": "1 like"
 *     },
 *     "agreeCount": 3,
 *     "user": {
 *    }
 *    }
 *
 */
public class FSTipDTO extends DataObject {

    public static final String CLASS_NAME = FSTipDTO.class.getName();
    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

     public long createdAt;
     public String text;
     public String type;//"user"
     public String canonicalUrl;

    public FSTipDTO() {
        CRUDOperation = "U";
    }
}
