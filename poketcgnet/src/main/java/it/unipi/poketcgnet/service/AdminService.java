package it.unipi.poketcgnet.service;

import it.unipi.poketcgnet.dto.AdminDTO;
import it.unipi.poketcgnet.dto.PasswordChangeRequest;

public interface AdminService {

    AdminDTO getAdminByUsername(String username);

    void changePassword(String username, PasswordChangeRequest request);
}
