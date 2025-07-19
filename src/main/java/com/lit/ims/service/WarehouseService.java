package com.lit.ims.service;

import com.lit.ims.dto.WarehouseDTO;
import com.lit.ims.entity.Warehouse;
import com.lit.ims.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final TransactionLogService logService;

    // 🔥 Generate TRNO like WH20250701001
    private String generateNextTrno(Long companyId, Long branchId) {
        String prefix = "WH";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int nextSequence = 1;

        Optional<Warehouse> last = warehouseRepository.findTopByCompanyIdAndBranchIdOrderByIdDesc(companyId, branchId);

        if (last.isPresent()) {
            String lastTrno = last.get().getTrno();
            if (lastTrno != null && lastTrno.startsWith(prefix + date)) {
                String lastSeq = lastTrno.substring((prefix + date).length());
                try {
                    nextSequence = Integer.parseInt(lastSeq) + 1;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return prefix + date + String.format("%03d", nextSequence);
    }

    // ✅ Save from DTO
    public WarehouseDTO saveWarehouse(WarehouseDTO dto, Long companyId, Long branchId) {
        if (warehouseRepository.existsByCodeAndCompanyIdAndBranchId(dto.getCode(), companyId, branchId)) {
            throw new IllegalArgumentException("Code '" + dto.getCode() + "' already exists.");
        }

        Warehouse warehouse = fromDTO(dto);
        warehouse.setCompanyId(companyId);
        warehouse.setBranchId(branchId);
        warehouse.setTrno(generateNextTrno(companyId, branchId));

        Warehouse saved = warehouseRepository.save(warehouse);

        logService.log(
                "CREATE",
                "Warehouse",
                saved.getId(),
                "Created warehouse: " + saved.getCode() + " - " + saved.getName()
        );

        return toDTO(saved);
    }

    // ✅ Update from DTO
    @Transactional
    public WarehouseDTO updateWarehouse(Long id, WarehouseDTO dto, Long companyId, Long branchId) {
        Warehouse existing = warehouseRepository.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found with ID: " + id));

        if (!existing.getCode().equals(dto.getCode()) &&
                warehouseRepository.existsByCodeAndCompanyIdAndBranchId(dto.getCode(), companyId, branchId)) {
            throw new IllegalArgumentException("Code '" + dto.getCode() + "' already exists.");
        }

        existing.setCode(dto.getCode());
        existing.setName(dto.getName());
        existing.setStatus(dto.getStatus());
        existing.setType(dto.getType());

        Warehouse saved = warehouseRepository.save(existing);

        logService.log(
                "UPDATE",
                "Warehouse",
                saved.getId(),
                "Updated warehouse to: " + saved.getCode() + " - " + saved.getName()
        );

        return toDTO(saved);
    }

    // ✅ Get All
    public List<WarehouseDTO> getAllWarehouses(Long companyId, Long branchId) {
        return warehouseRepository.findAllByCompanyIdAndBranchId(companyId, branchId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ Get By ID
    public WarehouseDTO getWarehouseById(Long id, Long companyId, Long branchId) {
        Warehouse warehouse = warehouseRepository.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found with ID: " + id));
        return toDTO(warehouse);
    }

    // ✅ Delete
    @Transactional
    public void deleteWarehouse(Long id, Long companyId, Long branchId) {
        Warehouse warehouse = warehouseRepository.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + id));

        warehouseRepository.deleteById(id);

        logService.log(
                "DELETE",
                "Warehouse",
                id,
                "Deleted warehouse: " + warehouse.getCode() + " - " + warehouse.getName()
        );
    }

    // ✅ Delete Multiple
    @Transactional
    public void deleteMultipleWarehouses(List<Long> ids, Long companyId, Long branchId) {
        List<Warehouse> warehouses = ids.stream()
                .map(id -> warehouseRepository.findByIdAndCompanyIdAndBranchId(id, companyId, branchId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (warehouses.isEmpty()) {
            throw new IllegalArgumentException("No warehouses found to delete.");
        }

        warehouseRepository.deleteAll(warehouses);

        warehouses.forEach(w -> logService.log(
                "DELETE",
                "Warehouse",
                w.getId(),
                "Deleted warehouse: " + w.getCode() + " - " + w.getName()
        ));
    }

    // ✅ Convert Entity to DTO
    public WarehouseDTO toDTO(Warehouse warehouse) {
        return WarehouseDTO.builder()
                .id(warehouse.getId())
                .trno(warehouse.getTrno())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .status(warehouse.getStatus())
                .type(warehouse.getType())
                .build();
    }

    // ✅ Convert DTO to Entity
    public Warehouse fromDTO(WarehouseDTO dto) {
        return Warehouse.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .name(dto.getName())
                .status(dto.getStatus())
                .type(dto.getType())
                .build();
    }
}
