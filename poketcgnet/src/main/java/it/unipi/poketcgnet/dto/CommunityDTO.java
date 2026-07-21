package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Riga di risultato della query "community" 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityDTO {

    private String gymName;
    private List<String> members;
    private Long sharedTournaments;
}
