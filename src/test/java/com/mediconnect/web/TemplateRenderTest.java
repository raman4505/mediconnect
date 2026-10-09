package com.mediconnect.web;

import com.mediconnect.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TemplateRenderTest {
    private final SpringTemplateEngine engine;
    private final MockServletContext servletContext = new MockServletContext();
    TemplateRenderTest() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); resolver.setSuffix(".html"); resolver.setTemplateMode("HTML"); resolver.setCacheable(false);
        engine = new SpringTemplateEngine(); engine.setTemplateResolver(resolver);
    }
    private String renderDashboard(Role role) {
        User u = new User(role.ordinal()+1,"Test User","test@example.local","",role,true);
        Map<String,Object> model = new HashMap<>();
        model.put("csrf","test-token");model.put("user",u);model.put("appointments",List.of());model.put("professionals",List.of());model.put("users",role==Role.ADMIN?List.of(u):List.of());model.put("people",Map.of(u.id(),u));model.put("stats",Map.of("Users",1L));model.put("currency","INR");model.put("settings",Map.of("application_name","MediConnect","appointment_duration_minutes","30","cancellation_notice_hours","0"));model.put("demoMode",true);model.put("messages",List.of());model.put("messageContacts",List.of());model.put("availability",List.of());model.put("authorizedRecords",List.of());model.put("record",new HealthRecord(u.id(),"","","","","",""));
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext,"GET","/dashboard");
        MockHttpServletResponse response = new MockHttpServletResponse();
        WebContext context = new WebContext(JakartaServletWebApplication.buildApplication(servletContext).buildExchange(request,response),Locale.US,model);
        return engine.process("dashboard",context);
    }
    @Test void homeTemplateRendersWithSettings() {
        MockHttpServletRequest request=new MockHttpServletRequest(servletContext,"GET","/");MockHttpServletResponse response=new MockHttpServletResponse();
        WebContext context=new WebContext(JakartaServletWebApplication.buildApplication(servletContext).buildExchange(request,response),Locale.US,Map.of("csrf","token","settings",Map.of("application_name","MediConnect"),"demoMode",true));
        String html=engine.process("home",context);assertTrue(html.contains("Sign in"));assertTrue(html.contains("Create patient account"));
    }
    @Test void patientProfessionalAndAdminDashboardsRender() { for(Role role:Role.values()){String html=renderDashboard(role);assertTrue(html.contains("Profile and password"));assertTrue(html.contains("System settings")==(role==Role.ADMIN));} }
}
