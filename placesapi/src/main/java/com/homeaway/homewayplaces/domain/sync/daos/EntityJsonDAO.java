package com.homeaway.homewayplaces.domain.sync.daos;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.homeaway.homewayplaces.domain.sync.dtos.EntityJsonDTO;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.droid.sync.daos.SQLiteQuery;
import com.jvanila.droid.sync.daos.SQLiteTableInfo;
import com.jvanila.droid.sync.daos.TypedDAO;
import com.jvanila.mobile.MobilePlatformFactoryLocator;
import com.jvanila.mobile.json.JSONUtils;
import com.jvanila.mobile.storage.ISqliteAccessor;

/**
 * Created by pavan on 28/05/18.
 *
 * Supports a raw json CRUD in/out database.
 */
public class EntityJsonDAO extends TypedDAO<EntityJsonDTO> {

	private static final long serialVersionUID = 1L;

    private static class EntityJsonDTOTableInfo extends SQLiteTableInfo<EntityJsonDTO> {

        EntityJsonDTOTableInfo(String name, String primaryKey, String foreignKey,
                String[] compositePrimaryKey, String[] compositeForeignKey) {

            super(name, primaryKey, foreignKey, compositePrimaryKey, compositeForeignKey);
        }

        @Override
        public String getValueOf(EntityJsonDTO dto, String key) {
            if (key.equals(foreignKey)) {
                return dto.entity_group;
            }
            return null;
        }

        @Override
        public String[] getCompositeValueOf(EntityJsonDTO dto, String[] compositeKey) {
            String[] values = new String[compositeKey.length];
            int i = 0;
            for (String key : compositeKey) {
                if (key.equals(foreignKey)) {
                    values[i++] = dto.entity_group;
                }
                else if (key.equals("entity_uuid")) {
                    values[i++] = dto.entity_uuid;
                }
            }

            return values;
        }
    }

    public EntityJsonDAO(ISqliteAccessor database) {
        super(new EntityJsonDTOTableInfo("EntityJson", null,
                        "entity_group", null, null),
                database);
    }

    public int createRecord(EntityJsonDTO dto) {
        if (isDebug()) {
            System.out.println("DAOThreadDebug : EntityJsonDAO -> createRecord" +
                    " from " + Thread.currentThread().getName());
        }

		if (dto.CRUDOperation.equals("D")) {
			return -1;
		}

		String query = "INSERT INTO EntityJson (entity_uuid, entity_group, " +
				"entity_class, entity_json, sort_by) VALUES (?, ?, ?, ?, ?);";
        SQLiteStatement statement = mDatabase.compileStatement(query);

		if (dto.entity_uuid != null) {
			statement.bindString(1, dto.entity_uuid);
		} else {
			statement.bindNull(1);
		}
		if (dto.entity_group != null) {
			statement.bindString(2, dto.entity_group);
		} else {
			statement.bindNull(2);
		}
        if (dto.entity_class != null) {
            statement.bindString(3, dto.entity_class);
        } else {
            statement.bindNull(3);
        }
        if (dto.entity_json != null) {
            statement.bindString(4, dto.entity_json);
        } else {
            statement.bindNull(4);
        }
        statement.bindLong(5, dto.sort_by);

        int id = -1;
        try {
            id = (int) statement.executeInsert();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            statement.clearBindings();
            statement.close();
        }

        if (id > 0) {
            MobilePlatformFactoryLocator.getMobilePlatformFactory().getEventBus().publish(dto);
        }

		return id;
	}

    public int updateRecord(EntityJsonDTO dto) {
        if (isDebug()) {
            System.out.println("DAOThreadDebug : EntityJsonDAO -> updateRecord from "
                    + Thread.currentThread().getName());
        }

        String _entity_uuid = dto.entity_uuid;
        String _entity_group = dto.entity_group;
        deleteEntityJsonByIdAndPublish(_entity_uuid, _entity_group, false);
        return createRecord(dto);
    }

    @Override
    public EntityJsonDTO toDataObject(Cursor cursor) {
        EntityJsonDTO dto = new EntityJsonDTO();
        dto.entity_uuid = cursor.getString(cursor.getColumnIndex("entity_uuid"));
        dto.entity_group = cursor.getString(cursor.getColumnIndex("entity_group"));
        dto.entity_class = cursor.getString(cursor.getColumnIndex("entity_class"));
        dto.entity_json = cursor.getString(cursor.getColumnIndex("entity_json"));
        dto.sort_by = cursor.getLong(cursor.getColumnIndex("sort_by"));

        enrichEntity(dto);

        return dto;
    }

    private void enrichEntity(EntityJsonDTO dto) {
        try {
            dto.entity = JSONUtils.toDataObject(dto.entity_json, dto.entity_class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteRecordBy(String entityUuid, String entityGroup) {
        return deleteEntityJsonByIdAndPublish(entityUuid, entityGroup, true);
    }

    public boolean deleteEntityJsonByIdAndPublish(String entity_uuid, String entity_group,
            boolean isPublishable) {

        if (isDebug()) {
            System.out.println("DAOThreadDebug : EntityJsonDAO -> " +
                    "deleteEntityJsonByIdAndPublish from " + Thread.currentThread().getName());
        }

        String query = "DELETE FROM EntityJson WHERE entity_uuid =? AND entity_group =?";
        String[] params = new String[] {String.valueOf(entity_uuid), String.valueOf(entity_group)};

        return new SQLiteQuery<>(mDatabase, query, params, this).execute();
    }

    public boolean deleteRecordBy(String entity_group) {
        if (isDebug()) {
            System.out.println("DAOThreadDebug : EntityJsonDAO -> " +
                    "deleteEntityJsonByIdAndPublish from " + Thread.currentThread().getName());
        }

        String query = "DELETE FROM EntityJson WHERE entity_group =?";
        String[] params = new String[] {String.valueOf(entity_group)};

        return new SQLiteQuery<>(mDatabase, query, params, this).execute();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

	public IGenericList<EntityJsonDTO> getAllRecords() {
        if (isDebug()) {
            System.out.println("DAOThreadDebug : " + mTableInfo.name + "DAO -> " +
                    "getAllRecords "
                    + Thread.currentThread().getName());
        }

        String query = "SELECT * FROM EntityJson ORDER BY sort_by ASC";

        return new SQLiteQuery<>(mDatabase, query, null, this).getRecordList();
	}

    public IGenericList<EntityJsonDTO> getRecordListByEntityGroup(String entity_group) {
        return super.getRecordListByColumn("entity_group", entity_group);
    }
}

