package com.bizket.insight.repository;

import com.bizket.insight.domain.BusinessSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusinessSubCategoryRepository extends JpaRepository<BusinessSubCategory, Long> {
    List<BusinessSubCategory> findByParentCategoryId(Long categoryId);
}