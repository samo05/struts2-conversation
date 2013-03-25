package com.github.overengineer.scope.container.standalone;

import java.util.Map;

public interface Module {
    Map<Class<?>, Class<?>> getTypeMappings();
    Map<Class<?>, Object> getInstanceMappings();
    Map<String, Object> getProperties();
}