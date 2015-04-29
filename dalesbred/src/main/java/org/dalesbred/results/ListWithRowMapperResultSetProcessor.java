/*
 * Copyright (c) 2015 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.dalesbred.results;

import org.dalesbred.internal.utils.Require;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A ResultSetProcessor that creates a list of results using given RowMapper.
 */
public final class ListWithRowMapperResultSetProcessor<T> implements ResultSetProcessor<List<T>> {
    
    @NotNull
    private final RowMapper<T> rowMapper;
    
    public ListWithRowMapperResultSetProcessor(@NotNull RowMapper<T> rowMapper) {
        this.rowMapper = Require.requireNonNull(rowMapper);
    }

    @Override
    @NotNull
    public List<T> process(@NotNull ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();

        while (resultSet.next())
            result.add(rowMapper.mapRow(resultSet));

        return result;
    }
}
