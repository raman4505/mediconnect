package com.mediconnect.web;

import com.mediconnect.model.*;
import com.mediconnect.service.MediConnectService;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.UUID;

/** Maps browser requests to role-checked service operations and Thymeleaf views. */
@Controller
public class WebController {
    private final MediConnectService service;
    public WebController(MediConnectService service) { this.service = service; }

    @ModelAttribute("csrf")
    String csrf(HttpSession session) {
        String token = (String) session.getAttribute("csrf");
        if (token == null) { token = UUID.randomUUID().toString(); session.setAttribute("csrf", token); }
        return token;
    }
    private void checkCsrf(HttpServletRequest req, HttpSession session) {
        Object expected = session.getAttribute("csrf");
        if (expected == null || !expected.equals(req.getParameter("_csrf"))) throw new IllegalArgumentException("Your form expired. Please reload and try again.");
    }
    private User current(HttpSession session) { return (User) session.getAttribute("user"); }
    private User require(HttpSession session, Role... roles) {
        User u = current(session);
        if (u == null) throw new IllegalStateException("Please sign in first.");
        if (roles.length > 0 && java.util.Arrays.stream(roles).noneMatch(r -> r == u.role())) throw new SecurityException("This page is not available to your account.");
        return u;
    }
    @GetMapping("/") String home(HttpSession session, Model model) { if (current(session) != null) return "redirect:/dashboard"; model.addAttribute("demoMode",service instanceof com.mediconnect.service.PlatformService); model.addAttribute("settings",service.settings()); return "home"; }
    @PostMapping("/login") String login(@RequestParam String email, @RequestParam String password, HttpServletRequest request, HttpServletResponse response, RedirectAttributes flash) {
        HttpSession old = request.getSession(); checkCsrf(request, old);
        User u = service.authenticate(email.trim(), password.toCharArray());
        if (u == null) { flash.addFlashAttribute("error", "Email or password is incorrect."); return "redirect:/"; }
        request.changeSessionId(); HttpSession session = request.getSession(); session.setAttribute("user", u); session.setAttribute("csrf", UUID.randomUUID().toString()); return "redirect:/dashboard";
    }
    @PostMapping("/register") String register(@RequestParam String name, @RequestParam String email, @RequestParam String password, HttpServletRequest request, RedirectAttributes flash) {
        checkCsrf(request, request.getSession());
        try { service.addUser(name, email, password.toCharArray(), Role.PATIENT); flash.addFlashAttribute("success", "Account created. You can now sign in."); }
        catch (RuntimeException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/";
    }
    @GetMapping("/dashboard") String dashboard(HttpSession session, Model model) {
        User u = require(session); model.addAttribute("user", u); model.addAttribute("appointments", service.appointmentsFor(u)); model.addAttribute("professionals", service.professionals());
        model.addAttribute("users", u.role() == Role.ADMIN ? service.users() : java.util.List.of()); model.addAttribute("people", service.users().stream().collect(java.util.stream.Collectors.toMap(User::id, x -> x))); model.addAttribute("stats", service.analytics()); model.addAttribute("currency", service.currency());model.addAttribute("settings",service.settings());model.addAttribute("demoMode",service instanceof com.mediconnect.service.PlatformService);
        if (u.role() == Role.PATIENT) model.addAttribute("record", service.recordFor(u, u.id()));
        if (u.role() == Role.PROFESSIONAL) model.addAttribute("authorizedRecords", service.appointmentsFor(u).stream().map(Appointment::patientId).distinct().map(id -> service.recordFor(u, id)).toList());
        var ownAppointments=service.appointmentsFor(u);
        model.addAttribute("messages",service.messagesFor(u));
        model.addAttribute("messageContacts",ownAppointments.stream().map(a->u.role()==Role.PATIENT?a.professionalId():a.patientId()).distinct().map(id->service.users().stream().filter(person->person.id()==id&&person.active()).findFirst().orElse(null)).filter(java.util.Objects::nonNull).toList());
        if(u.role()==Role.PROFESSIONAL)model.addAttribute("availability",service.availabilityFor(u));
        return "dashboard";
    }
    @PostMapping("/appointments") String book(@RequestParam long professionalId, @RequestParam String startsAt, @RequestParam String type, HttpSession session, HttpServletRequest request, RedirectAttributes flash) {
        checkCsrf(request, session); User p = require(session, Role.PATIENT);
        try { User d = service.professionals().stream().filter(x -> x.id() == professionalId).findFirst().orElseThrow(() -> new IllegalArgumentException("Professional not found.")); service.book(p, d, LocalDateTime.parse(startsAt), type); flash.addFlashAttribute("success", "Your consultation is booked."); }
        catch (RuntimeException ex) { flash.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/dashboard";
    }
    @PostMapping("/appointments/{id}/cancel") String cancel(@PathVariable long id, HttpSession session, HttpServletRequest request, RedirectAttributes flash) {
        checkCsrf(request, session); try { service.cancel(require(session), id); flash.addFlashAttribute("success", "Appointment cancelled."); } catch (RuntimeException ex) { flash.addFlashAttribute("error", ex.getMessage()); } return "redirect:/dashboard";
    }
    @PostMapping("/appointments/{id}/reschedule") String reschedule(@PathVariable long id,@RequestParam String startsAt,HttpSession session,HttpServletRequest request,RedirectAttributes flash){checkCsrf(request,session);try{service.reschedule(require(session),id,LocalDateTime.parse(startsAt));flash.addFlashAttribute("success","Appointment rescheduled.");}catch(RuntimeException ex){flash.addFlashAttribute("error",ex.getMessage());}return "redirect:/dashboard";}
    @PostMapping("/consultations/{id}") String consultation(@PathVariable long id, @RequestParam String notes, @RequestParam String advice, HttpSession session, HttpServletRequest request, RedirectAttributes flash) {
        checkCsrf(request, session); try { service.saveConsultation(require(session, Role.PROFESSIONAL), id, notes, advice); flash.addFlashAttribute("success", "Consultation saved and marked complete."); } catch (RuntimeException ex) { flash.addFlashAttribute("error", ex.getMessage()); } return "redirect:/dashboard";
    }
    @PostMapping("/health-record") String saveRecord(@RequestParam String bloodGroup, @RequestParam String allergies, @RequestParam String conditions, @RequestParam String medications, @RequestParam String history, @RequestParam String emergencyContact, HttpSession session, HttpServletRequest request, RedirectAttributes flash) {
        checkCsrf(request, session); User u = require(session, Role.PATIENT); service.saveRecord(u, new HealthRecord(u.id(), bloodGroup, allergies, conditions, medications, history, emergencyContact)); flash.addFlashAttribute("success", "Health record saved."); return "redirect:/dashboard";
    }
    @PostMapping("/currency") String currency(@RequestParam String currency, HttpSession session, HttpServletRequest request, RedirectAttributes flash) {
        checkCsrf(request, session); require(session); try { service.setCurrency(currency); flash.addFlashAttribute("success", "Display currency updated."); } catch (RuntimeException ex) { flash.addFlashAttribute("error", ex.getMessage()); } return "redirect:/dashboard";
    }
    @PostMapping("/profile") String profile(@RequestParam String name,@RequestParam String email,HttpSession session,HttpServletRequest request,RedirectAttributes flash){checkCsrf(request,session);try{User updated=service.updateProfile(require(session),name,email);session.setAttribute("user",updated);flash.addFlashAttribute("success","Profile updated.");}catch(RuntimeException ex){flash.addFlashAttribute("error",ex.getMessage());}return "redirect:/dashboard";}
    @PostMapping("/password") String password(@RequestParam String currentPassword,@RequestParam String newPassword,HttpSession session,HttpServletRequest request,RedirectAttributes flash){checkCsrf(request,session);try{service.changePassword(require(session),currentPassword.toCharArray(),newPassword.toCharArray());flash.addFlashAttribute("success","Password changed.");}catch(RuntimeException ex){flash.addFlashAttribute("error",ex.getMessage());}return "redirect:/dashboard";}
    @PostMapping("/admin/users") String addManagedUser(@RequestParam String name,@RequestParam String email,@RequestParam String password,@RequestParam Role role,HttpSession session,HttpServletRequest request,RedirectAttributes flash){checkCsrf(request,session);try{require(session,Role.ADMIN);service.addUser(name,email,password.toCharArray(),role);flash.addFlashAttribute("success","User created.");}catch(RuntimeException ex){flash.addFlashAttribute("error",ex.getMessage());}return "redirect:/dashboard";}
    @PostMapping("/admin/users/{id}") String updateManagedUser(@PathVariable long id,@RequestParam String name,@RequestParam String email,@RequestParam Role role,HttpSession session,HttpServletRequest request,RedirectAttributes flash){checkCsrf(request,session);try{service.adminUpdateUser(require(session,Role.ADMIN),id,name,email,role);flash.addFlashAttribute("success","User details updated.");}catch(RuntimeException ex){flash.addFlashAttribute("error",ex.getMessage());}return "redirect:/dashboard";}
    @PostMapping("/admin/settings") String setting(@RequestParam String key,@RequestParam String value,HttpSession session,HttpServletRequest request,RedirectAttributes flash){checkCsrf(request,session);try{service.updateSetting(require(session,Role.ADMIN),key,value);flash.addFlashAttribute("success","System setting updated.");}catch(RuntimeException ex){flash.addFlashAttribute("error",ex.getMessage());}return "redirect:/dashboard";}
    @PostMapping("/availability") String availability(@RequestParam int weekday,@RequestParam String startsAt,@RequestParam String endsAt,@RequestParam(defaultValue="true") boolean available,HttpSession session,HttpServletRequest request,RedirectAttributes flash){checkCsrf(request,session);try{service.saveAvailability(require(session,Role.PROFESSIONAL),java.time.DayOfWeek.of(weekday),java.time.LocalTime.parse(startsAt),java.time.LocalTime.parse(endsAt),available);flash.addFlashAttribute("success","Availability saved.");}catch(RuntimeException ex){flash.addFlashAttribute("error",ex.getMessage());}return "redirect:/dashboard";}
    @PostMapping("/messages") String sendMessage(@RequestParam long recipientId,@RequestParam String body,HttpSession session,HttpServletRequest request,RedirectAttributes flash){checkCsrf(request,session);try{service.sendMessage(require(session,Role.PATIENT,Role.PROFESSIONAL),recipientId,body);flash.addFlashAttribute("success","Message sent.");}catch(RuntimeException ex){flash.addFlashAttribute("error",ex.getMessage());}return "redirect:/dashboard";}
    @PostMapping("/admin/users/{id}/active") String userActive(@PathVariable long id,@RequestParam boolean active,HttpSession session,HttpServletRequest request,RedirectAttributes flash){checkCsrf(request,session);try{service.setUserActive(require(session,Role.ADMIN),id,active);flash.addFlashAttribute("success",active?"User activated.":"User deactivated.");}catch(RuntimeException ex){flash.addFlashAttribute("error",ex.getMessage());}return "redirect:/dashboard";}
    @PostMapping("/admin/appointments/{id}/status") String appointmentStatus(@PathVariable long id,@RequestParam Appointment.Status status,HttpSession session,HttpServletRequest request,RedirectAttributes flash){checkCsrf(request,session);try{service.setAppointmentStatus(require(session,Role.ADMIN),id,status);flash.addFlashAttribute("success","Appointment status updated.");}catch(RuntimeException ex){flash.addFlashAttribute("error",ex.getMessage());}return "redirect:/dashboard";}
    @PostMapping("/logout") String logout(HttpSession session, HttpServletRequest request) { checkCsrf(request, session); session.invalidate(); return "redirect:/"; }
    @ExceptionHandler({IllegalStateException.class, SecurityException.class}) String accessDenied(RuntimeException ex, RedirectAttributes flash) { flash.addFlashAttribute("error", ex.getMessage()); return "redirect:/"; }
}
