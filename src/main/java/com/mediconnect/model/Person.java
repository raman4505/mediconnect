package com.mediconnect.model;

/** Shared identity and contact details inherited by application users. */
public abstract class Person {
    private final long id;
    private final String name;
    private final String email;

    protected Person(long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public long id() { return id; }
    public String name() { return name; }
    public String email() { return email; }
}
