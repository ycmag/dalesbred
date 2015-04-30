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

package org.dalesbred;

import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class DatabaseOptionalTest {

    private final Database db = TestDatabaseProvider.createInMemoryHSQLDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void emptyOptionalIsBoundAsNull() {
        assertThat(db.findUnique(Integer.class, "values (cast (? as int))", Optional.empty()), is(nullValue()));
        assertThat(db.findUnique(Integer.class, "values (cast (? as int))", OptionalInt.empty()), is(nullValue()));
        assertThat(db.findUnique(Long.class, "values (cast (? as int))", OptionalLong.empty()), is(nullValue()));
        assertThat(db.findUnique(Double.class, "values (cast (? as float))", OptionalDouble.empty()), is(nullValue()));
    }

    @Test
    public void nonEmptyOptionalIsBoundAsValue() {
        db.update("drop table if exists optional_test");
        db.update("create table optional_test (val VARCHAR(20))");

        db.update("insert into optional_test (val) values (?)", Optional.of("foo"));

        assertThat(db.findUniqueInt("select count(*) from optional_test where val = 'foo'"), is(1));
    }
}
