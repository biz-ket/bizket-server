package com.bizket.marketing.domain.marketingcontent.repository;

import com.bizket.marketing.domain.marketingcontent.model.MarketingContent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingContentRepository extends JpaRepository<MarketingContent, Long> {

    List<MarketingContent> findByMemberIdOrClientToken(Long memberId, String clientToken);
}
