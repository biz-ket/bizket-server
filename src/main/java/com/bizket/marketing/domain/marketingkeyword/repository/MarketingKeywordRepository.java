package com.bizket.marketing.domain.marketingkeyword.repository;

import com.bizket.marketing.domain.marketingkeyword.model.MarketingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingKeywordRepository extends JpaRepository<MarketingKeyword, Long> {

}
