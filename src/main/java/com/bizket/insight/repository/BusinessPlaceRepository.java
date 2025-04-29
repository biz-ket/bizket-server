package com.bizket.insight.repository;

import com.bizket.common.member.domain.Member;
import com.bizket.insight.domain.BusinessPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessPlaceRepository extends JpaRepository<BusinessPlace, Long> {

}
