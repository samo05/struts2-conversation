package com.github.overengineer.scope.conversation.context;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.overengineer.scope.testutil.SerializationTestingUtil;
import com.github.overengineer.scope.testutil.thread.BasicTaskThread;
import com.github.overengineer.scope.testutil.thread.TaskThread;
import com.github.overengineer.scope.testutil.thread.ThreadTask;


public class DefaultConversationContextManagerTest implements Serializable
{

    static final String TEST_NAME = "test";

    @Test
    public void testGetContext() {

    }

    @Test
    public void testSerialization() throws Exception {

        String testName = "test-conversation";
        String testId = "testId";
        DefaultConversationContextManager mgr = new DefaultConversationContextManager();
        mgr.setContextFactory(new ConversationContextFactory() {
            @Override
            public ConversationContext create(String conversationName, String conversationId, long maxIdleTime) {
                return new DefaultConversationContext(conversationName, conversationId, maxIdleTime);
            }
        });
        ConversationContext ctx = mgr.getContext(testName, testId);

        mgr = SerializationTestingUtil.getSerializedCopy(mgr);
        assertEquals(ctx, mgr.getContext(testName, testId));

    }

    /**
     * test takes a long time, run manually
     */
    @Test
    public void testConcurrentModification() throws InterruptedException {

        DefaultConversationContextManager manager = new DefaultConversationContextManager();
        manager.setContextFactory(new ConversationContextFactory() {
            @Override
            public ConversationContext create(String conversationName, String conversationId, long maxIdleTime) {
                return new DefaultConversationContext(conversationName, conversationId, maxIdleTime);
            }
        });

        TaskThread contractionThread = BasicTaskThread.spawnInstance();
        CollectionContractionTask collectionContractionTask = new CollectionContractionTask(manager);
        contractionThread.addTask(collectionContractionTask);

        TaskThread expansionThread = BasicTaskThread.spawnInstance();
        expansionThread.addTask(new CollectionExpansionTask(manager, collectionContractionTask));

        Thread.sleep(8000L);

        for (int i = 0; i < 12; i++) {
            BasicTaskThread.spawnInstance().addTask(new CollectionExpansionTask(manager, collectionContractionTask));
        }

        Thread.sleep(8000L);


    }

    class CollectionExpansionTask implements ThreadTask {

        private final Logger LOG = LoggerFactory.getLogger(CollectionExpansionTask.class);

        private ConversationContextManager manager;
        private CollectionContractionTask removalTask;

        CollectionExpansionTask(ConversationContextManager manager, CollectionContractionTask removalTask) {
            this.manager = manager;
            this.removalTask = removalTask;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void doTask() {
            //this will create a new context
            LOG.info("Expanding...");
            ConversationContext c = this.manager.createContext(TEST_NAME, 300L, 5);
            this.removalTask.addId(c.getId());
            Thread.yield();
        }

    }

    class CollectionContractionTask implements ThreadTask {

        private final Logger LOG = LoggerFactory.getLogger(CollectionContractionTask.class);

        private final ConversationContextManager manager;
        private final Collection<String> ids = new CopyOnWriteArraySet<String>();

        CollectionContractionTask(ConversationContextManager manager) {
            this.manager = manager;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void doTask() {
            for (String id : this.ids) {
                LOG.info("Contracting...");
                manager.remove(TEST_NAME, id);
                Thread.yield();
            }
            this.ids.clear();
            try {
                Thread.sleep(1L);
            } catch (InterruptedException e) {
                // meh
            }
        }

        public void addId(String id) {
            this.ids.add(id);
        }

    }

}
