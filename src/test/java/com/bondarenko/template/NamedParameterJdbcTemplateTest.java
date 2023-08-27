package com.bondarenko.template;

import com.bondarenko.PrepareStatementExecutor;
import com.bondarenko.TestEntity;
import com.bondarenko.mapper.ResultSetMapper;
import com.bondarenko.mapper.RowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NamedParameterJdbcTemplateTest {
    @Mock
    private DataSource dataSource;
    @Mock
    private PrepareStatementExecutor executor;
    @Mock
    private ResultSetMapper rowMapper;
    @Mock
    private RowMapper<Object> mapper;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Test
    void shouldQueryAndMapResultSet() throws SQLException {
        String sql = "SELECT * FROM table";
        Object expectedObject = new Object();

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(executor.execute(preparedStatement)).thenReturn(resultSet);
        when(rowMapper.mapResultSetToEntity(resultSet, mapper)).thenReturn(expectedObject);

        TestEntity result = (TestEntity) jdbcTemplate.query(sql, mapper);

        verify(executor, times(1)).execute(preparedStatement);
        verify(rowMapper, times(1)).mapResultSetToEntity(resultSet, mapper);
        assertEquals(expectedObject, result);
    }

    @Test
    void shouldThrowException_When_GetConnectionFails() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> {
            jdbcTemplate.query("SELECT * FROM table", mapper);
        });
    }

    @Test
    void shouldThrowException_When_RowMapperIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            jdbcTemplate.query("SELECT * FROM table", null);
        });
    }

    @Test
    void shouldClosePreparedStatement_When_QueryCompletes() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(executor.execute(preparedStatement)).thenReturn(resultSet);

        jdbcTemplate.query("SELECT * FROM table", mapper);

        verify(preparedStatement, times(1)).close();
    }

    @Test
    void shouldThrowException_When_RowMapperThrowsException() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(executor.execute(preparedStatement)).thenReturn(resultSet);
        when(rowMapper.mapResultSetToEntity(resultSet, mapper)).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> {
            jdbcTemplate.query("SELECT * FROM table", mapper);
        });
    }

    @Test
    void shouldQueryForObjectAndMapResultSet() throws SQLException {
        String sql = "SELECT * FROM table WHERE id = :id";
        Object expectedObject = new Object();
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);


        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(executor.execute(preparedStatement)).thenReturn(resultSet);
        when(rowMapper.mapResultSetToEntity(resultSet, mapper)).thenReturn(expectedObject);

        TestEntity result = (TestEntity) jdbcTemplate.queryForObject(sql, mapper, params);

        verify(executor, times(1)).execute(preparedStatement);
        verify(preparedStatement, times(1)).setObject(1, 1);
        verify(rowMapper, times(1)).mapResultSetToEntity(resultSet, mapper);
        assertEquals(expectedObject, result);
    }

    @Test
    void shouldThrowExceptionWhenGettingConnectionFails_ForQueryForObject() throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);

        when(dataSource.getConnection()).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> {
            jdbcTemplate.queryForObject("SELECT * FROM table WHERE id = :id", mapper, params);
        });
    }

    @Test
    void shouldThrowExceptionWhenSqlIsNull_ForQueryForObject() {
        assertThrows(IllegalArgumentException.class, () -> {
            jdbcTemplate.queryForObject(null, mapper, 1);
        });
    }

    @Test
    void shouldThrowExceptionWhenRowMapperIsNull_ForQueryForObject() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);

        assertThrows(IllegalArgumentException.class, () -> {
            jdbcTemplate.queryForObject("SELECT * FROM table WHERE id = :id", null, params);
        });
    }


    @Test
    void shouldThrowExceptionWhenRowMapperThrowsException_ForQueryForObject() throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(executor.execute(preparedStatement)).thenReturn(resultSet);
        when(rowMapper.mapResultSetToEntity(resultSet, mapper)).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> {
            jdbcTemplate.queryForObject("SELECT * FROM table WHERE id = :id", mapper, params);
        });
    }

    @Test
    void shouldThrowExceptionWhenGettingConnectionFails_ForUpdate() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException());

        Map<String, Object> params = getStringObjectMap();
        String sql = "UPDATE table SET name = :name WHERE id = :id";

        assertThrows(SQLException.class, () -> {
            jdbcTemplate.update(sql, params);
        });
    }

    @Test
    void shouldThrowExceptionWhenSqlIsNull_ForUpdate() {
        assertThrows(IllegalArgumentException.class, () -> {
            jdbcTemplate.update(null, "NewName", 1);
        });
    }

    @Test
    void shouldThrowExceptionWhenExecuteUpdateThrowsException() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException());

        Map<String, Object> params = getStringObjectMap();

        String sql = "UPDATE table SET name = :name WHERE id = :id";

        assertThrows(SQLException.class, () -> {
            jdbcTemplate.update(sql, params);
        });
    }

    @Test
    void shouldQueryAndMapResultSetToTestEntity() throws SQLException {
        String sql = "SELECT * FROM table";
        TestEntity expectedEntity = getTestEntity();

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(executor.execute(preparedStatement)).thenReturn(resultSet);
        when(rowMapper.mapResultSetToEntity(resultSet, mapper)).thenReturn(expectedEntity);

        TestEntity result = (TestEntity) jdbcTemplate.query(sql, mapper);

        verify(executor, times(1)).execute(preparedStatement);
        verify(rowMapper, times(1)).mapResultSetToEntity(resultSet, mapper);
        assertEquals(expectedEntity, result);
    }

    @Test
    void shouldQueryForObjectUsingTestEntityAndParameters() throws SQLException {
        String sql = "SELECT * FROM table WHERE id = :id";
        TestEntity expectedEntity = getTestEntity();
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(executor.execute(preparedStatement)).thenReturn(resultSet);
        when(rowMapper.mapResultSetToEntity(resultSet, mapper)).thenReturn(expectedEntity);

        TestEntity result = (TestEntity) jdbcTemplate.queryForObject(sql, mapper, params);

        verify(preparedStatement, times(1)).setObject(1, 1);
        assertEquals(expectedEntity, result);
    }

    private TestEntity getTestEntity() {
        TestEntity expectedEntity = new TestEntity();
        expectedEntity.setId(1);
        expectedEntity.setName("Name");
        return expectedEntity;
    }
    private Map<String, Object> getStringObjectMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "NewName");
        params.put("id", 1);
        return params;
    }

}