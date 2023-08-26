package com.bondarenko.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ResultSetMapperTest {

    @Mock
    private ResultSet resultSet;

    @Mock
    private RowMapper<TestEntity> rowMapper;

    private ResultSetMapper resultSetMapper;

    @BeforeEach
    void setUp() {
        resultSetMapper = new ResultSetMapper();
    }

    @Test
    void shouldMapResultSetToEntitySuccessfully() throws SQLException {
        TestEntity expectedEntity = new TestEntity();
        expectedEntity.setId(1);
        expectedEntity.setName("Name");

        when(rowMapper.map(resultSet)).thenReturn(expectedEntity);

        TestEntity result = (TestEntity) resultSetMapper.mapResultSetToEntity(resultSet, rowMapper);

        assertNotNull(result);
        assertEquals(expectedEntity.getId(), result.getId());
        assertEquals(expectedEntity.getName(), result.getName());
    }

    @Test
    void shouldReturnNullWhenResultSetIsEmpty() throws SQLException {
        when(rowMapper.map(resultSet)).thenReturn(null);

        Object result = resultSetMapper.mapResultSetToEntity(resultSet, rowMapper);

        assertNull(result);
    }

    @Test
    void shouldThrowExceptionWhenMappingFails() throws SQLException {
        when(rowMapper.map(resultSet)).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> {
            resultSetMapper.mapResultSetToEntity(resultSet, rowMapper);
        });
    }

    public static class TestEntity {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}