package com.bizket.marketing.domain.marketingcontent;

import com.bizket.common.member.domain.Member;
import com.bizket.marketing.domain.hashtag.Hashtag;
import com.bizket.marketing.domain.marketingkeyword.MarketingKeyword;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class MarketingContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String platform;
    private String imageUrl;
    private String profileText;
    private String generatedText;
    private LocalDateTime createdAt;
    private String clientToken;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToMany
    @JoinTable(
        name = "content_hashtag",
        joinColumns = @JoinColumn(name = "content_id"),
        inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private List<Hashtag> hashtags = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    private List<MarketingKeyword> keywords = new ArrayList<>();

    @Builder
    private MarketingContent(Long id, String platform, String imageUrl, String profileText, String generatedText,
        LocalDateTime createdAt, String clientToken, Member member, List<Hashtag> hashtags,
        List<MarketingKeyword> keywords) {
        this.id = id;
        this.platform = platform;
        this.imageUrl = imageUrl;
        this.profileText = profileText;
        this.generatedText = generatedText;
        this.createdAt = createdAt;
        this.clientToken = clientToken;
        this.member = member;
        this.hashtags = hashtags;
        this.keywords = keywords;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
}
