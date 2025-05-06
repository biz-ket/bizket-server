package com.bizket.marketing.domain.marktingimage.model;

import com.bizket.marketing.domain.marketingcontent.model.MarketingContent;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class MarketingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "content_id")
    private MarketingContent content;

    private int sequence;
    private String url;

    @Builder
    private MarketingImage(String url, int sequence) {
        this.url = url;
        this.sequence = sequence;
    }

    public static MarketingImage of(String url, int sequence) {
        return new MarketingImage(url, sequence);
    }

    public void assignContent(MarketingContent content) {
        this.content = content;
    }
}
