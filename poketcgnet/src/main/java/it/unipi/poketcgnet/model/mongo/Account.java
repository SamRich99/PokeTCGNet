package it.unipi.poketcgnet.model.mongo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;

// Campi comuni a Trainer/Gym/Admin. Nessuna annotazione @Document: non e' mai
// persistita da sola, ogni sottoclasse ha la propria collezione dedicata.
// Serve come tipo comune nei punti di login/security che
// devono riconoscere un account a prescindere dal tipo.
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class Account {

    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private String email;
    private String password;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private LocalDate createdAt;
}
