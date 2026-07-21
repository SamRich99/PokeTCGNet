package it.unipi.poketcgnet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.poketcgnet.dto.AdminDTO;
import it.unipi.poketcgnet.dto.PasswordChangeRequest;
import it.unipi.poketcgnet.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admins", description = "Endpoints for the admin account")
@RestController
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Get the admin's profile by username")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{username}")
    public AdminDTO getAdminByUsername(@PathVariable String username) {
        return adminService.getAdminByUsername(username);
    }

    @Operation(summary = "Change the authenticated admin's password")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody PasswordChangeRequest request,
                               Authentication authentication) {
        adminService.changePassword(authentication.getName(), request);
    }
}
