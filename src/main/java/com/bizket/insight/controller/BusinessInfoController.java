package com.bizket.insight.controller;

import com.bizket.insight.dto.BusinessCategoryDto;
import com.bizket.insight.dto.BusinessDetailCategoryDto;
import com.bizket.insight.dto.BusinessSubCategoryDto;
import com.bizket.insight.dto.CustomerAgeGroupDto;
import com.bizket.common.member.service.BusinessInfoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
public class BusinessInfoController {
    private final BusinessInfoService service;

    // 1) 고객 연령대 전체
    @GetMapping("/customer-age-groups")
    public ResponseEntity<List<CustomerAgeGroupDto>> getAgeGroups() {
        return ResponseEntity.ok(service.getAllCustomerAgeGroups());
    }

    // 2) 대분류 전체
    @GetMapping("/categories")
    public ResponseEntity<List<BusinessCategoryDto>> getCategories() {
        return ResponseEntity.ok(service.getAllCategories());
    }

    // 3) 대분류 선택 → 중분류 조회
    @GetMapping("/categories/{categoryId}/sub-categories")
    public ResponseEntity<List<BusinessSubCategoryDto>> getSubCategories(
        @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(service.getSubCategories(categoryId));
    }

    // 4) 중분류 선택 → 소분류 조회
    @GetMapping("/sub-categories/{subCategoryId}/detail-categories")
    public ResponseEntity<List<BusinessDetailCategoryDto>> getDetailCategories(
        @PathVariable Long subCategoryId
    ) {
        return ResponseEntity.ok(service.getDetailCategories(subCategoryId));
    }
}
