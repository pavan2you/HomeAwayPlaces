package com.homeaway.homeawayplaces.sync.dtos;

import com.jvanila.core.io.DataObject;

/**
 * Created by pavan on 28/05/18.
 *
 */
public class EntityJsonDTO extends DataObject {

    public static final String CLASS_NAME = EntityJsonDTO.class.getName();

    private static final long serialVersionUID = 1L;

    public String CRUDOperation;

    public String entity_uuid;
    public String entity_group;
    public String entity_class;
    public String entity_json;
    public long sort_by;
    public DataObject entity;

    public EntityJsonDTO() {
        CRUDOperation = "U";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityJsonDTO that = (EntityJsonDTO) o;

        if (entity_uuid != null ? !entity_uuid.equals(that.entity_uuid) : that.entity_uuid != null)
            return false;
        return entity_group != null ? entity_group.equals(that.entity_group) : that.entity_group == null;
    }

    @Override
    public int hashCode() {
        int result = entity_uuid != null ? entity_uuid.hashCode() : 0;
        result = 31 * result + (entity_group != null ? entity_group.hashCode() : 0);
        return result;
    }
}
