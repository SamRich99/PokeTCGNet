package it.unipi.poketcgnet.service;

import it.unipi.poketcgnet.dto.DeletedAccountDTO;
import it.unipi.poketcgnet.dto.GymDTO;
import it.unipi.poketcgnet.dto.GymRatingDTO;
import it.unipi.poketcgnet.dto.GymRegistrationRequest;
import it.unipi.poketcgnet.dto.PasswordChangeRequest;
import it.unipi.poketcgnet.dto.ReviewRequest;
import it.unipi.poketcgnet.dto.SliceDTO;

import java.util.List;

public interface GymService {

    SliceDTO<GymDTO> getGymsPage(int pageNumber);

    GymDTO getGymByUsername(String username);

    List<GymRatingDTO> getGymsByPrefectureRankedByRating(String prefecture);

    GymDTO registerGym(GymRegistrationRequest request);

    List<GymDTO> searchGyms(String query);

    void changePassword(String username, PasswordChangeRequest request);

    GymDTO reviewGym(String gymUsername, String trainerUsername, ReviewRequest request);

    // Elimina una recensione di un gym. isAdmin=true: l'admin può eliminare
    // qualsiasi
    // recensione (moderazione). isAdmin=false: il trainer può eliminare SOLO la
    // propria
    GymDTO deleteReview(String gymUsername, String reviewerUsername, String requesterUsername, boolean isAdmin);

    DeletedAccountDTO deleteAccount(String targetUsername);
}
