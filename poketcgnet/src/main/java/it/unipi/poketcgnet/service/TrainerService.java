package it.unipi.poketcgnet.service;

import it.unipi.poketcgnet.dto.DeletedAccountDTO;
import it.unipi.poketcgnet.dto.MonthlySubscribesDTO;
import it.unipi.poketcgnet.dto.PasswordChangeRequest;
import it.unipi.poketcgnet.dto.SliceDTO;
import it.unipi.poketcgnet.dto.TrainerDTO;
import it.unipi.poketcgnet.dto.TrainerKpiDTO;
import it.unipi.poketcgnet.dto.TrainerRegistrationRequest;

import java.util.List;

public interface TrainerService {

    SliceDTO<TrainerDTO> getTrainersPage(int pageNumber);

    TrainerDTO getTrainerByUsername(String username);

    TrainerDTO registerTrainer(TrainerRegistrationRequest request);

    List<MonthlySubscribesDTO> getMonthlySubscribes();

    List<TrainerKpiDTO> getTrainerKpi(String username);

    List<TrainerDTO> searchTrainers(String query);

    void changePassword(String username, PasswordChangeRequest request);

    DeletedAccountDTO deleteAccount(String targetUsername);

    void endSeason();

    void recomputeProStatus();
}
