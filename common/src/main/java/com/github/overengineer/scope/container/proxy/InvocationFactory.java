package com.github.overengineer.scope.container.proxy;

import java.lang.reflect.Method;

/**
 */
public interface InvocationFactory {
    <T> Invocation<T> create(T target, Method method, Object[] parameters);
}
