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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NamedParameterJdbcTemplateITest {
    private DataSource dataSource = TestUtil.getJdbcDataSource();
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @BeforeEach
    public void setUp() throws SQLException {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        TestUtil.createTestTable(dataSource);
    }

    @Test
    @DisplayName("Should return correct entities in query")
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

    @Test
    @DisplayName("Should return correct entities in queryForObject")
    public void queryForObject_ShouldReturnCorrectEntity() {
        RowMapper<TestEntity> rowMapper = TestUtil::getTestEntityByResultSet;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", 1);

        TestEntity entity = namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM test_table WHERE id = :id",
                paramMap,
                rowMapper
        );

        assertNotNull(entity);
        assertEquals(1, entity.getId());
        assertEquals("Entity1", entity.getName());
    }

    @Test
    @DisplayName("Should update entity")
    public void update_ShouldUpdateEntity() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", "UpdatedEntity");
        paramMap.put("id", 1);

        int updatedRows = namedParameterJdbcTemplate.update(
                "UPDATE test_table SET name = :name WHERE id = :id",
                paramMap
        );

        assertEquals(1, updatedRows);

        RowMapper<TestEntity> rowMapper = TestUtil::getTestEntityByResultSet;
        Map<String, Object> selectParamMap = new HashMap<>();
        selectParamMap.put("id", 1);

        TestEntity updatedEntity = namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM test_table WHERE id = :id",
                selectParamMap,
                rowMapper
        );

        assertNotNull(updatedEntity);
        assertEquals("UpdatedEntity", updatedEntity.getName());
    }

    @AfterEach
    public void tearDown() throws SQLException {
        TestUtil.dropTestTable(dataSource);
    }

}
