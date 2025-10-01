package com.banking.onboarding.repository;

import com.banking.onboarding.model.Log;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends MongoRepository<Log, String> {
    List<Log> findByProcessId(String processId);
    List<Log> findByCorrelationId(String correlationId);
    List<Log> findByLogType(String logType);
    List<Log> findByLevel(String level);
}
