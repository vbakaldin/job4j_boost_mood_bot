package ru.job4j.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.job4j.model.MoodLog;
import ru.job4j.model.User;

import java.util.List;
import java.util.stream.Stream;

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

    List<MoodLog> findByUserId(Long userId);

    Stream<MoodLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
    select distinct log.user
    from MoodLog log
    where log.user not in (
        select todayLog.user
        from MoodLog todayLog
        where todayLog.createdAt between :from and :to
    )
    """)
    List<User> findUsersWhoDidNotVoteToday(long from, long to);

    @Query("""
        from MoodLog log
        where log.user.id = :userId
        and log.createdAt >= :weekStart
        order by log.createdAt desc
        """)
    List<MoodLog> findMoodLogsForWeek(Long userId, long weekStart);

    @Query("""
        from MoodLog log
        where log.user.id = :userId
        and log.createdAt >= :monthStart
        order by log.createdAt desc
        """)
    List<MoodLog> findMoodLogsForMonth(Long userId, long monthStart);
}