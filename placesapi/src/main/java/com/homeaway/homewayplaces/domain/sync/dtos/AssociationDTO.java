package com.homeaway.homewayplaces.domain.sync.dtos;

import com.jvanila.core.exception.VanilaException;
import com.jvanila.core.io.DataObject;
import com.jvanila.core.objectflavor.ICloneableObject;

/**
 * Created by pavan on 28/05/18.
 *
 */
public class AssociationDTO extends DataObject implements ICloneableObject {

    public static final String CLASS_NAME = AssociationDTO.class.getName();

    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public String lhs_uuid;
    public String rhs_uuid;

    public AssociationDTO(){
        CRUDOperation = "U";
    }

    @Override
    public ICloneableObject deepClone() throws VanilaException {
        AssociationDTO that = new AssociationDTO();
        that.lhs_uuid = this.lhs_uuid;
        that.rhs_uuid = this.rhs_uuid;
        return that;
    }
}
