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
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NamedParameterJdbcTemplateTest<T> {
    @Mock
    private DataSource dataSource;
    @Mock
    private ResultSetMapper<TestEntity> rowMapper;
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
    void setUp() {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate<>(dataSource);
    }

    @DisplayName("Should Query And Map ResultSet To List")
    @Test
    void shouldQueryAndMapResultSetToList() throws SQLException {
        // Given
        String sql = "SELECT id, name FROM table";
        TestEntity expectedEntity = TestUtil.getTestEntity(1, "Test Name");

        // Setup Mock Behavior
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(rowMapper.mapResultSetToEntity(resultSet, mapper)).thenReturn(expectedEntity);

        // When
        List<TestEntity> result = namedParameterJdbcTemplate.query(sql, mapper);

        // Then
        verify(preparedStatement, times(1)).executeQuery();
        verify(rowMapper, times(1)).mapResultSetToEntity(resultSet, mapper);
        assertEquals(1, result.size());
        assertEquals(expectedEntity, result.get(0));
    }

    @DisplayName("Should throw SQLException when connection to the database is fails")
    @Test
    void shouldThrowException_When_GetConnectionFails() throws SQLException {
        // Setup Mock Behavior
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

        SQLException exception = assertThrows(SQLException.class, () -> {
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
        when(preparedStatement.execute()).thenReturn(true);

        namedParameterJdbcTemplate.query("SELECT id, name FROM table", mapper);

        verify(preparedStatement, times(1)).close();
    }

    @DisplayName("Should throw SQLException when RowMapper throws exception")
    @Test
    void shouldThrowException_When_RowMapperThrowsException() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(true);
        when(rowMapper.mapResultSetToEntity(resultSet, mapper)).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> {
            namedParameterJdbcTemplate.query("SELECT id, name FROM table", mapper);
        });
    }

    @DisplayName("Should Throw Exception When Getting Connection Fails For queryForObject")
    @Test
    void shouldThrowExceptionWhenGettingConnectionFails_ForQueryForObject() throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);

        when(dataSource.getConnection()).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> {
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
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);

        // Mock behavior
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(true);
        when(rowMapper.mapResultSetToEntity(resultSet, mapper)).thenThrow(new SQLException());

        // Then
        assertThrows(SQLException.class, () -> {
            // When
            namedParameterJdbcTemplate.queryForObject(sql, mapper, params);
        });

        verify(preparedStatement, times(1)).execute();
        verify(rowMapper, times(1)).mapResultSetToEntity(resultSet, mapper);
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
        assertThrows(SQLException.class, () -> {
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

    @DisplayName("Should Throw Exception When Execute Update Throws Exception")
    @Test
    void shouldThrowExceptionWhenExecuteUpdateThrowsException() throws SQLException {
        // Given
        String sql = "UPDATE table SET name = :name WHERE id = :id";
        Map<String, Object> params = TestUtil.getStringObjectMap();

        // Mock behavior
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException());

        // Then
        assertThrows(SQLException.class, () -> {
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
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(true);
        when(rowMapper.mapResultSetToEntity(resultSet, mapper)).thenReturn(expectedEntity);

        TestEntity result = namedParameterJdbcTemplate.queryForObject(sql, mapper, params);

        verify(preparedStatement, times(1)).setObject(1, 1);
        assertEquals(expectedEntity, result);
    }

}