package com.bondarenko.template;

import com.bondarenko.TestEntity;
import com.bondarenko.TestUtil;
import com.bondarenko.mapper.RowMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JdbcTemplateITest {
    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource = TestUtil.getJdbcDataSource();

    @BeforeEach
    public void setUp() throws SQLException {
        jdbcTemplate = new JdbcTemplate(dataSource);
        TestUtil.createTestTable(dataSource);
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
    public void update_ShouldUpdateEntity() {
        int updatedRows = jdbcTemplate.update("UPDATE test_table SET name = ? WHERE id = ?", "UpdatedEntity", 1);

        assertEquals(1, updatedRows);

        RowMapper<TestEntity> rowMapper = TestUtil::getTestEntityByResultSet;
        TestEntity updatedEntity = jdbcTemplate.queryForObject("SELECT id, name FROM test_table WHERE id = ?", rowMapper, 1);

        assertEquals("UpdatedEntity", updatedEntity.getName());
    }

    @AfterEach
    public void tearDown() throws SQLException {
        TestUtil.dropTestTable(dataSource);
    }

}