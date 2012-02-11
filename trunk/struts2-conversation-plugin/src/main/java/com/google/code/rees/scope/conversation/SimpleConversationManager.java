package com.google.code.rees.scope.conversation;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleConversationManager implements ConversationManager {

    private static final long serialVersionUID = -518452439785782433L;
    private static final Logger LOG = LoggerFactory
            .getLogger(SimpleConversationManager.class);
    protected ConversationConfigurationProvider configurationProvider = new DefaultConversationConfigurationProvider();

    @Override
    public void setConfigurationProvider(
            ConversationConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @Override
    public void processConversations(ConversationAdapter conversationAdapter) {
        Object action = conversationAdapter.getAction();
        Collection<ConversationConfiguration> actionConversationConfigs = this.configurationProvider
                .getConfigurations(action.getClass());
        if (actionConversationConfigs != null) {
            for (ConversationConfiguration conversationConfig : actionConversationConfigs) {
                processConversation(conversationConfig, conversationAdapter,
                        action);
            }
        }
    }

    protected void processConversation(
            ConversationConfiguration conversationConfig,
            ConversationAdapter conversationAdapter, Object action) {

        String actionId = conversationAdapter.getActionId();
        String conversationName = conversationConfig.getConversationName();
        String conversationId = (String) conversationAdapter
                .getRequestContext().get(conversationName);

        if (conversationId != null) {
            if (conversationConfig.containsAction(actionId)) {
                if (conversationConfig.isEndAction(actionId)) {
                    conversationAdapter.addPostProcessor(
                            new ConversationEndProcessor(), conversationConfig,
                            conversationId);
                } else {
                    conversationAdapter.getViewContext().put(conversationName,
                            conversationId);
                }
            }
        } else if (conversationConfig.isBeginAction(actionId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Beginning new " + conversationName
                        + " conversation.");
            }
            conversationId = ConversationUtil.generateId();
            conversationAdapter.getViewContext().put(conversationName,
                    conversationId);
            conversationAdapter.createConversationContext(conversationId,
                    conversationAdapter.getSessionContext());
        }
    }

}
