package com.examinai.app.service;

import java.util.UUID;

import com.examinai.app.web.admin.EditUserRequest;

/**
 * Admin edit form state produced inside a transaction so role names are materialized before the
 * persistence session closes ({@code spring.jpa.open-in-view=false}).
 */
public record AdminUserEditModel(UUID userId, String email, EditUserRequest editRequest) {
}
