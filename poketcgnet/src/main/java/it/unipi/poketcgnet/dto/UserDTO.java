package it.unipi.poketcgnet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class UserDTO {

    private String username;
    private String role;
    private String email;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private LocalDate createdAt;
}
