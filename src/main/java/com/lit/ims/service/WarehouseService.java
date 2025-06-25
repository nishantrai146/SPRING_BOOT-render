package com.lit.ims.service;

import com.lit.ims.dto.WarehouseDTO;
import com.lit.ims.entity.Warehouse;
import com.lit.ims.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final TransactionLogService logService;

    private String generateNextTrno() {
        long timestamp = System.currentTimeMillis();
        int nextSequence = 1;
        Optional<Warehouse> last = warehouseRepository.findTopByOrderByIdDesc();
        if (last.isPresent()) {
            String lastTrno = last.get().getTrno();
            String lastSeq = lastTrno.substring(lastTrno.length() - 3);
            try {
                nextSequence = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return "WH" + timestamp + String.format("%03d", nextSequence);
    }

    public WarehouseDTO saveWarehouse(Warehouse warehouse) {
        if (warehouseRepository.existsByCode(warehouse.getCode())) {
            throw new RuntimeException("Code '" + warehouse.getCode() + "' already exists.");
        }
        warehouse.setTrno(generateNextTrno());
        Warehouse saved = warehouseRepository.save(warehouse);

        logService.log(
                "CREATE",
                "Warehouse",
                saved.getId(),
                "Created warehouse: " + saved.getCode() + " - " + saved.getName()
        );

        return toDTO(saved);
    }

    public WarehouseDTO updateWarehouse(Long id, Warehouse updated) {
        Warehouse existing = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        if (!existing.getCode().equals(updated.getCode()) &&
                warehouseRepository.existsByCode(updated.getCode())) {
            throw new RuntimeException("Code '" + updated.getCode() + "' already exists.");
        }

        existing.setCode(updated.getCode());
        existing.setName(updated.getName());
        existing.setStatus(updated.getStatus());
        Warehouse saved = warehouseRepository.save(existing);

        logService.log(
                "UPDATE",
                "Warehouse",
                saved.getId(),
                "Updated warehouse to: " + saved.getCode() + " - " + saved.getName()
        );

        return toDTO(saved);
    }

    public List<WarehouseDTO> getAllWarehouses() {
        return warehouseRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<Warehouse> getWarehouseById(Long id) {
        return warehouseRepository.findById(id);
    }

    public boolean deleteWarehouse(Long id) {
        Optional<Warehouse> existing = warehouseRepository.findById(id);
        if (existing.isPresent()) {
            warehouseRepository.deleteById(id);

            logService.log(
                    "DELETE",
                    "Warehouse",
                    id,
                    "Deleted warehouse: " + existing.get().getCode() + " - " + existing.get().getName()
            );

            return true;
        }
        return false;
    }

    public void deleteMultipleWarehouses(List<Long> ids) {
        List<Warehouse> warehouses = warehouseRepository.findAllById(ids);
        warehouseRepository.deleteAllById(ids);

        for (Warehouse w : warehouses) {
            logService.log(
                    "DELETE",
                    "Warehouse",
                    w.getId(),
                    "Deleted warehouse: " + w.getCode() + " - " + w.getName()
            );
        }
    }

    private WarehouseDTO toDTO(Warehouse warehouse) {
        return WarehouseDTO.builder()
                .id(warehouse.getId())
                .trno(warehouse.getTrno())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .status(warehouse.getStatus())
                .build();
    }
}
