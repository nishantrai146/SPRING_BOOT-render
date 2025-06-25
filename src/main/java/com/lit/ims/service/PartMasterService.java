package com.lit.ims.service;

import com.lit.ims.dto.PartMasterDT0;
import com.lit.ims.entity.PartMaster;
import com.lit.ims.repository.PartMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartMasterService {

    private final PartMasterRepository partMasterRepository;
    private final TransactionLogService logService;

    private PartMasterDT0 convertToDTO(PartMaster partMaster) {
        return PartMasterDT0.builder()
                .id(partMaster.getId())
                .code(partMaster.getCode())
                .name(partMaster.getName())
                .uom(partMaster.getUom())
                .status(partMaster.getStatus())
                .build();
    }

    private PartMaster convertToEntity(PartMasterDT0 dto) {
        return PartMaster.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .name(dto.getName())
                .uom(dto.getUom())
                .status(dto.getStatus())
                .build();
    }

    // ✅ Create
    public PartMasterDT0 savePart(PartMasterDT0 dto) {
        if (partMasterRepository.existsByCode(dto.getCode())) {
            throw new DataIntegrityViolationException("Part code already exists");
        }
        PartMaster saved = partMasterRepository.save(convertToEntity(dto));

        logService.log(
                "CREATE",
                "PartMaster",
                saved.getId(),
                "Created part with code: " + saved.getCode()
        );

        return convertToDTO(saved);
    }

    // ✅ Update
    public PartMasterDT0 updatePart(Long id, PartMasterDT0 dto) {
        Optional<PartMaster> optional = partMasterRepository.findById(id);
        if (optional.isEmpty()) {
            throw new RuntimeException("Part not found with ID: " + id);
        }
        PartMaster existing = optional.get();
        existing.setCode(dto.getCode());
        existing.setName(dto.getName());
        existing.setUom(dto.getUom());
        existing.setStatus(dto.getStatus());

        PartMaster updated = partMasterRepository.save(existing);

        logService.log(
                "UPDATE",
                "PartMaster",
                updated.getId(),
                "Updated part with code: " + updated.getCode()
        );

        return convertToDTO(updated);
    }

    // ✅ Get All
    public List<PartMasterDT0> getAllParts() {
        return partMasterRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ Get by ID
    public PartMasterDT0 getPartById(Long id) {
        PartMasterDT0 dto = partMasterRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Part not found with ID: " + id));

        logService.log(
                "VIEW",
                "PartMaster",
                id,
                "Viewed part with ID: " + id
        );

        return dto;
    }

    // ✅ Delete
    public void deletePart(Long id) {
        Optional<PartMaster> optional = partMasterRepository.findById(id);
        if (optional.isEmpty()) {
            throw new RuntimeException("Part not found with ID: " + id);
        }
        PartMaster part = optional.get();

        partMasterRepository.deleteById(id);

        logService.log(
                "DELETE",
                "PartMaster",
                id,
                "Deleted part with ID: " + id + " and code: " + part.getCode()
        );
    }

    // ✅ Delete Multiple
    public void deleteMultiple(List<Long> ids) {
        List<PartMaster> parts = partMasterRepository.findAllById(ids);
        partMasterRepository.deleteAllById(ids);

        String codes = parts.stream()
                .map(PartMaster::getCode)
                .collect(Collectors.joining(", "));

        logService.log(
                "DELETE_MULTIPLE",
                "PartMaster",
                null,
                "Deleted multiple parts with IDs: " + ids + " and codes: " + codes
        );
    }
}
