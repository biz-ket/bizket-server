package com.bizket.insight.repository;

import com.bizket.insight.domain.BusinessCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessCategoryRepository extends JpaRepository<BusinessCategory, Long> { }

