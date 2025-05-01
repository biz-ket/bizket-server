package com.bizket.insight.repository;

import com.bizket.insight.domain.BusinessDetailCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusinessDetailCategoryRepository extends JpaRepository<BusinessDetailCategory, Long> {
    List<BusinessDetailCategory> findBySubCategoryId(Long subCategoryId);
}