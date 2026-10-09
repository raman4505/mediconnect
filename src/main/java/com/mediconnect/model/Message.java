package com.mediconnect.model;

import java.time.LocalDateTime;

public record Message(long id, long senderId, long recipientId, String body, LocalDateTime sentAt, boolean read) { }
