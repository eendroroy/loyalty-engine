package io.github.eendroroy.loyalty.repository;

import io.github.eendroroy.loyalty.entity.Rule;
import io.github.eendroroy.loyalty.enums.RuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RuleRepository extends JpaRepository<Rule, Long>, JpaSpecificationExecutor<Rule> {

    @Query("SELECT DISTINCT r FROM Rule r LEFT JOIN FETCH r.actions")
    List<Rule> findAllWithActions();

    @Query("SELECT DISTINCT r FROM Rule r LEFT JOIN FETCH r.actions WHERE r.id = :id")
    Optional<Rule> findByIdWithActions(@Param("id") Long id);

    List<Rule> findByStatusAndFrequencyIsNotNull(RuleStatus status);

    List<Rule> findByStatus(RuleStatus status);

    @Modifying
    @Query("UPDATE Rule r SET r.lastRunAt = :timestamp WHERE r.id = :id")
    void updateLastRunAt(@Param("id") Long id, @Param("timestamp") LocalDateTime timestamp);
}
