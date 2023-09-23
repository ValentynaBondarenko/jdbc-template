package com.bondarenko.template;

import com.bondarenko.TestEntity;
import com.bondarenko.TestUtil;
import com.bondarenko.mapper.RowMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NamedParameterJdbcTemplateITest {
    private DataSource dataSource = TestUtil.getJdbcDataSource();

    private NamedParameterJdbcTemplate<TestEntity> namedParameterJdbcTemplate;

    @BeforeEach
    public void setUp() throws SQLException {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate<>(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(255));")) {
                statement.execute();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO test_table (id, name) VALUES (?, ?);")) {
                statement.setInt(1, 1);
                statement.setString(2, "Entity1");
                statement.execute();

                statement.setInt(1, 2);
                statement.setString(2, "Entity2");
                statement.execute();
            }
        }
    }

    @DisplayName("Should return correct entities in query")
    @Test
    public void query_ShouldReturnCorrectEntities() {
        RowMapper<TestEntity> rowMapper = TestUtil::getTestEntityByResultSet;
        List<TestEntity> entities = namedParameterJdbcTemplate.query("SELECT id, name FROM test_table", rowMapper);

        assertNotNull(entities);
        assertEquals(2, entities.size());
        assertEquals(1, entities.get(0).getId());
        assertEquals("Entity1", entities.get(0).getName());
        assertEquals(2, entities.get(1).getId());
        assertEquals("Entity2", entities.get(1).getName());
    }

    @DisplayName("Should return correct entities in queryForObject")
    @Test
    public void queryForObject_ShouldReturnCorrectEntity() {
        RowMapper<TestEntity> rowMapper = TestUtil::getTestEntityByResultSet;
        TestEntity entity = namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM test_table WHERE id = :id",
                rowMapper,
                "id", 1
        );

        assertNotNull(entity);
        assertEquals(1, entity.getId());
        assertEquals("Entity1", entity.getName());
    }

    @DisplayName("Should update entity")
    @Test
    public void update_ShouldUpdateEntity() {
        int updatedRows = namedParameterJdbcTemplate.update(
                "UPDATE test_table SET name = :name WHERE id = :id",
                "name", "UpdatedEntity",
                "id", 1
        );

        assertEquals(1, updatedRows);

        RowMapper<TestEntity> rowMapper = TestUtil::getTestEntityByResultSet;
        TestEntity updatedEntity = namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM test_table WHERE id = :id",
                rowMapper,
                "id", 1
        );

        assertNotNull(updatedEntity);
        assertEquals("UpdatedEntity", updatedEntity.getName());
    }


    @AfterEach
    public void down() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DROP TABLE test_table;")) {
            statement.execute();
        }
    }
}
