package com.bizket.auth.repository;

import com.bizket.auth.domain.InstagramToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstagramTokenRepository extends JpaRepository<InstagramToken, Long> {
}

