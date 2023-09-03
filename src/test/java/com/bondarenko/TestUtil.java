package com.bondarenko;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TestUtil {
    public static TestEntity getTestEntity(int id, String name) {
        TestEntity entityFirst = new TestEntity();
        entityFirst.setId(id);
        entityFirst.setName(name);
        return entityFirst;
    }

    public static TestEntity getTestEntityByResultSet(ResultSet resultSet) throws SQLException {
        TestEntity entity = new TestEntity();
        entity.setId(resultSet.getInt("id"));
        entity.setName(resultSet.getString("name"));
        return entity;
    }

    public static Map<String, Object> getStringObjectMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "NewName");
        params.put("id", 1);
        return params;
    }

    public static JdbcDataSource getJdbcDataSource() {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("sa");
        return h2DataSource;
    }
}
