package com.bondarenko.template.validation;


import com.bondarenko.mapper.RowMapper;

import java.util.Map;

public class ValidationUtils {
    private ValidationUtils() {
    }

    /**
     * Validates the paramMap to ensure it is not null .
     *
     * @param paramMap The map to be validated.
     * @throws IllegalArgumentException If paramMap is null or has an odd size.
     */
    public static void validateParamMap(Map<String, ?> paramMap) {
        if (paramMap == null) {
            throw new IllegalArgumentException("paramMap should not be null, and its size should be even.");
        }
    }

    /**
     * Validates the params array to ensure it is not null .
     *
     * @param params The array of parameters to be validated.
     * @throws IllegalArgumentException If params is null or has an odd length.
     */
    public static void validateParamArray(Object[] params) {
        if (params == null) {
            throw new IllegalArgumentException("params should not be null.");
        }
    }

    /**
     * Validates the SQL query to ensure it is not null.
     *
     * @param sql The SQL query to be validated.
     * @throws IllegalArgumentException If sql is null.
     */
    public static void validateSql(String sql) {
        if (sql == null) {
            throw new IllegalArgumentException("SQL query should not be null.");
        }
    }

    /**
     * Validates the RowMapper to ensure it is not null.
     *
     * @param rowMapper The RowMapper to be validated.
     * @param <T>       The type of objects to be mapped.
     * @throws IllegalArgumentException If rowMapper is null.
     */
    public static <T> void validateRowMapper(RowMapper<T> rowMapper) {
        if (rowMapper == null) {
            throw new IllegalArgumentException("RowMapper should not be null.");
        }
    }
}