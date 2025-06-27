package com.lit.ims.service;

import com.lit.ims.dto.TypeMasterDTO;
import com.lit.ims.entity.TypeMaster;
import com.lit.ims.repository.TypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypeService {

    private final TypeMasterRepository repository;

    // ✅ Generate TRNO
    private String generateTrno(Long companyId, Long branchId) {
        String prefix = "TYP";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int nextSequence = 1;

        Optional<TypeMaster> last = repository.findTopByCompanyIdAndBranchIdOrderByIdDesc(companyId, branchId);
        if (last.isPresent()) {
            String lastTrno = last.get().getTrno();
            if (lastTrno != null && lastTrno.length() >= 3) {
                String lastSeq = lastTrno.substring(lastTrno.length() - 3);
                try {
                    nextSequence = Integer.parseInt(lastSeq) + 1;
                } catch (NumberFormatException ignored) {}
            }
        }

        return prefix + date + String.format("%03d", nextSequence);
    }

    // ✅ Save
    public TypeMasterDTO save(TypeMasterDTO dto, Long companyId, Long branchId) {
        if (repository.existsByNameAndCompanyIdAndBranchId(dto.getName(), companyId, branchId)) {
            throw new RuntimeException("Type Name Already Exists in this Branch");
        }

        String trno = generateTrno(companyId, branchId);

        TypeMaster type = TypeMaster.builder()
                .trno(trno)
                .name(dto.getName())
                .status(dto.getStatus())
                .companyId(companyId)
                .branchId(branchId)
                .build();

        TypeMaster saved = repository.save(type);
        return mapToDTO(saved);
    }

    // ✅ Update
    public TypeMasterDTO update(Long id, TypeMasterDTO dto, Long companyId, Long branchId) {
        TypeMaster type = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Type not found with ID: " + id));

        if (!type.getName().equals(dto.getName()) &&
                repository.existsByNameAndCompanyIdAndBranchId(dto.getName(), companyId, branchId)) {
            throw new RuntimeException("Type Name Already Exists in this Branch");
        }

        type.setName(dto.getName());
        type.setStatus(dto.getStatus());

        TypeMaster updated = repository.save(type);
        return mapToDTO(updated);
    }

    // ✅ Get One
    public TypeMasterDTO getOne(Long id) {
        TypeMaster type = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Type not found with ID: " + id));
        return mapToDTO(type);
    }

    // ✅ Get All
    public List<TypeMasterDTO> getAll(Long companyId, Long branchId) {
        return repository.findAllByCompanyIdAndBranchId(companyId, branchId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ✅ Delete One
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Type not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    // ✅ Delete Multiple
    @Transactional
    public void deleteMultiple(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("No IDs provided for deletion");
        }
        repository.deleteAllById(ids);
    }

    // ✅ Map Entity to DTO
    private TypeMasterDTO mapToDTO(TypeMaster entity) {
        return TypeMasterDTO.builder()
                .id(entity.getId())
                .trno(entity.getTrno())
                .name(entity.getName())
                .status(entity.getStatus())
                .build();
    }

    // ✅ Get TRNO by ID
    public String getTrnoById(Long id) {
        return repository.findById(id)
                .map(TypeMaster::getTrno)
                .orElse("Unknown TRNO");
    }

    // ✅ Get TRNO list by multiple IDs
    public List<String> getTrnosByIds(List<Long> ids) {
        return repository.findAllById(ids)
                .stream()
                .map(TypeMaster::getTrno)
                .toList();
    }
}
