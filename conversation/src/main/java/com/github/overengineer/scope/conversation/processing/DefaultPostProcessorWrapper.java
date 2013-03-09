/*******************************************************************************
 * 
 *  Struts2-Conversation-Plugin - An Open Source Conversation- and Flow-Scope Solution for Struts2-based Applications
 *  =================================================================================================================
 * 
 *  Copyright (C) 2012 by Rees Byars
 *  http://code.google.com/p/struts2-conversation/
 * 
 * **********************************************************************************************************************
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations under the License.
 * 
 * **********************************************************************************************************************
 * 
 *  $Id: DefaultPostProcessorWrapper.java reesbyars $
 ******************************************************************************/
package com.github.overengineer.scope.conversation.processing;

import com.github.overengineer.scope.conversation.ConversationAdapter;
import com.github.overengineer.scope.conversation.configuration.ConversationClassConfiguration;

/**
 * The default implementation of the {@link PostProcessorWrapper}
 * 
 * @author rees.byars
 */
public class DefaultPostProcessorWrapper<T extends PostProcessor> implements PostProcessorWrapper<T> {

    private static final long serialVersionUID = -8235162251071925835L;

    private transient ConversationClassConfiguration conversationConfig;
    private String conversationId;
    private T postProcessor;
    private ConversationAdapter conversationAdapter;

    public DefaultPostProcessorWrapper(
            ConversationAdapter conversationAdapter,
            T postProcessor,
            ConversationClassConfiguration conversationConfig, String conversationId) {
        this.conversationAdapter = conversationAdapter;
        this.postProcessor = postProcessor;
        this.conversationConfig = conversationConfig;
        this.conversationId = conversationId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postProcessConversation() {
        this.postProcessor.postProcessConversation(conversationAdapter, conversationConfig, conversationId);
    }

}
