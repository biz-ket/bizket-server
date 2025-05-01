package com.bizket.common.member.service;

import com.bizket.insight.dto.BusinessCategoryDto;
import com.bizket.insight.dto.BusinessSubCategoryDto;
import com.bizket.insight.dto.BusinessDetailCategoryDto;
import com.bizket.insight.dto.CustomerAgeGroupDto;
import com.bizket.insight.repository.BusinessCategoryRepository;
import com.bizket.insight.repository.BusinessDetailCategoryRepository;
import com.bizket.insight.repository.BusinessSubCategoryRepository;
import com.bizket.insight.repository.CustomerAgeGroupRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessInfoService {
    private final CustomerAgeGroupRepository ageRepo;
    private final BusinessCategoryRepository categoryRepo;
    private final BusinessSubCategoryRepository subCategoryRepo;
    private final BusinessDetailCategoryRepository detailRepo;

    /** 1) 모든 고객 연령대 조회 */
    public List<CustomerAgeGroupDto> getAllCustomerAgeGroups() {
        return ageRepo.findAll()
            .stream()
            .map(CustomerAgeGroupDto::of)
            .collect(Collectors.toList());
    }

    /** 2) 모든 대분류 조회 */
    public List<BusinessCategoryDto> getAllCategories() {
        return categoryRepo.findAll()
            .stream()
            .map(BusinessCategoryDto::of)
            .collect(Collectors.toList());
    }

    /** 3) 선택한 대분류의 중분류 조회 */
    public List<BusinessSubCategoryDto> getSubCategories(Long categoryId) {
        return subCategoryRepo.findByParentCategoryId(categoryId)
            .stream()
            .map(BusinessSubCategoryDto::of)
            .collect(Collectors.toList());
    }

    /** 4) 선택한 중분류의 소분류 조회 */
    public List<BusinessDetailCategoryDto> getDetailCategories(Long subCategoryId) {
        return detailRepo.findBySubCategoryId(subCategoryId)
            .stream()
            .map(BusinessDetailCategoryDto::of)
            .collect(Collectors.toList());
    }
}
