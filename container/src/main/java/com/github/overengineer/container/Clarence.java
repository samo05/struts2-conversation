package com.github.overengineer.container;

import com.github.overengineer.container.factory.DefaultMetaFactory;
import com.github.overengineer.container.proxy.HotSwappableContainer;
import com.github.overengineer.container.proxy.ProxyModule;
import com.github.overengineer.container.proxy.aop.AopContainer;
import com.github.overengineer.container.proxy.aop.AopModule;
import com.github.overengineer.container.key.DefaultKeyGenerator;

/**
 * @author rees.byars
 */
public class Clarence {

    Container builder =
            new DefaultContainer(
                    new DefaultComponentStrategyFactory(),
                    new DefaultKeyGenerator(),
                    new DefaultMetaFactory());

    public static Clarence please() {
        return new Clarence();
    }

    public HotSwappableContainer gimmeThatProxyTainer() {
        return builder.loadModule(ProxyModule.class).get(HotSwappableContainer.class);
    }

    public AopContainer gimmeThatAopTainer() {
        return builder.loadModule(AopModule.class).get(AopContainer.class);
    }

    public Container gimmeThatTainer() {
        return builder;
    }

}