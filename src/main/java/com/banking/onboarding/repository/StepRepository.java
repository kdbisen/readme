package com.banking.onboarding.repository;

import com.banking.onboarding.model.Step;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StepRepository extends MongoRepository<Step, String> {
    List<Step> findByProcessId(String processId);
    List<Step> findByProcessIdOrderByStepOrderAsc(String processId);
    List<Step> findByProcessIdAndStatus(String processId, String status);
}
