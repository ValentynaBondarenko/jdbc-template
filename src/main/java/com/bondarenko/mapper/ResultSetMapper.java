package com.bondarenko.mapper;

import com.bondarenko.exception.DataAccessException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetMapper<T> {
    public T mapResultSetToEntity(ResultSet resultSet, RowMapper<T> rowMapper) {
        try {
            if (resultSet.next()) {
                return rowMapper.map(resultSet);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error mapping result set to entity", e);
        }
        return null;
    }

    public List<T> mapResultSetToList(ResultSet resultSet, RowMapper<T> rowMapper) {
        List<T> results = new ArrayList<>();
        try {
            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error mapping result set to list", e);
        }
        return results;
    }
}
