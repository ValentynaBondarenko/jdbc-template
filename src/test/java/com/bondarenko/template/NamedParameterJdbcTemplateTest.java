package com.bondarenko.template;

import com.bondarenko.TestEntity;
import com.bondarenko.TestUtil;
import com.bondarenko.mapper.ResultSetMapper;
import com.bondarenko.mapper.RowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
class NamedParameterJdbcTemplateTest<T> {
    @Mock(lenient = true)
    private DataSource dataSource;
    @Mock
    private ResultSetMapper<T> resultSetMapper;
    @Mock
    private RowMapper<TestEntity> mapper;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    private NamedParameterJdbcTemplate<TestEntity> namedParameterJdbcTemplate;

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

        // Mock Behavior
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(mapper.map(resultSet)).thenReturn(expectedEntity);

        // When
        List<TestEntity> result = namedParameterJdbcTemplate.query(sql, mapper);

        // Then
        verify(preparedStatement, times(1)).executeQuery();
        verify(mapper, times(1)).map(resultSet);
        assertEquals(1, result.size());
        assertEquals(expectedEntity, result.get(0));
    }

    @DisplayName("Should throw SQLException when connection to the database is fails")
    @Test
    void shouldThrowException_When_GetConnectionFails() throws SQLException {
        //Mock Behavior
        when(dataSource.getConnection()).thenThrow(new RuntimeException("Connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            namedParameterJdbcTemplate.query("SELECT id, name FROM table", mapper);
        });

        assertEquals("Connection failed", exception.getMessage());
    }

    @DisplayName("Should throw IllegalArgumentException when RowMapper is null")
    @Test
    void shouldThrowException_When_RowMapperIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            namedParameterJdbcTemplate.query("SELECT id, name FROM table", null);
        });

        assertEquals("RowMapper should not be null", exception.getMessage());
    }

    @DisplayName("Should close PreparedStatement when query completes")
    @Test
    void shouldClosePreparedStatement_When_QueryCompletes() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(resultSet.next()).thenReturn(false);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        namedParameterJdbcTemplate.query("SELECT id, name FROM table", mapper);

        verify(preparedStatement, times(1)).close();
    }

    @DisplayName("Should throw RuntimeException when RowMapper throws exception")
    @Test
    void shouldThrowException_When_RowMapperThrowsException() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(mapper.map(resultSet)).thenThrow(new SQLException());

        assertThrows(RuntimeException.class, () -> {
            namedParameterJdbcTemplate.query("SELECT id, name FROM table", mapper);
        });

        verify(mapper, times(1)).map(resultSet);
    }


    @DisplayName("Should Throw Exception When Getting Connection Fails For queryForObject")
    @Test
    void shouldThrowExceptionWhenGettingConnectionFails_ForQueryForObject() throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);

        when(dataSource.getConnection()).thenThrow(new SQLException());

        assertThrows(RuntimeException.class, () -> {
            namedParameterJdbcTemplate.queryForObject("SELECT  id, name  FROM table WHERE id = :id", mapper, params);
        });
    }

    @DisplayName("Should Throw Exception When Sql Is Null for queryForObject")
    @Test
    void shouldThrowExceptionWhenSqlIsNull_ForQueryForObject() {
        assertThrows(IllegalArgumentException.class, () -> {
            namedParameterJdbcTemplate.queryForObject(null, mapper, 1);
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
            namedParameterJdbcTemplate.queryForObject(sql, null, params);
        });
    }


    @DisplayName("Should Throw Exception When RowMapper Throws Exception For QueryForObject Operation")
    @Test
    void shouldThrowExceptionWhenRowMapperThrowsException_ForQueryForObject() throws SQLException {
        // Given
        String sql = "SELECT id, name FROM table WHERE id = :id";

        // Mock behavior
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(mapper.map(resultSet)).thenThrow(new SQLException());

        // Then
        assertThrows(RuntimeException.class, () -> {
            // When
            namedParameterJdbcTemplate.queryForObject(sql, mapper, "id", 1);
        });

        verify(mapper, times(1)).map(resultSet);
        verify(preparedStatement, times(1)).setObject(1, 1);
    }

    @DisplayName("Should Throw Exception When Getting Connection Fails For Update Operation")
    @Test
    void shouldThrowExceptionWhenGettingConnectionFails_ForUpdate() throws SQLException {
        // Given
        String sql = "UPDATE table SET name = :name WHERE id = :id";
        Map<String, Object> params = TestUtil.getStringObjectMap();

        // Mock behavior
        when(dataSource.getConnection()).thenThrow(new SQLException());

        // Then
        assertThrows(RuntimeException.class, () -> {
            // When
            namedParameterJdbcTemplate.update(sql, params);
        });

        verifyNoInteractions(preparedStatement);
    }

    @DisplayName("Should throw IllegalArgumentException when SQL query is null for queryForObject")
    @Test
    void shouldThrowExceptionWhenSqlIsNull_ForUpdate() {
        assertThrows(IllegalArgumentException.class, () -> {
            namedParameterJdbcTemplate.update(null, "NewName", 1);
        });
    }

    @Test
    void shouldThrowExceptionWhenExecuteUpdateThrowsException() throws SQLException {
        // Given
        String sql = "UPDATE table SET name = :name WHERE id = :id";
        Map<String, Object> paramMap = TestUtil.getStringObjectMap();

        // Convert the map to a sequential array
        Object[] params = new Object[paramMap.size() * 2];
        int index = 0;
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            params[index++] = entry.getKey();
            params[index++] = entry.getValue();
        }

        // Mock behavior
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        doThrow(new SQLException()).when(preparedStatement).executeUpdate();

        // Then
        assertThrows(RuntimeException.class, () -> {
            // When
            namedParameterJdbcTemplate.update(sql, params);
        });

        verify(preparedStatement, times(1)).executeUpdate();
    }

    @DisplayName("Should Query For Object Using TestEntity And Parameters")
    @Test
    void shouldQueryForObjectUsingTestEntityAndParameters() throws SQLException {
        String sql = "SELECT  id, name  FROM table WHERE id = :id";
        TestEntity expectedEntity = TestUtil.getTestEntity(1, "TestName");

        // Mock behavior
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(mapper.map(resultSet)).thenReturn(expectedEntity);

        // When
        TestEntity result = namedParameterJdbcTemplate.queryForObject(sql, mapper, "id", 1);

        // Then
        verify(preparedStatement, times(1)).setObject(1, 1);
        assertEquals(expectedEntity, result);
        verify(resultSet, times(1)).next();
    }

}