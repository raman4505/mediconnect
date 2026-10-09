package com.mediconnect.service;

import com.mediconnect.model.*;
import com.mediconnect.util.PasswordUtil;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** In-memory demo repository. Locking protects booking and shared collection updates. */
public final class PlatformService implements MediConnectService {
    private final Map<Long, User> users = new LinkedHashMap<>();
    private final Map<Long, Appointment> appointments = new LinkedHashMap<>();
    private final Map<Long, HealthRecord> records = new HashMap<>();
    private final Map<Long, Availability> schedules = new LinkedHashMap<>();
    private final List<Message> messages = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private long nextUser = 1, nextAppointment = 1, nextAvailability = 1, nextMessage = 1;
    private String currency = "INR";
    private int appointmentDurationMinutes = 30;
    private int cancellationNoticeHours;
    private String applicationName = "MediConnect";

    public PlatformService() {
        addUser("Asha Admin", "admin@demo.local", "Admin123!".toCharArray(), Role.ADMIN);
        addUser("Dr. Maya Rao", "doctor@demo.local", "Doctor123!".toCharArray(), Role.PROFESSIONAL);
        addUser("Sam Patient", "patient@demo.local", "Patient123!".toCharArray(), Role.PATIENT);
    }
    public User authenticate(String email, char[] password) {
        lock.readLock().lock(); try { return users.values().stream().filter(u -> u.email().equalsIgnoreCase(email) && u.active() && PasswordUtil.verify(password, u.passwordHash())).findFirst().orElse(null); }
        finally { lock.readLock().unlock(); }
    }
    public User addUser(String name, String email, char[] password, Role role) {
        if (name == null || name.isBlank() || email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || password == null || password.length < 8 || role == null)
            throw new IllegalArgumentException("Enter a name, valid email, and password of at least 8 characters.");
        lock.writeLock().lock(); try {
            if (users.values().stream().anyMatch(u -> u.email().equalsIgnoreCase(email))) throw new IllegalArgumentException("That email is already registered.");
            User user = new User(nextUser++, name.trim(), email.trim(), PasswordUtil.hash(password), role, true); users.put(user.id(), user); return user;
        } finally { lock.writeLock().unlock(); }
    }
    public List<User> users() { lock.readLock().lock(); try { return List.copyOf(users.values()); } finally { lock.readLock().unlock(); } }
    public List<User> professionals() { return users().stream().filter(u -> u.role() == Role.PROFESSIONAL && u.active()).toList(); }
    public List<Appointment> appointmentsFor(User user) {
        lock.readLock().lock(); try { return appointments.values().stream().filter(a -> user.role() == Role.ADMIN || (user.role() == Role.PATIENT ? a.patientId() == user.id() : a.professionalId() == user.id())).toList(); }
        finally { lock.readLock().unlock(); }
    }
    public Appointment book(User patient, User professional, LocalDateTime startsAt, String type) {
        if (patient.role() != Role.PATIENT || professional.role() != Role.PROFESSIONAL) throw new SecurityException("Only patients can book with a healthcare professional.");
        if (startsAt == null || !startsAt.isAfter(LocalDateTime.now())) throw new IllegalArgumentException("Choose a future date and time.");
        lock.writeLock().lock(); try {
            List<Availability> daySchedule=schedules.values().stream().filter(a->a.professionalId()==professional.id()&&a.weekday()==startsAt.getDayOfWeek()).toList();
            if(!daySchedule.isEmpty()&&daySchedule.stream().noneMatch(a->a.available()&&!startsAt.toLocalTime().isBefore(a.startsAt())&&!startsAt.plusMinutes(30).toLocalTime().isAfter(a.endsAt())))throw new IllegalArgumentException("The professional is unavailable at that time.");
            boolean conflict = appointments.values().stream().filter(a -> a.status() != Appointment.Status.CANCELLED)
                .anyMatch(a -> overlaps(a.startsAt(),a.startsAt().plusMinutes(appointmentDurationMinutes),startsAt,startsAt.plusMinutes(appointmentDurationMinutes)) && (a.professionalId() == professional.id() || a.patientId() == patient.id()));
            if (conflict) throw new IllegalArgumentException("This time conflicts with an existing appointment.");
            Appointment a = new Appointment(nextAppointment++, patient.id(), professional.id(), startsAt, type, Appointment.Status.SCHEDULED, "", ""); appointments.put(a.id(), a); return a;
        } finally { lock.writeLock().unlock(); }
    }
    public void cancel(User actor, long appointmentId) {
        lock.writeLock().lock(); try { Appointment a = requireAppointment(appointmentId); if (actor.role() != Role.ADMIN && actor.id() != a.patientId() && actor.id() != a.professionalId()) throw new SecurityException("You cannot manage this appointment."); if(a.status()==Appointment.Status.COMPLETED||a.status()==Appointment.Status.CANCELLED||!a.startsAt().isAfter(LocalDateTime.now())||LocalDateTime.now().plusHours(cancellationNoticeHours).isAfter(a.startsAt()))throw new IllegalArgumentException("This appointment is outside the cancellation window."); appointments.put(a.id(), a.withStatus(Appointment.Status.CANCELLED)); }
        finally { lock.writeLock().unlock(); }
    }
    public void saveConsultation(User actor, long appointmentId, String notes, String advice) {
        lock.writeLock().lock(); try { Appointment a = requireAppointment(appointmentId); if (actor.role() != Role.ADMIN && (actor.role() != Role.PROFESSIONAL || actor.id() != a.professionalId())) throw new SecurityException("Only the assigned professional can save consultation notes."); appointments.put(a.id(), a.withConsultation(notes, advice)); }
        finally { lock.writeLock().unlock(); }
    }
    private Appointment requireAppointment(long id) { Appointment a = appointments.get(id); if (a == null) throw new IllegalArgumentException("Appointment not found."); return a; }
    public HealthRecord recordFor(User actor, long patientId) {
        lock.readLock().lock(); try {
            boolean allowed = actor.role() == Role.ADMIN || (actor.role() == Role.PATIENT && actor.id() == patientId) ||
                    (actor.role() == Role.PROFESSIONAL && appointments.values().stream().anyMatch(a -> a.patientId() == patientId && a.professionalId() == actor.id()));
            if (!allowed) throw new SecurityException("You are not authorized to view this health record.");
            return records.getOrDefault(patientId, new HealthRecord(patientId, "", "", "", "", "", ""));
        } finally { lock.readLock().unlock(); }
    }
    public void saveRecord(User actor, HealthRecord record) { if (actor.role() != Role.PATIENT || actor.id() != record.patientId()) throw new SecurityException("Patients may only update their own health record."); lock.writeLock().lock(); try { records.put(record.patientId(), record); } finally { lock.writeLock().unlock(); } }
    public void setCurrency(String value) { if (!Set.of("INR", "USD", "EUR", "GBP").contains(value)) throw new IllegalArgumentException("Unsupported currency."); currency = value; }
    public String currency() { return currency; }
    public Map<String, Long> analytics() { List<Appointment> all; List<User> people = users(); lock.readLock().lock(); try { all = List.copyOf(appointments.values()); } finally { lock.readLock().unlock(); } return Map.of("Users", (long)people.size(), "Patients", people.stream().filter(u -> u.role()==Role.PATIENT).count(), "Professionals", people.stream().filter(u -> u.role()==Role.PROFESSIONAL).count(), "Appointments", (long)all.size(), "Upcoming", all.stream().filter(a -> a.startsAt().isAfter(LocalDateTime.now()) && a.status()!=Appointment.Status.CANCELLED).count(), "Completed", all.stream().filter(a -> a.status()==Appointment.Status.COMPLETED).count(), "Cancelled", all.stream().filter(a -> a.status()==Appointment.Status.CANCELLED).count()); }
    @Override public List<Availability> availabilityFor(User professional) {
        if (professional.role()!=Role.PROFESSIONAL && professional.role()!=Role.ADMIN) throw new SecurityException("Only professionals can manage availability.");
        lock.readLock().lock(); try { return schedules.values().stream().filter(a -> professional.role()==Role.ADMIN || a.professionalId()==professional.id()).toList(); } finally { lock.readLock().unlock(); }
    }
    @Override public void saveAvailability(User actor,DayOfWeek weekday,LocalTime start,LocalTime end,boolean available) {
        if(actor.role()!=Role.PROFESSIONAL||weekday==null||start==null||end==null||!start.isBefore(end))throw new IllegalArgumentException("Select a working day and valid start/end times.");
        lock.writeLock().lock();try{boolean exists=schedules.values().stream().anyMatch(a->a.professionalId()==actor.id()&&a.weekday()==weekday);if(exists)schedules.values().removeIf(a->a.professionalId()==actor.id()&&a.weekday()==weekday);schedules.put(nextAvailability,new Availability(nextAvailability++,actor.id(),weekday,start,end,available));}finally{lock.writeLock().unlock();}
    }
    private boolean canMessage(long a,long b){return appointments.values().stream().anyMatch(x->x.status()!=Appointment.Status.CANCELLED&&((x.patientId()==a&&x.professionalId()==b)||(x.patientId()==b&&x.professionalId()==a)));}
    @Override public List<Message> messagesFor(User actor){lock.writeLock().lock();try{List<Message> out=new ArrayList<>();for(int i=0;i<messages.size();i++){Message m=messages.get(i);if(m.recipientId()==actor.id()&&!m.read()){m=new Message(m.id(),m.senderId(),m.recipientId(),m.body(),m.sentAt(),true);messages.set(i,m);}if(m.senderId()==actor.id()||m.recipientId()==actor.id())out.add(m);}return List.copyOf(out);}finally{lock.writeLock().unlock();}}
    @Override public void sendMessage(User actor,long recipientId,String body){if(body==null||body.isBlank()||body.length()>4000)throw new IllegalArgumentException("Enter a message (up to 4,000 characters).");lock.writeLock().lock();try{User recipient=users.get(recipientId);if(recipient==null||!canMessage(actor.id(),recipientId)||actor.role()==recipient.role())throw new SecurityException("Messages are limited to patients and professionals connected by an appointment.");messages.add(new Message(nextMessage++,actor.id(),recipientId,body.trim(),LocalDateTime.now(),false));}finally{lock.writeLock().unlock();}}
    @Override public void setUserActive(User actor,long userId,boolean active){if(actor.role()!=Role.ADMIN)throw new SecurityException("Administrator access is required.");lock.writeLock().lock();try{User target=users.get(userId);if(target==null)throw new IllegalArgumentException("User not found.");if(target.role()==Role.ADMIN&&target.id()==1&&!active)throw new IllegalArgumentException("The initial administrator cannot be deactivated.");users.put(userId,new User(target.id(),target.name(),target.email(),target.passwordHash(),target.role(),active));}finally{lock.writeLock().unlock();}}
    @Override public void setAppointmentStatus(User actor,long appointmentId,Appointment.Status status){if(actor.role()!=Role.ADMIN)throw new SecurityException("Administrator access is required.");if(status==null)throw new IllegalArgumentException("Choose an appointment status.");lock.writeLock().lock();try{Appointment a=requireAppointment(appointmentId);if(a.status()==Appointment.Status.COMPLETED&&status!=Appointment.Status.COMPLETED)throw new IllegalArgumentException("Completed consultations retain their history and cannot be reopened.");if(status==Appointment.Status.COMPLETED&&a.notes().isBlank())throw new IllegalArgumentException("A consultation record is required before completion.");appointments.put(a.id(),a.withStatus(status));}finally{lock.writeLock().unlock();}}
    @Override public Appointment reschedule(User actor,long id,LocalDateTime at){if(at==null||!at.isAfter(LocalDateTime.now()))throw new IllegalArgumentException("Choose a future date and time.");lock.writeLock().lock();try{Appointment a=requireAppointment(id);if(actor.role()!=Role.ADMIN&&actor.id()!=a.patientId()&&actor.id()!=a.professionalId())throw new SecurityException("You cannot manage this appointment.");if(a.status()==Appointment.Status.CANCELLED||a.status()==Appointment.Status.COMPLETED||!a.startsAt().isAfter(LocalDateTime.now()))throw new IllegalArgumentException("This appointment can no longer be rescheduled.");List<Availability> day=schedules.values().stream().filter(x->x.professionalId()==a.professionalId()&&x.weekday()==at.getDayOfWeek()).toList();if(!day.isEmpty()&&day.stream().noneMatch(x->x.available()&&!at.toLocalTime().isBefore(x.startsAt())&&!at.plusMinutes(appointmentDurationMinutes).toLocalTime().isAfter(x.endsAt())))throw new IllegalArgumentException("The professional is unavailable at that time.");boolean conflict=appointments.values().stream().filter(x->x.id()!=id&&x.status()!=Appointment.Status.CANCELLED).anyMatch(x->overlaps(x.startsAt(),x.startsAt().plusMinutes(appointmentDurationMinutes),at,at.plusMinutes(appointmentDurationMinutes))&&(x.professionalId()==a.professionalId()||x.patientId()==a.patientId()));if(conflict)throw new IllegalArgumentException("This time conflicts with another appointment.");Appointment updated=new Appointment(a.id(),a.patientId(),a.professionalId(),at,a.type(),a.status(),a.notes(),a.advice());appointments.put(id,updated);return updated;}finally{lock.writeLock().unlock();}}
    @Override public User updateProfile(User actor,String name,String email){if(actor==null||name==null||name.isBlank()||name.length()>120||email==null||email.length()>190||!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))throw new IllegalArgumentException("Enter a valid name and email.");lock.writeLock().lock();try{User current=users.get(actor.id());if(current==null)throw new SecurityException("You cannot update this profile.");if(users.values().stream().anyMatch(u->u.id()!=actor.id()&&u.email().equalsIgnoreCase(email.trim())))throw new IllegalArgumentException("That email is already registered.");User updated=new User(current.id(),name.trim(),email.trim(),current.passwordHash(),current.role(),current.active());users.put(updated.id(),updated);return updated;}finally{lock.writeLock().unlock();}}
    @Override public void changePassword(User actor,char[] current,char[] next){if(next==null||next.length<8)throw new IllegalArgumentException("New password must be at least 8 characters.");lock.writeLock().lock();try{User u=users.get(actor.id());if(u==null||!PasswordUtil.verify(current,u.passwordHash()))throw new IllegalArgumentException("Current password is incorrect.");users.put(u.id(),new User(u.id(),u.name(),u.email(),PasswordUtil.hash(next),u.role(),u.active()));}finally{lock.writeLock().unlock();}}
    private static boolean overlaps(LocalDateTime aStart,LocalDateTime aEnd,LocalDateTime bStart,LocalDateTime bEnd){return aStart.isBefore(bEnd)&&aEnd.isAfter(bStart);}
    @Override public Map<String,String> settings(){lock.readLock().lock();try{return Map.of("application_name",applicationName,"appointment_duration_minutes",String.valueOf(appointmentDurationMinutes),"cancellation_notice_hours",String.valueOf(cancellationNoticeHours),"default_currency",currency);}finally{lock.readLock().unlock();}}
    @Override public void updateSetting(User actor,String key,String value){if(actor.role()!=Role.ADMIN)throw new SecurityException("Administrator access is required.");if(value==null)throw new IllegalArgumentException("A setting value is required.");lock.writeLock().lock();try{switch(key){case "application_name"->{if(value.isBlank()||value.length()>80)throw new IllegalArgumentException("Application name must be 1–80 characters.");applicationName=value.trim();}case "appointment_duration_minutes"->{int n;try{n=Integer.parseInt(value);}catch(NumberFormatException ex){throw new IllegalArgumentException("Appointment duration must be a number.");}if(n<15||n>120||n%15!=0)throw new IllegalArgumentException("Choose a duration from 15 to 120 minutes in 15-minute increments.");appointmentDurationMinutes=n;}case "cancellation_notice_hours"->{int n;try{n=Integer.parseInt(value);}catch(NumberFormatException ex){throw new IllegalArgumentException("Cancellation notice must be a number.");}if(n<0||n>168)throw new IllegalArgumentException("Cancellation notice must be from 0 to 168 hours.");cancellationNoticeHours=n;}case "default_currency"->setCurrency(value);default->throw new IllegalArgumentException("This setting cannot be changed.");}}finally{lock.writeLock().unlock();}}
    @Override public User adminUpdateUser(User actor,long id,String name,String email,Role role){if(actor.role()!=Role.ADMIN)throw new SecurityException("Administrator access is required.");if(name==null||name.isBlank()||name.length()>120||email==null||email.length()>190||!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")||role==null)throw new IllegalArgumentException("Enter a name, valid email, and role.");lock.writeLock().lock();try{User old=users.get(id);if(old==null)throw new IllegalArgumentException("User not found.");if(id==1&&old.role()==Role.ADMIN&&role!=Role.ADMIN)throw new IllegalArgumentException("The initial administrator role is protected.");if(users.values().stream().anyMatch(u->u.id()!=id&&u.email().equalsIgnoreCase(email.trim())))throw new IllegalArgumentException("That email is already registered.");if(role!=old.role()&&appointments.values().stream().anyMatch(a->a.patientId()==id||a.professionalId()==id))throw new IllegalArgumentException("Account roles cannot change while appointments are attached.");User updated=new User(id,name.trim(),email.trim(),old.passwordHash(),role,old.active());users.put(id,updated);return updated;}finally{lock.writeLock().unlock();}}
}
