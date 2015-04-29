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

package org.dalesbred.support.proxy;

import org.dalesbred.Database;
import org.dalesbred.TransactionSettings;
import org.dalesbred.Transactional;
import org.dalesbred.internal.utils.Require;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Creates proxies for transactional services.
 */
public final class TransactionalProxyFactory {

    private TransactionalProxyFactory() { }

    /**
     * Returns a new transactional proxy for given target object.
     */
    public static <T> T createTransactionalProxyFor(@NotNull Database db, @NotNull Class<T> iface, @NotNull T target) {
        return iface.cast(createTransactionalProxyFor(db, iface.getClassLoader(), new Class<?>[]{iface}, target));
    }

    /**
     * Returns a new transactional proxy for given target object. The proxy will implement all of the
     * given interfaces.
     */
    public static Object createTransactionalProxyFor(@NotNull Database db, @NotNull ClassLoader classLoader, @NotNull Class<?>[] interfaces, @NotNull Object target) {
        return Proxy.newProxyInstance(classLoader, interfaces, new TransactionInvocationHandler(db, target));
    }

    private static final class TransactionInvocationHandler implements InvocationHandler {

        @NotNull
        private final Database db;

        @NotNull
        private final Object target;

        public TransactionInvocationHandler(@NotNull Database db, @NotNull Object target) {
            this.db = Require.requireNonNull(db);
            this.target = Require.requireNonNull(target);
        }

        @Override
        public Object invoke(@NotNull Object proxy, @NotNull Method method, @Nullable Object[] args) throws Throwable {
            TransactionSettings tx = findTransactionSettings(method);

            if (tx == null)
                return invokeWithoutTransaction(method, args);
            else
                return invokeInTransaction(tx, method, args);
        }

        @Nullable
        private Object invokeWithoutTransaction(@NotNull Method method, @Nullable Object[] args) throws Throwable {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        @Nullable
        private Object invokeInTransaction(@NotNull TransactionSettings settings,
                                           @NotNull Method method,
                                           @Nullable Object[] args) throws Throwable {
            try {
                return db.withTransaction(settings, tx -> {
                    try {
                        return method.invoke(target, args);
                    } catch (IllegalAccessException e) {
                        throw new WrappedException(e);
                    } catch (InvocationTargetException e) {
                        throw new WrappedException(e.getTargetException());
                    }
                });
            } catch (WrappedException e) {
                throw e.getCause();
            }
        }

        @Nullable
        private TransactionSettings findTransactionSettings(@NotNull Method interfaceMethod) {
            Transactional tx = findTransactionDefinition(interfaceMethod);
            return  (tx != null) ? TransactionSettings.fromAnnotation(tx) : null;
        }

        @Nullable
        private Transactional findTransactionDefinition(@NotNull Method interfaceMethod) {
            try {
                Method actualMethod = target.getClass().getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
                Transactional tx = actualMethod.getAnnotation(Transactional.class);

                if (tx == null)
                    tx = interfaceMethod.getAnnotation(Transactional.class);

                if (tx == null)
                    tx = interfaceMethod.getDeclaringClass().getAnnotation(Transactional.class);

                return tx;

            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class WrappedException extends RuntimeException {
        WrappedException(@NotNull Throwable throwable) {
            super(throwable);
        }
    }
}