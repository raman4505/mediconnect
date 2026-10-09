package com.mediconnect.model;
public record HealthRecord(long patientId, String bloodGroup, String allergies, String conditions, String medications, String history, String emergencyContact) { }
