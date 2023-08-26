package com.bondarenko;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrepareStatementExecutorTest {
    @Mock
    private PreparedStatement statement;
    @Mock
    private ResultSet mockResultSet;
    private PrepareStatementExecutor executor;

    @BeforeEach
    void setUp() throws SQLException {
        executor = new PrepareStatementExecutor();
        when(statement.executeQuery()).thenReturn(mockResultSet);
    }

    @Test
    void shouldExecute_Then_ReturnResultSet() {
        //when
        ResultSet resultSet = executor.execute(statement);

        assertNotNull(resultSet);
        assertEquals(mockResultSet, resultSet);
    }

    @Test
    void shouldThrowException_When_ExecuteQueryFails() throws SQLException {
        when(statement.executeQuery()).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> {
            executor.execute(statement);
        });
    }

    @Test
    void shouldHandleNullPreparedStatement() {
        assertThrows(IllegalArgumentException.class, () -> {
            executor.execute(null);
        });
    }

    @Test
    void shouldClosePreparedStatement_When_Done() throws SQLException {
        executor.execute(statement);
        verify(statement, times(1)).close();
    }

    @Test
    void shouldCallExecuteQuery_When_Execute() throws SQLException {
        executor.execute(statement);
        verify(statement, times(1)).executeQuery();
    }


}