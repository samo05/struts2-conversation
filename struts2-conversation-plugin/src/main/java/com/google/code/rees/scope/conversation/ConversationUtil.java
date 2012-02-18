package com.google.code.rees.scope.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Provides static methods intended primarily for use internally and in
 * unit testing. Use outside of these contexts should come only as
 * a last resort.
 * 
 * @author rees.byars
 * 
 */
public class ConversationUtil {

    /**
     * Given a conversation name, returns the ID of the conversation for the
     * currently
     * executing thread.
     * 
     * @param conversationName
     * @return
     */
    public static String getConversationId(String conversationName) {
        if (!conversationName
                .endsWith(ConversationConstants.CONVERSATION_NAME_SESSION_MAP_SUFFIX)) {
            conversationName += ConversationConstants.CONVERSATION_NAME_SESSION_MAP_SUFFIX;
        }
        ConversationAdapter adapter = ConversationAdapter.getAdapter();
        String id = (String) adapter.getRequestContext().get(conversationName);
        return id;
    }

    /**
     * Given a conversation field's name, the value of the field is
     * returned from a conversation map in the current thread.
     * 
     * @param fieldName
     * @return
     */
    public static Object getConversationField(String fieldName) {
        Object field = null;
        ConversationAdapter adapter = ConversationAdapter.getAdapter();
        if (adapter != null) {
            Map<String, String> requestContext = adapter.getRequestContext();
            for (Entry<String, String> entry : requestContext.entrySet()) {
                String name = entry.getKey();
                String id = entry.getValue();
                Map<String, Object> conversationContext = adapter
                        .getConversationContext(name, id);
                if (conversationContext != null) {
                    field = (Object) conversationContext.get(fieldName);
                    if (field != null)
                        break;
                }
            }
        }
        return field;
    }

    /**
     * Given a conversation field's name and class, the value of the field is
     * returned from a conversation map in the current thread.
     * 
     * @param <T>
     * @param fieldName
     * @param fieldClass
     * @return
     */
    public static <T> T getConversationField(String fieldName,
            Class<T> fieldClass) {
        return getConversationField(fieldName, fieldClass, getConversations());
    }

    /**
     * Given a conversation field's name and class and an Array of conversation
     * names,
     * the value of the field is returned from the first conversation in the
     * Array
     * that contains an instance of the field.
     * 
     * @param <T>
     * @param fieldName
     * @param fieldClass
     * @param conversations
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getConversationField(String fieldName,
            Class<T> fieldClass, String[] conversations) {
        T field = null;
        ConversationAdapter adapter = ConversationAdapter.getAdapter();
        if (adapter != null) {
            for (String conversationName : conversations) {
                String id = getConversationId(conversationName);
                if (id != null) {
                    Map<String, Object> conversationContext = adapter
                            .getConversationContext(conversationName, id);
                    if (conversationContext != null) {
                        field = (T) conversationContext.get(fieldName);
                        if (field != null)
                            break;
                    }
                }
            }
        }
        return field;
    }

    /**
     * Given a conversation field's name and an instance, the value is set
     * for the field in all active conversations of which the field is a member.
     * 
     * @param fieldName
     * @param fieldValue
     */
    public static void setConversationField(String fieldName, Object fieldValue) {
        ConversationAdapter adapter = ConversationAdapter.getAdapter();
        if (adapter != null) {
            Map<String, String> requestContext = adapter.getRequestContext();
            for (Entry<String, String> entry : requestContext.entrySet()) {
                String name = entry.getKey();
                String id = entry.getValue();
                Map<String, Object> conversationContext = adapter
                        .getConversationContext(name, id);
                conversationContext.put(fieldName, fieldValue);
            }
        }
    }

    /**
     * An array of all active conversations for the current request.
     * 
     * @return
     */
    public static String[] getConversations() {

        List<String> convoIds = new ArrayList<String>();

        ConversationAdapter adapter = ConversationAdapter.getAdapter();
        if (adapter != null) {
            Map<String, String> requestContext = adapter.getRequestContext();
            convoIds.addAll(requestContext.keySet());
        }

        return convoIds.toArray(new String[convoIds.size()]);
    }

    /**
     * Returns an array of all the conversation IDs on the current request
     * 
     * @return
     */
    public static String[] getConversationIds() {

        List<String> convoIds = new ArrayList<String>();

        ConversationAdapter adapter = ConversationAdapter.getAdapter();
        if (adapter != null) {
            Map<String, String> requestContext = adapter.getRequestContext();
            convoIds.addAll(requestContext.values());
        }

        return convoIds.toArray(new String[convoIds.size()]);
    }

    /**
     * Used to remove undesired characters from conversation names
     * 
     * @param conversationName
     * @return
     */
    public static String sanitizeConversationName(String conversationName) {
        return conversationName.replaceAll(":", "").replaceAll(",", "");
    }

    /**
     * Used to generate a unique ID for a conversation context
     * 
     * @return
     */
    public static String generateId() {
        return java.util.UUID.randomUUID().toString();
    }

}
