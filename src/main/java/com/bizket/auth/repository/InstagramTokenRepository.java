package com.bizket.auth.repository;

import java.util.Optional;
import com.bizket.common.member.domain.Member;
import com.bizket.auth.domain.InstagramToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstagramTokenRepository extends JpaRepository<InstagramToken, Long> {
    Optional<InstagramToken> findByMember(Member member);
}

