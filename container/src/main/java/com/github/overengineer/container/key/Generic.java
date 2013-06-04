package com.github.overengineer.container.key;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author rees.byars
 */
public abstract class Generic<T> implements Key<T> {

    private final String name;
    private transient Type type;
    private transient Class<? super T> targetClass;
    private final int hash;

    public Generic() {
        init();
        this.name = "";
        this.hash = type.hashCode();
    }

    public Generic(String name) {
        init();
        this.name = name;
        this.hash = type.hashCode();
    }

    private void init() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof ParameterizedType) {
            targetClass = KeyUtil.getClass(type);
        } else {
            throw new UnsupportedOperationException("The GenericKey is invalid [" + type + "]");
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Class<? super T> getTargetClass() {
        return targetClass;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof BasicKey && type.equals(((BasicKey) object).getType()) && name.equals(((BasicKey) object).getName());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.init();
    }

}