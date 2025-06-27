package com.lit.ims.service;

import com.lit.ims.dto.PartMasterDTO;
import com.lit.ims.entity.PartMaster;
import com.lit.ims.repository.PartMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartMasterService {

    private final PartMasterRepository partRepo;
    private final TransactionLogService logService;

    // ✅ Mapper: Entity → DTO
    private PartMasterDTO convertToDTO(PartMaster entity) {
        return PartMasterDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .uom(entity.getUom())
                .status(entity.getStatus())
                .build();
    }

    // ✅ Mapper: DTO → Entity
    private PartMaster convertToEntity(PartMasterDTO dto, Long companyId, Long branchId) {
        return PartMaster.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .name(dto.getName())
                .uom(dto.getUom())
                .status(dto.getStatus())
                .companyId(companyId)
                .branchId(branchId)
                .build();
    }

    // ✅ Create
    public PartMasterDTO savePart(PartMasterDTO dto, Long companyId, Long branchId) {
        if (partRepo.existsByCodeAndCompanyIdAndBranchId(dto.getCode(), companyId, branchId)) {
            throw new DataIntegrityViolationException("Part code already exists");
        }

        if (partRepo.existsByNameAndCompanyIdAndBranchId(dto.getName(), companyId, branchId)) {
            throw new DataIntegrityViolationException("Part name already exists");
        }

        PartMaster saved = partRepo.save(convertToEntity(dto, companyId, branchId));

        logService.log(
                "CREATE",
                "PartMaster",
                saved.getId(),
                "Created part with code: " + saved.getCode()
        );

        return convertToDTO(saved);
    }

    // ✅ Update
    public PartMasterDTO updatePart(Long id, PartMasterDTO dto, Long companyId, Long branchId) {
        PartMaster existing = partRepo.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Part not found with ID: " + id));

        existing.setCode(dto.getCode());
        existing.setName(dto.getName());
        existing.setUom(dto.getUom());
        existing.setStatus(dto.getStatus());

        PartMaster updated = partRepo.save(existing);

        logService.log(
                "UPDATE",
                "PartMaster",
                updated.getId(),
                "Updated part with code: " + updated.getCode()
        );

        return convertToDTO(updated);
    }

    // ✅ Get All
    public List<PartMasterDTO> getAllParts(Long companyId, Long branchId) {
        return partRepo.findByCompanyIdAndBranchId(companyId, branchId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ Get by ID
    public PartMasterDTO getPartById(Long id, Long companyId, Long branchId) {
        PartMaster entity = partRepo.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Part not found with ID: " + id));

        logService.log(
                "VIEW",
                "PartMaster",
                id,
                "Viewed part with ID: " + id
        );

        return convertToDTO(entity);
    }

    // ✅ Delete
    public void deletePart(Long id, Long companyId, Long branchId) {
        PartMaster part = partRepo.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Part not found with ID: " + id));

        partRepo.deleteById(id);

        logService.log(
                "DELETE",
                "PartMaster",
                id,
                "Deleted part with ID: " + id + " and code: " + part.getCode()
        );
    }

    // ✅ Delete Multiple
    public void deleteMultiple(List<Long> ids, Long companyId, Long branchId) {
        List<PartMaster> parts = partRepo.findAllById(ids).stream()
                .filter(p -> p.getCompanyId().equals(companyId) && p.getBranchId().equals(branchId))
                .collect(Collectors.toList());

        if (parts.isEmpty()) {
            throw new RuntimeException("No parts found to delete.");
        }

        partRepo.deleteAll(parts);

        String codes = parts.stream()
                .map(PartMaster::getCode)
                .collect(Collectors.joining(", "));

        logService.log(
                "DELETE_MULTIPLE",
                "PartMaster",
                null,
                "Deleted parts with IDs: " + ids + " and codes: " + codes
        );
    }
}
