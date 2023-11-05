package com.bondarenko.template;

import com.bondarenko.TestEntity;
import com.bondarenko.TestUtil;
import com.bondarenko.exception.DataAccessException;
import com.bondarenko.mapper.RowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NamedParameterJdbcTemplateTest {
    @Mock
    private DataSource dataSource;
    @Mock
    private Map<String, ?> mapper;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @BeforeEach
    public void setUp() {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @DisplayName("Should Query And Map ResultSet To List")
    @Test
    void shouldQueryAndMapResultSetToList() throws SQLException {
        // Given
        String sql = "SELECT id, name FROM table";
        TestEntity expectedEntity = TestUtil.getTestEntity(1, "Test Name");
        RowMapper<TestEntity> rowMapper = resultSet -> expectedEntity;

        // Mock Behavior
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        // When
        List<TestEntity> result = namedParameterJdbcTemplate.query(sql, rowMapper);

        // Then
        verify(preparedStatement, times(1)).executeQuery();
        assertEquals(1, result.size());
        assertEquals(expectedEntity, result.get(0));
    }

    @DisplayName("Should throw SQLException when connection to the database fails")
    @Test
    void shouldThrowException_When_GetConnectionFails() throws SQLException {
        //Mock Behavior
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));
        TestEntity expectedEntity = TestUtil.getTestEntity(1, "Test Name");
        RowMapper<TestEntity> rowMapper = resultSet -> expectedEntity;

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            namedParameterJdbcTemplate.query("SELECT id, name FROM table", rowMapper);
        });

        assertEquals("Connection failed", exception.getCause().getMessage());
    }

    @DisplayName("Should throw IllegalArgumentException when RowMapper is null")
    @Test
    void shouldThrowException_When_RowMapperIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            namedParameterJdbcTemplate.query("SELECT id, name FROM table", null);
        });

        assertEquals("RowMapper should not be null.", exception.getMessage());
    }

    @DisplayName("Should close PreparedStatement when query completes")
    @Test
    void shouldClosePreparedStatement_When_QueryCompletes() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(resultSet.next()).thenReturn(false);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        TestEntity expectedEntity = TestUtil.getTestEntity(1, "Test Name");
        RowMapper<TestEntity> rowMapper = resultSet -> expectedEntity;

        namedParameterJdbcTemplate.query("SELECT id, name FROM table", rowMapper);

        verify(preparedStatement, times(1)).close();
    }

    @DisplayName("Should Throw Exception When Getting Connection Fails For queryForObject")
    @Test
    void shouldThrowExceptionWhenGettingConnectionFails_ForQueryForObject() throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        TestEntity expectedEntity = TestUtil.getTestEntity(1, "Test Name");
        RowMapper<TestEntity> rowMapper = resultSet -> expectedEntity;
        when(dataSource.getConnection()).thenThrow(new SQLException());

        assertThrows(RuntimeException.class, () -> {
            namedParameterJdbcTemplate.queryForObject("SELECT  id, name  FROM table WHERE id = :id", params, rowMapper);
        });
    }

    @DisplayName("Should Throw Exception When Sql Is Null for queryForObject")
    @Test
    void shouldThrowExceptionWhenSqlIsNull_ForQueryForObject() {
        TestEntity expectedEntity = TestUtil.getTestEntity(1, "Test Name");
        RowMapper<TestEntity> rowMapper = resultSet -> expectedEntity;
        assertThrows(IllegalArgumentException.class, () -> {
            namedParameterJdbcTemplate.queryForObject(null, mapper, rowMapper);
        });
    }

    @DisplayName("Should Throw Exception When RowMapper Is Null For QueryForObject Operation")
    @Test
    void shouldThrowExceptionWhenRowMapperIsNull_ForQueryForObject() {
        // Given
        String sql = "SELECT id, name FROM table WHERE id = :id";
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);

        // Then
        assertThrows(IllegalArgumentException.class, () -> {
            // When
            namedParameterJdbcTemplate.queryForObject(sql, params, null);
        });
    }

    @DisplayName("Should Throw Exception When Getting Connection Fails For Update Operation")
    @Test
    void shouldThrowExceptionWhenGettingConnectionFails_ForUpdate() throws SQLException {
        // Given
        String sql = "UPDATE table SET name = :name WHERE id = :id";
        Map<String, Object> params = TestUtil.getStringObjectMap();

        // Mock behavior
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            // When
            namedParameterJdbcTemplate.update(sql, params);
        });

        assertEquals("Connection failed", exception.getCause().getMessage());

        verifyNoInteractions(preparedStatement);
    }

    @DisplayName("Should throw IllegalArgumentException when SQL query is null for update")
    @Test
    void shouldThrowExceptionWhenSqlIsNull_ForUpdate() {
        assertThrows(IllegalArgumentException.class, () -> {
            namedParameterJdbcTemplate.update(null, Collections.emptyMap());
        });
    }
}