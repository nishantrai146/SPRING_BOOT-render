package com.lit.ims.service;

import com.lit.ims.dto.GroupMasterDTO;
import com.lit.ims.entity.GroupMaster;
import com.lit.ims.repository.GroupMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupMasterService {

    private final GroupMasterRepository groupRepo;

    // ✅ TRNO Generator
    private String generateTrno(Long companyId, Long branchId) {
        long timestamp = Instant.now().toEpochMilli();
        int nextSequence = 1;

        Optional<GroupMaster> last = groupRepo.findTopByCompanyIdAndBranchIdOrderByIdDesc(companyId, branchId);
        if (last.isPresent()) {
            String lastTrno = last.get().getTrno();
            String lastSeq = lastTrno.substring(lastTrno.length() - 3);
            try {
                nextSequence = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignored) {}
        }

        return "GRP" + timestamp + String.format("%03d", nextSequence);
    }

    // ✅ Save
    public GroupMasterDTO save(GroupMasterDTO dto, Long companyId, Long branchId) {
        if (groupRepo.existsByNameAndCompanyIdAndBranchId(dto.getName(), companyId, branchId)) {
            throw new RuntimeException("Group name already exists.");
        }

        String trno = generateTrno(companyId, branchId);

        GroupMaster group = GroupMaster.builder()
                .trno(trno)
                .name(dto.getName())
                .status(dto.getStatus())
                .companyId(companyId)
                .branchId(branchId)
                .build();

        GroupMaster saved = groupRepo.save(group);
        return mapToDTO(saved);
    }

    // ✅ Update
    public GroupMasterDTO update(Long id, GroupMasterDTO dto, Long companyId, Long branchId) {
        GroupMaster group = groupRepo.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        group.setName(dto.getName());
        group.setStatus(dto.getStatus());

        GroupMaster updated = groupRepo.save(group);
        return mapToDTO(updated);
    }

    // ✅ Get One
    public GroupMasterDTO getOne(Long id, Long companyId, Long branchId) {
        GroupMaster group = groupRepo.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        return mapToDTO(group);
    }

    // ✅ Get All
    public List<GroupMasterDTO> getAll(Long companyId, Long branchId) {
        return groupRepo.findByCompanyIdAndBranchId(companyId, branchId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ✅ Delete
    public void delete(Long id, Long companyId, Long branchId) {
        GroupMaster group = groupRepo.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        groupRepo.delete(group);
    }

    // ✅ Delete Multiple
    public void deleteMultiple(List<Long> ids, Long companyId, Long branchId) {
        List<GroupMaster> groups = groupRepo.findAllById(ids).stream()
                .filter(g -> g.getCompanyId().equals(companyId) && g.getBranchId().equals(branchId))
                .collect(Collectors.toList());

        if (groups.isEmpty()) {
            throw new RuntimeException("No records found to delete.");
        }

        groupRepo.deleteAll(groups);
    }

    // ✅ Mapping
    private GroupMasterDTO mapToDTO(GroupMaster entity) {
        return GroupMasterDTO.builder()
                .id(entity.getId())
                .trno(entity.getTrno())
                .name(entity.getName())
                .status(entity.getStatus())
                .build();
    }
}
