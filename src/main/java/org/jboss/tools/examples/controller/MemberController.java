package org.jboss.tools.examples.controller;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.PostAddToViewEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.jboss.tools.examples.model.Member;
import org.jboss.tools.examples.service.MemberRegistration;

// The @Model stereotype is a convenience mechanism to make this a request-scoped bean that has an
// EL name
// Read more about the @Model stereotype in this FAQ:
// http://sfwk.org/Documentation/WhatIsThePurposeOfTheModelAnnotation
@Model
public class MemberController {

   @Inject
   private FacesContext facesContext;

   @Inject
   private MemberRegistration memberRegistration;

   private Member newMember;

   @Produces
   @Named
   public Member getNewMember() {
      return newMember;
   }

   public void register() throws Exception {
       try {
           memberRegistration.register(newMember);
           FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, "Registered!", "Registration successful");
           facesContext.addMessage(null, m);
           initNewMember();
       } catch (Exception e) {
           String errorMessage = getRootErrorMessage(e);
           FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, "Registration unsuccessful");
           facesContext.addMessage(null, m);
       }
   }

   @PostConstruct
   public void initNewMember() {
      newMember = new Member();
   }
   
   private String getRootErrorMessage(Exception e) {
       // Default to general error message that registration failed.
       String errorMessage = "Registration failed. See server log for more information";
       if (e == null) {
           // This shouldn't happen, but return the default messages
           return errorMessage;
       }

       // Start with the exception and recurse to find the root cause
       Throwable t = e;
       while (t != null) {
           // Get the message from the Throwable class instance
           errorMessage = t.getLocalizedMessage();
           t = t.getCause();
       }
       // This is the root cause message
       return errorMessage;
   }
   
   public void dummy() {
      System.out.println("XXX: Dummy called");
   }
   
   public void setDummyValue(String value) {
      System.out.println("XXX: Dummy value set: " + value);
   }

   public String getDummyValue() {
      return "dummy";
            
   }
   
   /*
   public void setValue(String expression, String value) {
        
      System.out.println("XXX: settingValue: " + expression + " to " + value);
      
      FacesContext context = FacesContext.getCurrentInstance();
      UIViewRoot viewRoot = context.getViewRoot();
      Application application = context.getApplication();
      
      UIComponent magicRoot = viewRoot.findComponent("reg:magic");
      
      UIInput valueSetterComponent = (UIInput)application.createComponent(UIInput.COMPONENT_TYPE);
      
      ELContext ctx = FacesContext.getCurrentInstance().getELContext();
      ValueExpression valueExpression = FacesContext.getCurrentInstance().getApplication().getExpressionFactory().createValueExpression(ctx, expression, String.class);
      
      magicRoot.getChildren().add(valueSetterComponent);
      
      valueSetterComponent.setValueExpression("value", valueExpression);
      
      valueSetterComponent.setSubmittedValue(value);
      
      // valueSetterComponent.validate(context);
}

   public void postAddToViewEventListener(ComponentSystemEvent event) {
      
      System.out.println("XXX: component: " + event.getComponent().toString());
      
      setValue("#{newMember.name}", "Jane Doe");
      setValue("#{newMember.email}", "xxx 123");
      setValue("#{newMember.phoneNumber}", "xxx");
   }*/
}
