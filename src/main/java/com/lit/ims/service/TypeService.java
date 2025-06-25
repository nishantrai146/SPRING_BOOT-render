package com.lit.ims.service;


import com.lit.ims.dto.TypeMasterDTO;

import com.lit.ims.entity.TypeMaster;
import com.lit.ims.repository.TypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypeService {
    @Autowired
    private TypeMasterRepository tmr;

    private String generateTrno() {
        long timestamp = System.currentTimeMillis();
        int nextSequence = 1;

        Optional<TypeMaster> last = tmr.findTopByOrderByIdDesc();
        if (last.isPresent()) {
            String lastTrno = last.get().getTrno();
            String lastSeq = lastTrno.substring(lastTrno.length() - 3);
            try {
                nextSequence = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignored) {
            }
        }

        return "TYP" + timestamp + String.format("%03d", nextSequence);
    }

    public TypeMasterDTO save(TypeMasterDTO typeMasterDTO) {
        if (tmr.existsByName(typeMasterDTO.getName())) {
            throw new RuntimeException("Duplicate Type Name");
        }
        String token = generateTrno();

        TypeMaster type = TypeMaster.builder()
                .trno(token)
                .name(typeMasterDTO.getName())
                .status(typeMasterDTO.getStatus()).
                build();

        TypeMaster saved=tmr.save(type);
        return mapToDTO(saved);
    }

    public TypeMasterDTO update(Long id, TypeMasterDTO dto) {
        Optional<TypeMaster> optional = tmr.findById(id);
        if (optional.isEmpty()) throw new RuntimeException("Type not found");

        TypeMaster type = optional.get();
        type.setName(dto.getName());
        type.setStatus(dto.getStatus());

        TypeMaster updated = tmr.save(type);
        return mapToDTO(updated);
    }

    // Get One
    public TypeMasterDTO getOne(Long id) {
        TypeMaster type = tmr.findById(id).orElseThrow(() -> new RuntimeException("Type not found"));
        return mapToDTO(type);
    }

    // Get All
    public List<TypeMasterDTO> getAll() {
        return tmr.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Delete
    public void delete(Long id) {
        tmr.deleteById(id);
    }

    public void deleteMultiple(List<Long> ids){
        tmr.deleteAllById(ids);
    }


    private TypeMasterDTO mapToDTO(TypeMaster entity) {
        return TypeMasterDTO.builder()
                .id(entity.getId())
                .trno(entity.getTrno())
                .name(entity.getName())
                .status(entity.getStatus())
                .build();
    }

    public String getTrnoById(Long id) {
        return tmr.findById(id)
                .map(e -> e.getTrno())
                .orElse("Unknown TRNO");
    }

    public List<String> getTrnosByIds(List<Long> ids) {
        return tmr.findAllById(ids)
                .stream()
                .map(e -> e.getTrno())
                .toList();
    }


}
