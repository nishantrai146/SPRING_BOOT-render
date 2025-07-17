package com.lit.ims.service;

import com.lit.ims.dto.ProductionUsageDTO;
import com.lit.ims.entity.ProductionUsage;
import com.lit.ims.entity.ProductionUsageItem;
import com.lit.ims.repository.ProductionUsageRepository;
import com.lit.ims.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionUsageService {
    @Autowired
    private ProductionUsageRepository productionUsageRepository;

    public ApiResponse<String> saveProductionUsage(
            ProductionUsageDTO dto,
            Long companyId,
            Long branchId
    ){
        try {
            ProductionUsage usage=new ProductionUsage();
            usage.setTransactionNumber(dto.getTransactionNumber());
            usage.setUsageDate(dto.getUsageDate() !=null ? dto.getUsageDate(): LocalDateTime.now());
            usage.setWorkOrder(dto.getWorkOrder());
            usage.setCompanyId(companyId);
            usage.setBranchId(branchId);

            var itemList = dto.getItems().stream().map(i -> {
                ProductionUsageItem item = new ProductionUsageItem();
                item.setItemCode(i.getItemCode());
                item.setItemName(i.getItemName());
                item.setBatchNumber(i.getBatchNumber());
                item.setAvailableQty(i.getAvailableQuantity());
                item.setUsedQty(i.getUsedQty());
                item.setScrapQty(i.getScrapQty());
                item.setRemainingQty(i.getRemainingQty());
                item.setStatus(i.getStatus());
                item.setProductionUsage(usage);
                return item;
            }).collect(Collectors.toList());

            usage.setItems(itemList);
            productionUsageRepository.save(usage);

            return new ApiResponse<>(true,"Production usage saved successfully",null);
        }
        catch (Exception e){
            return new ApiResponse<>(false,"Error saving production usage: "+e.getMessage(),null);
        }
    }
}
