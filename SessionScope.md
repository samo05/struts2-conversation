#This page covers the use of the @SessionField annotation

# A Simple Session Scope Mechanism #

Frequently, the conversation scope is overkill or not appropriate to the problem space, but you still want data persisted across multiple requests.  Using the [SessionAware](http://struts.apache.org/2.x/struts2-core/apidocs/org/apache/struts2/interceptor/SessionAware.html) interface or the [ScopedModelDrivenInterceptor](http://struts.apache.org/2.x/docs/scoped-model-driven-interceptor.html) are the two "out-of-the-box" solutions for this in Struts2.  The Conversation plugin offers a third alternative:  the @SessionField annotation.

Use of this approach is a matter of taste:  instead of XML configuration, boiler-plate code, and String constants (the session keys), a simple annotation is used to achieve the same end.

Compare the following:

### SessionAware Approach ###

java:
```
public class ExampleAction extends ActionSupport implements SessionAware, ModelDriven<ExampleModel> {
    
    public static final String EXAMPLE_MODEL_SESSION_KEY = "exampleModel";
    private Map<String, Object> session;
    
    public String firstAction() {
        ExampleModel exampleModel = new ExampleModel();
        this.setModel(exampleModel);
        return SUCCESS;
    }
    
    public String secondAction() {
        ExampleModel exampleModel = this.getModel();
        //do something with the data
        return SUCCESS;
    }

    @Override
    public void setSession(final Map<String, Object> session) {
        this.session = session;
    }

    @Override
    public ExampleModel getModel() {
        return (ExampleModel) session.get(EXAMPLE_MODEL_SESSION_KEY);
    }
    
    protected void setModel(final ExampleModel exampleModel) {
        session.put(EXAMPLE_MODEL_SESSION_KEY, exampleModel);
    }

}
```

xml:
```
<!-- add to your interceptor stack -->
<interceptor-ref name="servletConfig" />
```

### ScopedModelDriven Approach ###

java:
```
public class ExampleAction extends ActionSupport implements ScopedModelDriven<ExampleModel> {
    
    public static final String EXAMPLE_MODEL_SESSION_KEY = "exampleModel";
    private ExampleModel exampleModel;
    
    public String firstAction() {
        exampleModel = new ExampleModel();
        return SUCCESS;
    }
    
    public String secondAction() {
        //do something with the data
        return SUCCESS;
    }

    @Override
    public ExampleModel getModel() {
        return exampleModel;
    }
    
    @Override
    public void setModel(final ExampleModel exampleModel) {
        this.exampleModel = exampleModel;
    }

    @Override
    public String getScopeKey() {
        return EXAMPLE_MODEL_SESSION_KEY;
    }

    @Override
    public void setScopeKey(String scopeKey) {
        //not likely useful
    }

}
```

xml:
```
<!-- add for each session-scoped model -->
<interceptor name="exampleModelInterceptor" class="com.opensymphony.interceptor.ScopedModelDrivenInterceptor">
     <param name="scope">session</param>
     <param name="name">exampleModel</param>
     <param name="className">com.byars.struts2.ExampleModel</param>
 </interceptor>

<!-- then add this ref to your interceptor stack -->
<interceptor-ref name="exampleModelInterceptor" />
```
### @SessionField Approach ###

the java:
```
public class ExampleAction extends ActionSupport implements ModelDriven<ExampleModel> {
    
    @SessionField private ExampleModel exampleModel;
    
    public String firstAction() {
        exampleModel = new ExampleModel();
        return SUCCESS;
    }
    
    public String secondAction() {
        //do something with the data
        return SUCCESS;
    }

    @Override
    public ExampleModel getModel() {
        return exampleModel;
    }

}
```

the xml:
```
<!-- add to your interceptor stack -->
<interceptor-ref name="sessionField" />
```


---