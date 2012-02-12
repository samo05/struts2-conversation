package com.google.code.rees.scope.struts2.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ui.AbstractUITag;

import com.opensymphony.xwork2.util.ValueStack;

/**
 * 
 * @author rees.byars
 * 
 */
public class ConversationsTag extends AbstractUITag {

    private static final long serialVersionUID = -2510111224558001809L;

    @Override
    public Component getBean(ValueStack paramValueStack,
            HttpServletRequest paramHttpServletRequest,
            HttpServletResponse paramHttpServletResponse) {
        return new Conversations(paramValueStack, paramHttpServletRequest,
                paramHttpServletResponse);
    }

}