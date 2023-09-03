package com.bondarenko.mapper;

import com.bondarenko.TestEntity;
import com.bondarenko.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ResultSetMapperTest {

    @Mock
    private ResultSet resultSet;

    @Mock
    private RowMapper<TestEntity> rowMapper;

    private ResultSetMapper<TestEntity> resultSetMapper;

    @BeforeEach
    void setUp() {
        resultSetMapper = new ResultSetMapper();
    }

    @DisplayName("Should successfully map a ResultSet to an Entity")
    @Test
    void shouldMapResultSetToEntitySuccessfully() throws SQLException {
        TestEntity expectedEntity = TestUtil.getTestEntity(1, "entityFirst");

        when(rowMapper.map(resultSet)).thenReturn(expectedEntity);

        TestEntity result = resultSetMapper.mapResultSetToEntity(resultSet, rowMapper);

        assertNotNull(result);
        assertEquals(expectedEntity.getId(), result.getId());
        assertEquals(expectedEntity.getName(), result.getName());
    }

    @DisplayName("Should return null even resulSet empty")
    @Test
    void shouldReturnNullWhenResultSetsEmpty() throws SQLException {
        when(rowMapper.map(resultSet)).thenReturn(null);

        TestEntity result = resultSetMapper.mapResultSetToEntity(resultSet, rowMapper);

        assertNull(result);
    }

    @DisplayName("Should throw an exception when mapping fails")
    @Test
    void shouldThrowExceptionWhenMappingFails() throws SQLException {
        when(rowMapper.map(resultSet)).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> {
            resultSetMapper.mapResultSetToEntity(resultSet, rowMapper);
        });
    }

    @DisplayName("Map ResultSet to List should return an empty list if the ResultSet is empty")
    @Test
    public void mapResultSetToList_ShouldReturnEmptyList_WhenResultSetIsEmpty() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        List<TestEntity> resultList = resultSetMapper.mapResultSetToList(resultSet, rowMapper);

        assertTrue(resultList.isEmpty());
    }

    @DisplayName("map ResultSet to List should return a list of entities if the ResultSet contains data")
    @Test
    public void mapResultSetToList_ShouldReturnListOfEntities_WhenResultSetHasData() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        TestEntity entityFirst = TestUtil.getTestEntity(1, "entityFirst");
        TestEntity entitySecond = TestUtil.getTestEntity(2, "entitySecond");

        when(rowMapper.map(resultSet)).thenReturn(entityFirst, entitySecond);

        List<TestEntity> resultList = resultSetMapper.mapResultSetToList(resultSet, rowMapper);

        assertEquals(2, resultList.size());
        assertEquals(entityFirst, resultList.get(0));
        assertEquals(entitySecond, resultList.get(1));
    }

}