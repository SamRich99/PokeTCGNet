package it.unipi.poketcgnet.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GymDTO extends UserDTO {

    private String piva;
    private String shopName;
    private String shopAddress;
    private String prefecture;
    private List<ReviewDTO> reviews;
    private List<OrganizedTournamentEntryDTO> organizedTournaments;
}
