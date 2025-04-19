package com.bizket.common.member.repository;

import com.bizket.common.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByProviderIdAndOauth2Provider(String providerId, String oauth2Provider);

}
