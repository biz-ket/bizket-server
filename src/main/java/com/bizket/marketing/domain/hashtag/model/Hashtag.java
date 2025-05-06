package com.bizket.marketing.domain.hashtag.model;

import com.bizket.marketing.domain.marketingcontent.model.MarketingContent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "hashtags")
    private List<MarketingContent> contents = new ArrayList<>();

    @Builder
    private Hashtag(String name) {
        this.name = name;
    }

    public static Hashtag of(String name) {
        return new Hashtag(name);
    }

}
