package ru.job4j.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.job4j.model.Achievement;
import java.util.List;

@Repository
public interface AchievementRepository extends CrudRepository<Achievement, Long> {
    List<Achievement> findAll();

    @Query("""
        from Achievement achievement
        where achievement.user.clientId = :clientId
        order by achievement.createAt desc
        """)
    List<Achievement> findByUser(Long clientId);
}
