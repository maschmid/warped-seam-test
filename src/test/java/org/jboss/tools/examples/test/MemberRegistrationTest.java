package org.jboss.tools.examples.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
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

   @Test
   public void testRegister() throws Exception {
      
      driver.navigate().to(basePath.toString() + "/index.jsf");
      
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
         
         @Inject
         private List<Member> members;
        
         
         @BeforePhase(Phase.INVOKE_APPLICATION)
         public void beforeInvokeApplication() {
            
            ELContext ctx = FacesContext.getCurrentInstance().getELContext();
            
            ValueExpression valueExpression = FacesContext.getCurrentInstance().getApplication().getExpressionFactory().createValueExpression(ctx, "#{newMember}", Member.class);
            Member newMember = (Member)valueExpression.getValue(ctx);
            
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
