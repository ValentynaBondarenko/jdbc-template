package com.bondarenko.template;

import com.bondarenko.mapper.RowMapper;

import java.util.List;

public interface JdbcOperations<T> {
    List<T> query(String sql, RowMapper<T> rowMapper);

    T queryForObject(String sql, RowMapper<T> rowMapper, Object... params);

    int update(String sql, Object... params);
}
