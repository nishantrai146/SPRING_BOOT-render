package com.lit.ims.service;

import com.lit.ims.dto.VendorItemsMasterDTO;
import com.lit.ims.entity.Item;
import com.lit.ims.entity.VendorItemsMaster;
import com.lit.ims.repository.ItemRepository;
import com.lit.ims.repository.VendorItemsMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorItemsMasterService {

    private final VendorItemsMasterRepository repository;
    private final TransactionLogService logService;
    private final ItemRepository itemRepository;

    private VendorItemsMasterDTO convertToDTO(VendorItemsMaster entity) {
        Item item = itemRepository
                .findByCodeAndCompanyIdAndBranchId(entity.getItemCode(),
                        entity.getCompanyId(),
                        entity.getBranchId())
                .orElse(null);

        return VendorItemsMasterDTO.builder()
                .id(entity.getId())
                .vendorCode(entity.getVendorCode())
                .vendorName(entity.getVendorName())
                .itemCode(entity.getItemCode())
                .itemName(entity.getItemName())
                .days(entity.getDays())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .isInventory(item != null && Boolean.TRUE.equals(item.isInventoryItem()))
                .isIqc(item != null && Boolean.TRUE.equals(item.isIqc()))
                .build();
    }

    private VendorItemsMaster convertToEntity(VendorItemsMasterDTO dto, Long companyId, Long branchId) {
        return VendorItemsMaster.builder()
                .id(dto.getId())
                .vendorCode(dto.getVendorCode())
                .vendorName(dto.getVendorName())
                .itemCode(dto.getItemCode())
                .itemName(dto.getItemName())
                .days(dto.getDays())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .status(dto.getStatus())
                .companyId(companyId)
                .branchId(branchId)
                .build();
    }

    @Transactional
    public VendorItemsMasterDTO save(VendorItemsMasterDTO dto, Long companyId, Long branchId) {
        if (repository.existsByVendorCodeAndItemCodeAndCompanyIdAndBranchId(
                dto.getVendorCode(), dto.getItemCode(), companyId, branchId)) {
            throw new DataIntegrityViolationException("Vendor Item already exists for this vendor and item.");
        }

        VendorItemsMaster saved = repository.save(convertToEntity(dto, companyId, branchId));

        logService.log(
                "CREATE",
                "VendorItemsMaster",
                saved.getId(),
                "Created VendorItem with VendorCode: " + saved.getVendorCode() + ", ItemCode: " + saved.getItemCode()
        );

        return convertToDTO(saved);
    }

    @Transactional
    public VendorItemsMasterDTO update(Long id, VendorItemsMasterDTO dto, Long companyId, Long branchId) {
        VendorItemsMaster existing = repository.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor Item not found with ID: " + id));

        if ((!existing.getVendorCode().equalsIgnoreCase(dto.getVendorCode()) ||
                !existing.getItemCode().equalsIgnoreCase(dto.getItemCode())) &&
                repository.existsByVendorCodeAndItemCodeAndCompanyIdAndBranchId(
                        dto.getVendorCode(), dto.getItemCode(), companyId, branchId)) {
            throw new DataIntegrityViolationException("Vendor Item already exists for this vendor and item.");
        }

        existing.setVendorCode(dto.getVendorCode());
        existing.setVendorName(dto.getVendorName());
        existing.setItemCode(dto.getItemCode());
        existing.setItemName(dto.getItemName());
        existing.setDays(dto.getDays());
        existing.setQuantity(dto.getQuantity());
        existing.setPrice(dto.getPrice());
        existing.setStatus(dto.getStatus());

        VendorItemsMaster updated = repository.save(existing);

        logService.log(
                "UPDATE",
                "VendorItemsMaster",
                updated.getId(),
                "Updated VendorItem with VendorCode: " + updated.getVendorCode() + ", ItemCode: " + updated.getItemCode()
        );

        return convertToDTO(updated);
    }

    public List<VendorItemsMasterDTO> getAll(Long companyId, Long branchId) {
        return repository.findByCompanyIdAndBranchId(companyId, branchId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public VendorItemsMasterDTO getById(Long id, Long companyId, Long branchId) {
        VendorItemsMaster entity = repository.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor Item not found with ID: " + id));

        logService.log(
                "VIEW",
                "VendorItemsMaster",
                id,
                "Viewed VendorItem with ID: " + id
        );

        return convertToDTO(entity);
    }

    @Transactional
    public void delete(Long id, Long companyId, Long branchId) {
        VendorItemsMaster entity = repository.findByIdAndCompanyIdAndBranchId(id, companyId, branchId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor Item not found with ID: " + id));

        repository.delete(entity);

        logService.log(
                "DELETE",
                "VendorItemsMaster",
                id,
                "Deleted VendorItem with ID: " + id + ", VendorCode: " + entity.getVendorCode()
        );
    }

    @Transactional
    public void deleteMultiple(List<Long> ids, Long companyId, Long branchId) {
        List<VendorItemsMaster> items = repository.findAllById(ids).stream()
                .filter(p -> p.getCompanyId().equals(companyId) && p.getBranchId().equals(branchId))
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            throw new IllegalArgumentException("No matching VendorItems found to delete.");
        }

        repository.deleteAll(items);

        String codes = items.stream()
                .map(VendorItemsMaster::getVendorCode)
                .collect(Collectors.joining(", "));

        logService.log(
                "DELETE_MULTIPLE",
                "VendorItemsMaster",
                null,
                "Deleted VendorItems with IDs: " + ids + " and VendorCodes: " + codes
        );
    }

    @Transactional
    public List<VendorItemsMasterDTO> getItemByVendor(String vendorCode, Long companyId, Long branchId) {
        List<VendorItemsMaster> items = repository.findByVendorCodeAndCompanyIdAndBranchId(vendorCode, companyId, branchId);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("No items found for Vendor Code: " + vendorCode);
        }

        return items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
