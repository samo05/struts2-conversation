package com.github.overengineer.container;

import java.io.Serializable;

/**
 * @author rees.byars
 */
public interface ComponentStrategyFactory extends Serializable {

    <T> ComponentStrategy<T> create(Class<T> implementationType);

    <T> ComponentStrategy<T> createInstanceStrategy(T implementation);

    <T> ComponentStrategy<T> createCustomStrategy(ComponentStrategy providerStrategy);

}