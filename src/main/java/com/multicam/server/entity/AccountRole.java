package com.multicam.server.entity;

public enum AccountRole {
    CONTROLLER("controller"),
    REMOTE("remote");

    private final String value;

    AccountRole(String value) {
        this.value = value;
    }

    /** Giá trị trả về cho client, và dùng cho claim "acc" trong token sau này. */
    public String getValue() { return value; }
}