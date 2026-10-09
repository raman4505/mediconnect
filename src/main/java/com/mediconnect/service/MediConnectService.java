package com.mediconnect.service;

import com.mediconnect.model.*;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/** Application contract shared by the in-memory demo and JDBC persistence services. */
public interface MediConnectService {
    User authenticate(String email, char[] password);
    User addUser(String name, String email, char[] password, Role role);
    List<User> users();
    List<User> professionals();
    List<Appointment> appointmentsFor(User user);
    Appointment book(User patient, User professional, LocalDateTime startsAt, String type);
    void cancel(User actor, long appointmentId);
    void saveConsultation(User actor, long appointmentId, String notes, String advice);
    HealthRecord recordFor(User actor, long patientId);
    void saveRecord(User actor, HealthRecord record);
    void setCurrency(String value);
    String currency();
    Map<String, Long> analytics();
    List<Availability> availabilityFor(User professional);
    void saveAvailability(User actor, DayOfWeek weekday, LocalTime startsAt, LocalTime endsAt, boolean available);
    List<Message> messagesFor(User actor);
    void sendMessage(User actor, long recipientId, String body);
    void setUserActive(User actor, long userId, boolean active);
    void setAppointmentStatus(User actor, long appointmentId, Appointment.Status status);
    Appointment reschedule(User actor, long appointmentId, LocalDateTime newTime);
    User updateProfile(User actor, String name, String email);
    void changePassword(User actor, char[] currentPassword, char[] newPassword);
    Map<String,String> settings();
    void updateSetting(User actor, String key, String value);
    User adminUpdateUser(User actor, long userId, String name, String email, Role role);
}
