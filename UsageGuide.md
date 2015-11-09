# Sections #

  1. [Annotations](#Annotations.md)
  1. [Conversation Redirects](#Conversation_Redirects.md)
  1. [Memory Management](#Memory_Management.md)
  1. [Error Mapping](#Error_Mapping.md)
  1. [Unit Testing](#Unit_Testing.md)
  1. [Spring](#Spring.md)


---


## Annotations ##

Examples of the available annotations:

The @ConversationField annotation is used to designate a particular field as conversation-scoped.  Fields annotated as such must implement the Serializable interface or a run-time exception will occur (this is done purposefully to prevent serialization pitfalls).  Otherwise, any action fields can be a conversation field and any number of field in the action can be designated as such, e.g. you could have three Strings in an action class all persisted as conversation fields by annotating them as such.  For purposes of good design, it is usually best to have a single conversation model in the action rather than lots of fields, but you are free to choose what works best for you.

The conversation fields are persisted using bijection.  This means that you can assign a new instance to a conversation field, unlike in a framework such as Spring that uses proxies and auto-instantiation and does not allow reassignment.  Conceptually, the conversation plugin does not perform dependency injection - it is purely a field-level scoping mechanism.  And although it integrates well with DI frameworks such as Spring (as covered below), in which case Spring's injection and instantiation mechanisms are used instead of the bijection approach, to treat the front-end-conversation problem space as a dependency injection problem space is flawed conceptually (though not necessarily flawed in practice, how's that? ;D ).


The @ConversationController annotation is a "convention-over-configuration" tool that can be used to include all the actions in an action class as conversation actions, using the convention that action methods that begin with "begin" or "end" will begin or end the controller's conversation, respectively, and that the default conversation name will be the action class name minus the action suffix (so in the below example the conversation name is "example"):

```
@ConversationController
public class ExampleAction extends ActionSupport implements ModelDriven<ExampleModel> {
    
    @ConversationField private ExampleModel exampleModel;
    
    public String beginAction() {
        exampleModel = new ExampleModel();
        return SUCCESS;
    }
    
    public String intermediateAction() {
        //do something with the data
        return SUCCESS;
    }
    
    public String endAction() {
        //do something with the data
        return SUCCESS;
    }

    @Override
    public ExampleModel getModel() {
        return exampleModel;
    }

}
```

Alternatively, the @BeginConversation, @ConversationAction, and @EndConversation annotations can be used:

```
public class ExampleAction extends ActionSupport implements ModelDriven<ExampleModel> {
    
    @ConversationField(conversations = "example") 
    private ExampleModel exampleModel;
    
    @BeginConversation(conversations = "example") 
    public String beginAction() {
        exampleModel = new ExampleModel();
        return SUCCESS;
    }
    
    @ConversationAction(conversations = "example") 
    public String intermediateAction() {
        return SUCCESS;
    }
    
    @EndConversation(conversations = "example") 
    public String endAction() {
        //do something with the data
        return SUCCESS;
    }

    @Override
    public ExampleModel getModel() {
        return exampleModel;
    }

}
```


---


## Conversation Redirects ##

If the standard Struts2 result types "redirect" and "redirectAction" are used, then the action conversations are not propagated along with the redirect.  If you wish to allow redirects of conversations, then the following approach should be used:


In the struts.xml, either extend struts-conversation-default package:

```
<package name="your-package" extends="struts-conversation-default">
```


Or, declare the result types in your package:

```
<package name="your-package" extends="struts-default">
   <result-types>
      <result-type name="conversationRedirectAction" class="com.google.code.rees.scope.struts2.ConversationActionRedirectResult"/>
      <result-type name="conversationRedirect" class="com.google.code.rees.scope.struts2.ConversationRedirectResult"/>
   </result-types>
```


Then, in your result mapping set the result type as either "conversationRedirect" or "conversationRedirectAction", and the conversations will propagate over the redirection.


---

## Memory Management ##

Conversations have a default idle-time timeout of 8 hours in order to keep old conversations from taking up system resources.  Conversations are, by default, monitored once every 5 minutes to scan for cleanup of the stale conversations.  As well, there is a default maximum of 20 instances of each conversation per session to prevent users from unnecessarily overloading the system by creating countless conversations (so, in the above example, there could be up to 20 "base" conversations, 20 "registration" conversations, etc.).  The size of the timeout monitoring thread-pool can be configured also (the default is 20).  Each of these settings can be changed in the struts.xml by configuring constants:


```
<!-- monitoring frequency in milliseconds -->
<constant name="conversation.monitoring.frequency" value="300000"/>

<!-- idle conversation timeout in milliseconds -->
<constant name="conversation.idle.timeout" value="28800000"/>

<!-- max instances of a conversation -->
<constant name="conversation.max.instances" value="20"/>

<!-- number of timeout monitoring threads -->
<constant name="conversation.monitoring.thread.pool.size" value="20"/>
```

If you wish to have different timeouts for different conversations, the BeginConversation annotation can be used to specify a different timeout period (this feature only available since version _1.6.1_):

```
//This conversation will only last 10 seconds!
@BeginConversation(maxIdleTimeMillis=10000L)
public String begin() {
   return "hurry";
}
```

It is important to note the an HttpSessionListener is used to cleanup each session's conversation resources when the session ends.  This listener will startup automatically for Servlet API 3.0+.  For earlier Servlet APIs, the listener should be configured in the web.xml:

```
<!-- only needed for apps not using Servlet API 3.0 or greater -->
<listener>
   <listener-class>com.google.code.rees.scope.conversation.context.ConversationCleanupListener</listener-class>
</listener>
```

Another important point for @ConversationField annotated fields is to be aware of the default field-to-conversation assignment behavior.  By default, conversation fields declared in sub-conversations are also stored in the context of the super conversations.  This is the default behavior because it lends to quicker development time and incurs little cost unless the field has a large memory imprint.  For such cases as when the field has a large memory cost that needs to be minimized, the _conversations_ attribute of the @ConversationField annotation should be used to specify the exact conversations whose context should include the field.


---



## Error Mapping ##

_since v1.7.0_ (in v1.6.2, the result key was "conversation.exception" and the message functionality was absent)

Inevitably, users will sometimes submit a request that contains an ID for a conversation that has already expired or that has been explicitly ended on another tab that they have open.  When this happens, the framework will produce a "conversation.exception" result.  This value can be used to map conversation errors gracefully (or not!):

```
        <global-results>
            <result name="struts.conversation.invalid.id">/globalConversationException.jsp</result>
        </global-results>
```

More on global result mapping in Struts2:  http://struts.apache.org/2.x/docs/exception-configuration.html

Add messages in this format to a resource bundle to customize conversation error messages for your users (yes, these are stupid messages intended for learning purposes):
```
struts.conversation.invalid.id=Your request did not contain a valid ID.  Please try again. (${conversation.name}:${conversation.id})
struts.conversation.invalid.id.registration_conversation=This Registration Session has expired.  Please begin a new one, Jack.
```

The first message above would display the conversation name and ID.  These could be included in a message to help in debugging, etc.

The second message above uses the conversation name in the key.  In this way, conversation-specific messages can be provided in place of the default (the first message).

If no message are specified, then the default message is: "The workflow that you are attempting to continue has ended or expired.  Your requested action was not processed."

These messages are placed on the ValueStack, so they can be referenced from the view using `<s:property value="struts.conversation.invalid.id" />`.  Note that this is always the key used, don't try appending the conversation name like is done in the resource bundle.


---


## Unit Testing ##
This is an example of how to test actions against the interceptor stack
```
public class RegistrationControllerTest extends StrutsJUnit4TestCase<RegistrationController> {

     @ConversationField
     RegistrationModel registrationModel;

     @Test
     public testContinueRegistration() throws Exception {

         //set test data
         registrationModel = RegistrationFactory.getNewModel();
         registrationModel.setSomeField("oopy doopy");
		
         //place IDs on request
	 ScopeTestUtil.setConversationIdsOnRequest(request);
	
         //create proxy for the action
	 ActionProxy proxy = this.getActionProxy("/registration/continue-registration.action");
		
         //extract the test data from the test class into the session
	 ScopeTestUtil.extractScopeFields(this);
		
         //execute the action/proxy and obtain result
	 String result = proxy.execute();
	
         //assert result
	 assertEquals(Action.SUCCESS, result);
		
         //inject the scoped fields back into the test from the session
	 ScopeTestUtil.injectScopeFields(this);

         //assert some value
         assertEquals("Cinco Phone", registrationModel.getSomeField());
     }

}
```

---

## Spring ##
For use with the Spring IoC container, a custom conversation scope is provided.  Add the following to your applicationContext.xml:
```
    <bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
        <property name="scopes">
            <map>
                <entry key="conversation">
                    <bean class="com.google.code.rees.scope.spring.ConversationScope"/>
                </entry>
            </map>
        </property>
    </bean>

    <bean id="registrationModel" class="com.google.code.rees.scope.mocks.beans.RegistrationModel" scope="conversation"/>
```
Then in your java classes, you can autowire conversation-scoped dependencies:
```
@ConversationController
public class RegistrationController extends ActionSupport implements ModelDriven<RegistrationModel> {

     //injection of this field now managed by Spring IoC container
     //yet its scope is still managed by the Struts2 ConversationManager.
     //The scope must be declared as "conversation" in a context file
     //or using the @Scope("conversation") annotation on the model class
     @Autowired
     private RegistrationModel registrationModel; 

     @Action("begin-registration")
     public String beginRegistration() {
          return SUCCESS;
     }

     @Action("continue-registration")
     public String continueRegistration() {
          return SUCCESS;
     }

     @Action("end-registration")
     public String endRegistration() {
          return SUCCESS;
     }

     public RegistrationModel getModel() {
          return registrationModel;
     }
}
```
Using Spring, the conversation-scoped beans now become available in other Spring-managed beans as well:
```
public class ConversationRegistrationService implements RegistrationService {

     @Autowired
     private RegistrationModel registrationModel; 

     ......

}
```

---