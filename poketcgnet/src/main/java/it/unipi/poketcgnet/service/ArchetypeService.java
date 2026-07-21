package it.unipi.poketcgnet.service;

import it.unipi.poketcgnet.dto.ArchetypeDTO;
import it.unipi.poketcgnet.dto.ArchetypeRequest;

import java.util.List;

public interface ArchetypeService {

    List<ArchetypeDTO> getAllArchetypes();

    ArchetypeDTO getArchetypeById(String id);

    List<ArchetypeDTO> searchArchetypesByName(String name);

    List<ArchetypeDTO> findMatchingArchetypes(List<String> cardIds);

    ArchetypeDTO createArchetype(ArchetypeRequest request);

    ArchetypeDTO updateArchetype(String id, ArchetypeRequest request);

    void deleteArchetype(String id);
}
