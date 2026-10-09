package com.mediconnect.model;

import java.time.LocalDateTime;

public record Appointment(long id, long patientId, long professionalId, LocalDateTime startsAt,
                          String type, Status status, String notes, String advice) {
    public enum Status { SCHEDULED, CONFIRMED, COMPLETED, CANCELLED }
    public Appointment withStatus(Status next) { return new Appointment(id, patientId, professionalId, startsAt, type, next, notes, advice); }
    public Appointment withConsultation(String newNotes, String newAdvice) { return new Appointment(id, patientId, professionalId, startsAt, type, Status.COMPLETED, newNotes, newAdvice); }
}
