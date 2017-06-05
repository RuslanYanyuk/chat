package com.test.utils;

import com.test.utils.yaml.YamlDataSetLoader;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;

/**
 * @author Ruslan Yaniuk
 * @date September 2015
 */
@Component
public class DBUnitHelper {

    public static final String DBUNIT_DATASETS = "/dbunit-datasets/";

    @Autowired
    private DataSource dataSource;

    private IDatabaseConnection dbUnitCon;

    private IDataSet users;
    private IDataSet userContacts;
    private IDataSet chatRooms;
    private IDataSet chatRoomsToUsers;

    @PostConstruct
    private void init() throws DatabaseUnitException, IOException {
        Connection con = DataSourceUtils.getConnection(dataSource);

        dbUnitCon = new DatabaseConnection(con);
        dbUnitCon.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());

        users = YamlDataSetLoader.load(DBUNIT_DATASETS + "users.yml");
        userContacts = YamlDataSetLoader.load(DBUNIT_DATASETS + "user_contacts.yml");
        chatRooms = YamlDataSetLoader.load(DBUNIT_DATASETS + "chat_rooms.yml");
        chatRoomsToUsers = YamlDataSetLoader.load(DBUNIT_DATASETS + "chat_rooms_to_users.yml");
    }

    public DBUnitHelper insertUsers() {
        try {
            DatabaseOperation.INSERT.execute(dbUnitCon, users);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public DBUnitHelper insertUserContacts() {
        try {
            DatabaseOperation.INSERT.execute(dbUnitCon, userContacts);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public DBUnitHelper insertChatRooms() {
        try {
            DatabaseOperation.INSERT.execute(dbUnitCon, chatRooms);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public DBUnitHelper insertChatRoomsToUsers() {
        try {
            DatabaseOperation.INSERT.execute(dbUnitCon, chatRoomsToUsers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public DBUnitHelper deleteAllFixtures() {
        try {
            DatabaseOperation.DELETE_ALL.execute(dbUnitCon, chatRoomsToUsers);
            DatabaseOperation.DELETE_ALL.execute(dbUnitCon, chatRooms);
            DatabaseOperation.DELETE_ALL.execute(dbUnitCon, userContacts);
            DatabaseOperation.DELETE_ALL.execute(dbUnitCon, users);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }
}
