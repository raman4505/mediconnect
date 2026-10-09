package com.mediconnect.model;

import java.util.Objects;

/** An authenticated person with a role and password hash. */
public final class User extends Person {
    private final String passwordHash;
    private final Role role;
    private final boolean active;

    public User(long id, String name, String email, String passwordHash, Role role, boolean active) {
        super(id, name, email);
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    public String passwordHash() { return passwordHash; }
    public Role role() { return role; }
    public boolean active() { return active; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof User user)) return false;
        return id() == user.id()
                && active == user.active
                && name().equals(user.name())
                && email().equals(user.email())
                && passwordHash.equals(user.passwordHash)
                && role == user.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id(), name(), email(), passwordHash, role, active);
    }

    /** Avoid exposing the password hash in logs or diagnostic output. */
    @Override
    public String toString() {
        return "User[id=" + id() + ", name=" + name() + ", email=" + email()
                + ", role=" + role + ", active=" + active + "]";
    }
}
