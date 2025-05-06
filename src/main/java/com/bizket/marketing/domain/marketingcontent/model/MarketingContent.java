package com.bizket.marketing.domain.marketingcontent.model;

import com.bizket.common.member.domain.Member;
import com.bizket.marketing.domain.hashtag.model.Hashtag;
import com.bizket.marketing.domain.marketingkeyword.model.MarketingKeyword;
import com.bizket.marketing.domain.marktingimage.model.MarketingImage;
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

    private String prompt;
    private String generatedContent;
    private LocalDateTime createdAt;
    private String clientToken;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MarketingImage> images = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "content_hashtag",
        joinColumns = @JoinColumn(name = "content_id"),
        inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private List<Hashtag> hashtags = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "content_keyword",
        joinColumns = @JoinColumn(name = "content_id"),
        inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    private List<MarketingKeyword> keywords = new ArrayList<>();

    @Builder
    private MarketingContent(Long id, String platform, String prompt, String generatedContent, LocalDateTime createdAt,
        String clientToken, Member member, List<MarketingImage> images, List<Hashtag> hashtags,
        List<MarketingKeyword> keywords
    ) {
        this.id = id;
        this.platform = platform;
        this.prompt = prompt;
        this.generatedContent = generatedContent;
        this.createdAt = createdAt;
        this.clientToken = clientToken;
        this.member = member;
        this.images = images;
        this.hashtags = hashtags;
        this.keywords = keywords;
    }

    public static MarketingContent of(
        String platform,
        String prompt,
        String generatedContent,
        List<Hashtag> hashtags
    ) {
        return MarketingContent.builder()
            .platform(platform)
            .prompt(prompt)
            .keywords(new ArrayList<>())
            .generatedContent(generatedContent)
            .hashtags(hashtags)
            .images(new ArrayList<>())
            .build();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void addKeyword(MarketingKeyword keyword) {
        keywords.add(keyword);
        keyword.assignContent(this);
    }

    public void addImage(MarketingImage image) {
        images.add(image);
        image.assignContent(this);
    }

    public void assignMember(Member member) {
        this.member = member;
    }

    public void assignClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

}
