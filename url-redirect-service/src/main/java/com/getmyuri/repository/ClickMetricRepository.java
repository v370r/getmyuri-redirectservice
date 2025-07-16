package com.getmyuri.repository;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.getmyuri.model.ClickMetric;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;

@Repository
public interface ClickMetricRepository extends JpaRepository<ClickMetric, Long> {

        @Modifying
        @Transactional
        @Query(value = """
                            INSERT INTO click_metrics (username, alias, click_count, click_date)
                            VALUES (:username, :alias, :clickCount, :clickDate)
                            ON CONFLICT (username, alias)
                            DO UPDATE SET
                                click_count = click_metrics.click_count + EXCLUDED.click_count,
                                click_date = EXCLUDED.click_date
                        """, nativeQuery = true)
        void upsertClickMetric(@Param("username") String username,
                        @Param("alias") String alias,
                        @Param("clickCount") long clickCount,
                        @Param("clickDate") LocalDateTime clickDate);

        List<ClickMetric> findByUsernameOrderByClickDateDesc(String username);
}
