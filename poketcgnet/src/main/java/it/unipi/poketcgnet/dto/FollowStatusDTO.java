package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Risposta di follow/unfollow. 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowStatusDTO {

    private String follower;
    private String target;
    private Boolean following;
}
