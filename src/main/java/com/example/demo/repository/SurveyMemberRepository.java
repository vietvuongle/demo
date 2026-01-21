package com.example.demo.repository;

import com.example.demo.entity.SurveyMember;
import com.example.demo.entity.key.SurveyMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository

public interface SurveyMemberRepository extends JpaRepository<SurveyMember, SurveyMemberId> {
}

