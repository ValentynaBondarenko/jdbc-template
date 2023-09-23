package com.bondarenko.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetMapper<T> {
    public <T> T mapResultSetToEntity(ResultSet resultSet, RowMapper<T> rowMapper) {
        try {
            if (resultSet.next()) {
                return rowMapper.map(resultSet);
            }
        } catch (SQLException e) {

            throw new RuntimeException("Error mapping result set to entity", e);
        }
        return null;
    }

    public <T> List<T> mapResultSetToList(ResultSet resultSet, RowMapper<T> rowMapper) {
        List<T> results = new ArrayList<>();
        try {
            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping result set to list", e);
        }
        return results;
    }
}
