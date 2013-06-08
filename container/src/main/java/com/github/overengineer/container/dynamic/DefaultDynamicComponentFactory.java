package com.github.overengineer.container.dynamic;

import com.github.overengineer.container.Provider;
import com.github.overengineer.container.inject.InjectorFactory;
import com.github.overengineer.container.inject.MethodInjector;
import com.github.overengineer.container.instantiate.InstantiatorFactory;
import com.github.overengineer.container.key.Key;
import com.github.overengineer.container.metadata.MetadataAdapter;
import com.github.overengineer.container.parameter.ParameterMatchingUtil;
import com.github.overengineer.container.util.MethodCacheKey;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rees.byars
 */
public class DefaultDynamicComponentFactory implements DynamicComponentFactory {

    private final InstantiatorFactory instantiatorFactory;
    private final InjectorFactory injectorFactory;
    private final MetadataAdapter metadataAdapter;

    public DefaultDynamicComponentFactory(InstantiatorFactory instantiatorFactory, InjectorFactory injectorFactory, MetadataAdapter metadataAdapter) {
        this.instantiatorFactory = instantiatorFactory;
        this.injectorFactory = injectorFactory;
        this.metadataAdapter = metadataAdapter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createManagedComponentFactory(final Class factoryInterface, final Key producedTypeKey, final Provider provider) {
        DynamicManagedComponentFactory<T> dynamicFactory = new DynamicManagedComponentFactory<T>(factoryInterface, producedTypeKey, provider);
        T proxy = (T) Proxy.newProxyInstance(
                provider.getClass().getClassLoader(),
                new Class[]{factoryInterface, Serializable.class},
                dynamicFactory
        );
        dynamicFactory.proxy = proxy;
        return proxy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createNonManagedComponentFactory(Class factoryInterface, Class concreteProducedType, Provider provider) {
        DynamicNonManagedComponentFactory<T> dynamicFactory = new DynamicNonManagedComponentFactory<T>(
                factoryInterface,
                concreteProducedType,
                provider,
                instantiatorFactory.create(
                        concreteProducedType,
                        factoryInterface.getDeclaredMethods()[0].getParameterTypes()));
        T proxy = (T) Proxy.newProxyInstance(
                provider.getClass().getClassLoader(),
                new Class[]{factoryInterface, Serializable.class},
                dynamicFactory
        );
        dynamicFactory.proxy = proxy;
        return proxy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createCompositeHandler(Class<T> targetInterface, final Provider provider) {
        DynamicComposite<T> handler = new DynamicComposite<T>(targetInterface, provider);
        handler.proxy = (T) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{targetInterface, Serializable.class},
                handler
        );
        return handler.proxy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createDelegatingService(Class<T> serviceInterface, Provider provider) {

        Map<MethodCacheKey, ServiceDelegateInvoker> delegateInvokerCache = new HashMap<MethodCacheKey, ServiceDelegateInvoker>();
        for (Method serviceMethod : serviceInterface.getDeclaredMethods()) {
            Key delegateKey = metadataAdapter.getDelegateKey(serviceMethod);
            Class<?> delegateClass = delegateKey.getTargetClass();
            Class[] providedArgs = serviceMethod.getParameterTypes();
            Method delegateMethod = null;
            for (Method delegateCandidateMethod : delegateClass.getDeclaredMethods()) {
                //TODO make the provided arg matching injectable to match different parameter builders
                if (delegateCandidateMethod.getName().equals(serviceMethod.getName()) && ParameterMatchingUtil.precedingMatch(providedArgs, delegateCandidateMethod.getParameterTypes())) {
                    delegateMethod = delegateCandidateMethod;
                    break;
                }
            }
            if (delegateMethod == null) {
                //TODO mrpvoe error message
                throw new IllegalArgumentException("No valid delegate methods could be found");
            }
            MethodInjector delegateMethodInjector = injectorFactory.create(delegateClass, delegateMethod, providedArgs);
            @SuppressWarnings("unchecked")
            ServiceDelegateInvoker<T> serviceDelegateInvoker = new ServiceDelegateInvoker<T>(delegateKey, delegateMethodInjector, provider);
            MethodCacheKey cacheKey = new MethodCacheKey(serviceMethod);
            delegateInvokerCache.put(cacheKey, serviceDelegateInvoker);
        }

        DelegatingService<T> delegatingService = new DelegatingService<T>(serviceInterface, delegateInvokerCache);
        delegatingService.proxy = (T) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{serviceInterface, Serializable.class},
                delegatingService
        );
        return delegatingService.proxy;
    }


}
