package com.bizket.marketing.domain.hashtag.repository;

import com.bizket.marketing.domain.hashtag.model.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

}
