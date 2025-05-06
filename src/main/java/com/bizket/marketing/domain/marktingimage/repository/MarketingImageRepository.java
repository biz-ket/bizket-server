package com.bizket.marketing.domain.marktingimage.repository;

import com.bizket.marketing.domain.marktingimage.model.MarketingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingImageRepository extends JpaRepository<MarketingImage, Long> {

}
