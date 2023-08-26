package com.bondarenko.template;


import com.bondarenko.PrepareStatementExecutor;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcTemplateTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private PrepareStatementExecutor executor;

    @Mock
    private RowMapper<Object> rowMapper;

    @Mock
    private ResultSet resultSet;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private Connection connection;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void shouldQueryAndReturnObject() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(executor.execute(preparedStatement)).thenReturn(resultSet);
        when(rowMapper.map(resultSet)).thenReturn(new Object());

        Object result = jdbcTemplate.query("SELECT * FROM table WHERE id = ?", rowMapper);

        assertNotNull(result);
    }

    @Test
    void shouldThrowExceptionForQueryWhenSQLExceptionOccurs() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> {
            jdbcTemplate.query("SELECT * FROM table WHERE id = ?", rowMapper);
        });
    }

    @Test
    void shouldQueryForObjectAndReturnObject() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(executor.execute(preparedStatement)).thenReturn(resultSet);
        when(rowMapper.map(resultSet)).thenReturn(new Object());

        Object result = jdbcTemplate.queryForObject("SELECT * FROM table WHERE id = ?", rowMapper, 1);

        assertNotNull(result);
    }

    @Test
    void shouldThrowExceptionForQueryForObjectWhenSQLExceptionOccurs() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> {
            jdbcTemplate.queryForObject("SELECT * FROM table WHERE id = ?", rowMapper, 1);
        });
    }

    @Test
    void shouldUpdateAndReturnRowCount() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        int updatedRows = jdbcTemplate.update("UPDATE table SET name = ? WHERE id = ?", "name", 1);

        assertEquals(1, updatedRows);
    }

    @Test
    void shouldThrowExceptionForUpdateWhenSQLExceptionOccurs() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> {
            jdbcTemplate.update("UPDATE table SET name = ? WHERE id = ?", "name", 1);
        });
    }
}