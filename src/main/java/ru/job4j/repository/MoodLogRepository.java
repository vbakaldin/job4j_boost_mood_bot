package ru.job4j.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.job4j.model.MoodLog;
import java.util.List;

@Repository
public interface MoodLogRepository extends CrudRepository<MoodLog, Long> {
    List<MoodLog> findAll();

    @Query("""
        from MoodLog log
        where log.user.clientId = :clientId
        and log.createdAt between :from and :to
        order by log.createdAt desc
        """)
    List<MoodLog> findMoodLogs(
            Long clientId,
            long from,
            long to
    );
}