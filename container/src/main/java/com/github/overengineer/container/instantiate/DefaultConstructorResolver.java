package com.github.overengineer.container.instantiate;

import com.github.overengineer.container.metadata.MetadataAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

/**
 * @author rees.byars
 */
public class DefaultConstructorResolver implements ConstructorResolver {

    private final MetadataAdapter metadataAdapter;

    public DefaultConstructorResolver(MetadataAdapter metadataAdapter) {
        this.metadataAdapter = metadataAdapter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Constructor<T> resolveConstructor(Class<T> type, Class ... providedArgs) {
        Type[] genericParameterTypes = {};
        Constructor<T> constructor = null;
        for (Constructor candidateConstructor : type.getDeclaredConstructors()) {
            if (metadataAdapter.isValidConstructor(candidateConstructor)) {
                Type[] candidateTypes = candidateConstructor.getGenericParameterTypes();
                if (candidateTypes.length >= genericParameterTypes.length && candidateTypes.length >= providedArgs.length) { //TODO use param matching util
                    constructor = candidateConstructor;
                    genericParameterTypes = candidateTypes;
                }
            }
        }
        assert constructor != null;
        constructor.setAccessible(true);
        return constructor;
    }

}
