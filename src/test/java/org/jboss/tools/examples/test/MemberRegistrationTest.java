package org.jboss.tools.examples.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.application.Application;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.validator.BeanValidator;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.jsf.AfterPhase;
import org.jboss.arquillian.warp.jsf.BeforePhase;
import org.jboss.arquillian.warp.jsf.Phase;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.tools.examples.controller.MemberController;
import org.jboss.tools.examples.data.MemberListProducer;
import org.jboss.tools.examples.model.Member;
import org.jboss.tools.examples.service.MemberRegistration;
import org.jboss.tools.examples.util.Resources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@WarpTest
@RunWith(Arquillian.class)
@RunAsClient
public class MemberRegistrationTest {
   @Deployment
   public static Archive<?> createTestArchive() {
      return ShrinkWrap.create(WebArchive.class, "test.war")
            .addClasses(MemberController.class, MemberListProducer.class, Member.class, MemberRegistration.class, Resources.class)
            .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsWebResource(new File("src/main/webapp/index.xhtml"))
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/templates/default.xhtml"), "templates/default.xhtml")
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/faces-config.xml"))
            // Deploy our test datasource
            .addAsWebInfResource("test-ds.xml", "test-ds.xml");
      
            
   }

   @Drone
   WebDriver driver;
   
   @ArquillianResource
   URL basePath;
   
   static By nameField = By.id("reg:name");
   static By emailField = By.id("reg:email");
   static By phoneField = By.id("reg:phoneNumber");
   static By registerButton = By.id("reg:register");
//   static By dummyField = By.id("reg:dummy");
//   static By magicButton = By.id("reg:mbutton");

   
   @Test
   public void testRegister() throws Exception {
      
      driver.navigate().to(basePath.toString() + "index.jsf");
      
      //driver.findElement(dummyField).sendKeys("dummy");
      driver.findElement(nameField).sendKeys("Jane Doe");
      driver.findElement(emailField).sendKeys("jane@mailinator.com");
      driver.findElement(phoneField).sendKeys("2125551234");
      
      Warp.initiate(new Activity()
      {
         @Override
         public void perform()
         {
            driver.findElement(registerButton).click();
         }
      })
      .group()
      .inspect(new Inspection() {
         private static final long serialVersionUID = 1L;
         
         @ArquillianResource
         FacesContext context;
         
         @ArquillianResource
         UIViewRoot viewRoot;
         
         @ArquillianResource
         Application application;
         
         void setValue(String expression, Class<?> returnType, Object value) {
            
            UIComponent magicRoot = viewRoot.findComponent("reg:magic");
            
            UIInput valueSetterComponent = (UIInput)application.createComponent(UIInput.COMPONENT_TYPE);
            
            ELContext ctx = FacesContext.getCurrentInstance().getELContext();
            ValueExpression valueExpression = FacesContext.getCurrentInstance().getApplication().getExpressionFactory().createValueExpression(ctx, expression, returnType);
            
            magicRoot.getChildren().add(valueSetterComponent);
            
            valueSetterComponent.setValueExpression("value", valueExpression);
            
            valueSetterComponent.setSubmittedValue(value);
            
            valueSetterComponent.addValidator(new BeanValidator());
            
            // valueSetterComponent.validate(context);
         }
         
         void setAction(String expression, Class<?> returnType, Class<?>... parameterTypes) {
            UICommand magicButton = (UICommand)viewRoot.findComponent("reg:magic_button");
            
            ELContext ctx = FacesContext.getCurrentInstance().getELContext();
            
            MethodExpression methodExpression = application.getExpressionFactory().createMethodExpression(ctx, expression, returnType, parameterTypes);
            
            magicButton.setActionExpression(methodExpression);
            //magicButton.setValueExpression("action", inding)
            
            
            //magicButton.setValueExpression("action", val)
            
         }
         
         @BeforePhase(Phase.APPLY_REQUEST_VALUES)
         public void beforeApplyRequestValues() {
            /*
            setValue("#{newMember.name}", String.class, "Jane Doe");
            setValue("#{newMember.email}", String.class, "jane@mailinator.com");
            setValue("#{newMember.phoneNumber}", String.class, "2125551234");
            */
            // setAction("#{memberController.register}", null);
            
            for (Map.Entry entry : context.getExternalContext().getRequestParameterMap().entrySet()) {
               System.out.println("XXX: " + entry.getKey().toString() + ": " + entry.getValue());
            }
            //setValue("#{newMember.email}", "xxx 123");
            //setValue("#{newMember.phoneNumber}", "xxx");
         }
         
         @AfterPhase(Phase.PROCESS_VALIDATIONS)
         public void afterProcessValidations() {
            
            /*
            UIComponent magicRoot = viewRoot.findComponent("reg:magic");
            assertEquals(3, magicRoot.getChildren().size());
            
            UIInput nameInput = (UIInput) magicRoot.getChildren().get(0);
            assertEquals(1, nameInput.getValidators().length);
           */
            
            // validation errors:
            assertTrue(context.getMessageList().size() == 0);
         }
         
         
         @Inject
         private List<Member> members;
        
         
         public <T> T getValue(String expression, Class<T> klass) {
            
            ELContext ctx = FacesContext.getCurrentInstance().getELContext();
            
            ValueExpression valueExpression = application.getExpressionFactory().createValueExpression(ctx, expression, klass);
            return (T)valueExpression.getValue(ctx);
         }
         
         @BeforePhase(Phase.INVOKE_APPLICATION)
         public void beforeInvokeApplication() {
            
            Member newMember = getValue("#{newMember}", Member.class);
            
            assertEquals("Jane Doe", newMember.getName());
            assertEquals("jane@mailinator.com", newMember.getEmail());
            assertEquals("2125551234", newMember.getPhoneNumber());
         }
         
         @AfterPhase(Phase.INVOKE_APPLICATION)
         public void afterInvokeApplication() {
            assertEquals(1, members.size());
         }
         
         
      }).execute();
      
      /*
      Member newMember = new Member();
      newMember.setName("Jane Doe");
      newMember.setEmail("jane@mailinator.com");
      newMember.setPhoneNumber("2125551234");
      memberRegistration.register(newMember);
      assertNotNull(newMember.getId());
      log.info(newMember.getName() + " was persisted with id " + newMember.getId());
      */
   }
   
   
   
}
