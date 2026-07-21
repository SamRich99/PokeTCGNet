package it.unipi.poketcgnet.service.impl;

import it.unipi.poketcgnet.dto.AdminDTO;
import it.unipi.poketcgnet.dto.PasswordChangeRequest;
import it.unipi.poketcgnet.model.mongo.Admin;
import it.unipi.poketcgnet.repository.mongo.AdminRepository;
import it.unipi.poketcgnet.service.AdminService;
import it.unipi.poketcgnet.service.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AdminDTO getAdminByUsername(String username) {
        return toDTO(adminRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Admin non trovato: " + username)));
    }

    @Override
    public void changePassword(String username, PasswordChangeRequest request) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Admin non trovato: " + username));
        if (!passwordEncoder.matches(request.getOldPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("La password attuale non è corretta");
        }
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        adminRepository.save(admin);
    }

    private AdminDTO toDTO(Admin admin) {
        return AdminDTO.builder()
                .username(admin.getUsername())
                .role(admin.getRole())
                .email(admin.getEmail())
                .name(admin.getName())
                .surname(admin.getSurname())
                .birthDate(admin.getBirthDate())
                .createdAt(admin.getCreatedAt())
                .build();
    }
}
