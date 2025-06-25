package com.lit.ims.service;

import com.lit.ims.dto.GroupMasterDTO;
import com.lit.ims.entity.GroupMaster;
import com.lit.ims.repository.GroupMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupMasterService {

    private final GroupMasterRepository groupRepo;

    // TRNO Generator
    private String generateTrno() {
        long timestamp = System.currentTimeMillis();
        int nextSequence = 1;

        Optional<GroupMaster> last = groupRepo.findTopByOrderByIdDesc();
        if (last.isPresent()) {
            String lastTrno = last.get().getTrno();
            String lastSeq = lastTrno.substring(lastTrno.length() - 3);
            try {
                nextSequence = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignored) {}
        }

        return "GRP" + timestamp + String.format("%03d", nextSequence);
    }


    // Save
    public GroupMasterDTO save(GroupMasterDTO dto) {
        if(groupRepo.existsByName(dto.getName())){
            throw new RuntimeException("Duplicate Group Name");
        }
        String trno = generateTrno();

        GroupMaster group = GroupMaster.builder()
                .trno(trno)
                .name(dto.getName())
                .status(dto.getStatus())
                .build();

        GroupMaster saved = groupRepo.save(group);

        return mapToDTO(saved);
    }

    // Update
    public GroupMasterDTO update(Long id, GroupMasterDTO dto) {
        Optional<GroupMaster> optional = groupRepo.findById(id);
        if (optional.isEmpty()) throw new RuntimeException("Group not found");

        GroupMaster group = optional.get();
        group.setName(dto.getName());
        group.setStatus(dto.getStatus());

        GroupMaster updated = groupRepo.save(group);
        return mapToDTO(updated);
    }

    // Get One
    public GroupMasterDTO getOne(Long id) {
        GroupMaster group = groupRepo.findById(id).orElseThrow(() -> new RuntimeException("Group not found"));
        return mapToDTO(group);
    }

    // Get All
    public List<GroupMasterDTO> getAll() {
        return groupRepo.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Delete
    public void delete(Long id) {
        groupRepo.deleteById(id);
    }

    public void deleteMultiple(List<Long> ids){
        groupRepo.deleteAllById(ids);
    }

    // Mapping
    private GroupMasterDTO mapToDTO(GroupMaster entity) {
        return GroupMasterDTO.builder()
                .id(entity.getId())
                .trno(entity.getTrno())
                .name(entity.getName())
                .status(entity.getStatus())
                .build();
    }
}
