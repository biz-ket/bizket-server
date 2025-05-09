package com.bizket.marketing.domain.marketingcontent.repository;

import static org.hibernate.jpa.HibernateHints.HINT_CACHEABLE;

import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bizket.marketing.domain.marketingcontent.model.MarketingContent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingContentRepository extends JpaRepository<MarketingContent, Long> {

    List<MarketingContent> findByMemberIdOrClientToken(Long memberId, String clientToken);
    Page<MarketingContent> findByMemberIdOrClientToken(Long memberId, String clientToken, Pageable pageable);
    List<MarketingContent> findByMemberIdOrClientTokenOrderByCreatedAtDesc(
        Long memberId,
        String clientToken
    );
    // prompt, generatedContent, hashtags.name 에서의 부분일치 페이징 조회
    @Query(
        value = """
        SELECT DISTINCT c
        FROM MarketingContent c
        LEFT JOIN c.hashtags h
        WHERE ( ( :memberId IS NULL OR c.member.id = :memberId )
           OR ( :clientToken IS NULL OR c.clientToken = :clientToken ) )
          AND (
            LOWER(c.prompt) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.generatedContent) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY c.createdAt DESC   
        """,
        countQuery = """
        SELECT COUNT(DISTINCT c)
        FROM MarketingContent c
        LEFT JOIN c.hashtags h
        WHERE ( ( :memberId IS NULL OR c.member.id = :memberId )
           OR ( :clientToken IS NULL OR c.clientToken = :clientToken ) )
          AND (
            LOWER(c.prompt) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.generatedContent) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        """
    )
    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "false"))
    Page<MarketingContent> searchByPromptContentOrHashtag(
        @Param("memberId") Long memberId,
        @Param("clientToken") String clientToken,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
