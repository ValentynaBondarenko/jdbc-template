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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcTemplateTest<T> {

    @Mock
    private DataSource dataSource;
    @Mock
    private RowMapper<TestEntity> rowMapper;
    @Mock
    private ResultSetMapper resultSetMapper;
    @Mock
    private ResultSet resultSet;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private Connection connection;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("Should Return Single Entity When Query Is Executed")
    @Test
    void shouldReturnSingleEntityWhenQueryIsExecuted() throws SQLException {
        TestEntity expectedEntity = TestUtil.getTestEntity(1, "Test");

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(rowMapper.map(resultSet)).thenReturn(expectedEntity);

        List<TestEntity> result = jdbcTemplate.query("SELECT id, name FROM table WHERE id = ?", rowMapper);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedEntity, result.get(0));
        assertSame(expectedEntity, result.get(0));
    }

    @DisplayName("Should Return Null When ResultSet Is Empty")
    @Test
    void shouldReturnNullWhenResultSetIsEmpty() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<TestEntity> result = jdbcTemplate.query("SELECT id, name FROM table WHERE id = ?", rowMapper);

        assertTrue(result.isEmpty());
    }

    @DisplayName("Should Map To TestEntity")
    @Test
    void shouldMapToTestEntity() throws SQLException {
        TestEntity testEntity = TestUtil.getTestEntity(1, "Test");

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(rowMapper.map(resultSet)).thenReturn(testEntity);

        List<TestEntity> result = jdbcTemplate.query("SELECT id, name FROM table WHERE id = ?", rowMapper);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEntity, result.get(0));
        assertEquals(1, result.get(0).getId());
        assertEquals("Test", result.get(0).getName());
    }

    @DisplayName("Should throw exception for query when SQL exception occurs")
    @Test
    void shouldThrowExceptionForQueryWhenSQLExceptionOccurs() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException());

        assertThrows(RuntimeException.class, () -> {
            jdbcTemplate.query("SELECT id, name FROM table WHERE id = ?", rowMapper);
        });
    }

    @DisplayName("Should Return Empty List When ResultSet Is Empty")
    @Test
    void shouldReturnEmptyListWhenResultSetIsEmpty() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<TestEntity> result = jdbcTemplate.query("SELECT id, name FROM table WHERE id = ?", rowMapper);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @DisplayName("Should Set PreparedStatement Parameters For Query For Object")
    @Test
    void shouldSetPreparedStatementParametersForQueryForObject() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
       // when(resultSetMapper.mapResultSetToEntity(resultSet, rowMapper)).thenReturn(new TestEntity());

        jdbcTemplate.queryForObject("SELECT id, name FROM table WHERE id = ?", rowMapper, 1);

        verify(preparedStatement).setObject(1, 1);
    }

    @DisplayName("Should Throw SQLException When QueryForObject Encounters a SQLException")
    @Test
    void shouldThrowExceptionForQueryForObjectWhenSQLExceptionOccurs() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException());

        assertThrows(RuntimeException.class, () -> {
            jdbcTemplate.queryForObject("SELECT id, name FROM table WHERE id = ?", rowMapper, 1);
        });
    }

    @DisplayName("Should Update Record and Return Affected Row Count")
    @Test
    void shouldUpdateAndReturnRowCount() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        int updatedRows = jdbcTemplate.update("UPDATE table SET name = ? WHERE id = ?", "name", 1);

        assertEquals(1, updatedRows);
    }

    @DisplayName("Should Throw Exception For Update When SQLException Occurs")
    @Test
    void shouldThrowExceptionForUpdateWhenSQLExceptionOccurs() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException());

        assertThrows(RuntimeException.class, () -> {
            jdbcTemplate.update("UPDATE table SET name = ? WHERE id = ?", "name", 1);
        });
    }

    @DisplayName("Should Set PreparedStatement Parameters For Update")
    @Test
    void shouldSetPreparedStatementParametersForUpdate() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        jdbcTemplate.update("UPDATE table SET name = ? WHERE id = ?", "NewName", 1);

        verify(preparedStatement).setObject(1, "NewName");
        verify(preparedStatement).setObject(2, 1);
    }
}