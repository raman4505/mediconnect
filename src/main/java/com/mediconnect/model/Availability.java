package com.mediconnect.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record Availability(long id, long professionalId, DayOfWeek weekday, LocalTime startsAt, LocalTime endsAt, boolean available) { }
