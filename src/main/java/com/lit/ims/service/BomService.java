package com.lit.ims.service;

import com.lit.ims.dto.BomDTO;
import com.lit.ims.dto.BomItemDTO;
import com.lit.ims.entity.BOM;
import com.lit.ims.entity.BomItem;
import com.lit.ims.repository.BomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BomService {
    private final BomRepository bomRepository;

    public BOM saveBom(BomDTO bomDTO,Long companyId,Long branchId){
        if(bomRepository.existsByCodeAndCompanyIdAndBranchId(bomDTO.getCode(),companyId,branchId)){
            throw new RuntimeException("BOM code already exists");
        }

        BOM bom = BOM.builder()
                .name(bomDTO.getName())
                .code(bomDTO.getCode())
                .status(bomDTO.getStatus())
                .companyId(companyId)
                .branchId(branchId)
                .items(bomDTO.getItems().stream().map(item -> BomItem.builder()
                        .itemId(item.getItemId())
                        .itemName(item.getItemName())
                        .itemCode(item.getItemCode())
                        .uom(item.getUom())
                        .quantity(item.getQuantity())
                        .warehouseId(item.getWarehouseId())
                        .warehouseName(item.getWarehouseName())
                        .build()).toList())
                .build();

        bom.getItems().forEach(item->item.setBom(bom));
        return bomRepository.save(bom);
    }

    public List<BomDTO> getAll(Long companyId, Long branchId) {
        return bomRepository.findByCompanyIdAndBranchId(companyId, branchId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Optional<BomDTO> getById(Long id) {
        return bomRepository.findById(id).map(this::convertToDTO);
    }

    public BOM updateBom(Long id, BomDTO dto) {
        BOM existing = bomRepository.findById(id).orElseThrow(() -> new RuntimeException("BOM not found"));

        existing.setName(dto.getName());
        existing.setCode(dto.getCode());
        existing.setStatus(dto.getStatus());

        existing.getItems().clear();
        List<BomItem> newItems = dto.getItems().stream().map(item -> {
            BomItem bomItem = BomItem.builder()
                    .itemId(item.getItemId())
                    .itemName(item.getItemName())
                    .itemCode(item.getItemCode())
                    .uom(item.getUom())
                    .quantity(item.getQuantity())
                    .warehouseId(item.getWarehouseId())
                    .warehouseName(item.getWarehouseName())
                    .bom(existing)
                    .build();
            return bomItem;
        }).toList();

        existing.getItems().addAll(newItems);

        return bomRepository.save(existing);
    }

    public void deleteBom(Long id) {
        if (!bomRepository.existsById(id)) {
            throw new RuntimeException("BOM with ID " + id + " does not exist");
        }
        bomRepository.deleteById(id);
    }


    public void deleteMultiple(List<Long> ids) {
        List<Long> notFoundIds = ids.stream()
                .filter(id -> !bomRepository.existsById(id))
                .toList();

        if (!notFoundIds.isEmpty()) {
            throw new RuntimeException("BOM IDs not found: " + notFoundIds);
        }

        bomRepository.deleteAllById(ids);
    }


    private BomDTO convertToDTO(BOM bom) {
        return BomDTO.builder()
                .id(bom.getId())
                .name(bom.getName())
                .code(bom.getCode())
                .status(bom.getStatus())
                .items(bom.getItems().stream().map(item -> BomItemDTO.builder()
                        .itemId(item.getItemId())
                        .itemName(item.getItemName())
                        .itemCode(item.getItemCode())
                        .uom(item.getUom())
                        .quantity(item.getQuantity())
                        .warehouseId(item.getWarehouseId())
                        .warehouseName(item.getWarehouseName())
                        .build()).toList())
                .build();
    }
}
