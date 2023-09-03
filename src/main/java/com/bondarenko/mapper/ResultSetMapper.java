package com.bondarenko.mapper;

import java.sql.ResultSet;
import java.util.List;

public class ResultSetMapper<T> {
    public T mapResultSetToEntity(ResultSet resultSet, RowMapper<T> rowMapper) {
        return null;
    }

    public List<T> mapResultSetToList(ResultSet resultSet, RowMapper<T> rowMapper) {
        return null;
    }
}
