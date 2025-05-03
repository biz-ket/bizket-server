package com.bizket.marketing.domain.marketingkeyword.model;

import com.bizket.marketing.domain.marketingcontent.model.MarketingContent;
import com.bizket.marketing.domain.marketingkeyword.type.KeywordType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class MarketingKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private KeywordType type;

    private String keyword;

    @ManyToOne
    @JoinColumn(name = "content_id")
    private MarketingContent content;
}

