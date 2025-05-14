package com.bizket.marketing.domain.marketingcontent.repository;

import com.bizket.marketing.domain.marketingcontent.model.MarketingContent;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingContentRepository extends JpaRepository<MarketingContent, Long> {

    Page<MarketingContent> findByMemberId(Long memberId, Pageable pageable);

    Page<MarketingContent> findByClientToken(String clientToken, Pageable pageable);

    List<MarketingContent> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<MarketingContent> findByClientTokenOrderByCreatedAtDesc(String clientToken);

    @Query("""
            SELECT DISTINCT c FROM MarketingContent c
            LEFT JOIN c.hashtags h
            WHERE c.member.id = :memberId
              AND (
                LOWER(c.prompt) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.generatedContent) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY c.createdAt DESC
        """)
    Page<MarketingContent> searchByMemberIdAndKeyword(
        @Param("memberId") Long memberId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    @Query("""
            SELECT DISTINCT c FROM MarketingContent c
            LEFT JOIN c.hashtags h
            WHERE c.clientToken = :clientToken
              AND (
                LOWER(c.prompt) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.generatedContent) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY c.createdAt DESC
        """)
    Page<MarketingContent> searchByClientTokenAndKeyword(
        @Param("clientToken") String clientToken,
        @Param("keyword") String keyword,
        Pageable pageable
    );

}
