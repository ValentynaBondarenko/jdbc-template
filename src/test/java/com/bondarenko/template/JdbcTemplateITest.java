package com.bondarenko.template;

import com.bondarenko.TestEntity;
import com.bondarenko.TestUtil;
import com.bondarenko.mapper.RowMapper;
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

public class JdbcTemplateITest {
    private JdbcTemplate<TestEntity> jdbcTemplate;

    @BeforeEach
    public void setUp() throws SQLException {
        DataSource dataSource = TestUtil.getJdbcDataSource();
        jdbcTemplate = new JdbcTemplate<>(dataSource);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE test_table (id INT PRIMARY KEY, name VARCHAR(255));" +
                             "INSERT INTO test_table (id, name) VALUES (1, 'Entity1');" +
                             "INSERT INTO test_table (id, name) VALUES (2, 'Entity2');"
             )) {
            statement.execute();
        }
    }

    @DisplayName("Should return correct entities in query")
    @Test
    public void query_ShouldReturnCorrectEntities() {
        RowMapper<TestEntity> rowMapper = TestUtil::getTestEntityByResultSet;

        List<TestEntity> entities = jdbcTemplate.query("SELECT id, name FROM test_table", rowMapper);

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

        TestEntity entity = jdbcTemplate.queryForObject("SELECT id, name FROM test_table WHERE id = ?", rowMapper, 1);

        assertNotNull(entity);
        assertEquals(1, entity.getId());
        assertEquals("Entity1", entity.getName());
    }

    @DisplayName("Should update entity")
    @Test
    public void update_ShouldUpdateEntity() throws SQLException {
        int updatedRows = jdbcTemplate.update("UPDATE test_table SET name = ? WHERE id = ?", "UpdatedEntity", 1);

        assertEquals(1, updatedRows);

        TestEntity updatedEntity = jdbcTemplate.queryForObject("SELECT id, name FROM test_table WHERE id = ?",
                TestUtil::getTestEntityByResultSet, 1);

        assertEquals("UpdatedEntity", updatedEntity.getName());
    }
}
