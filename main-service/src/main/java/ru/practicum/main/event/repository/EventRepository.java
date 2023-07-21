package ru.practicum.main.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.utils.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCategoryId(Long catId);

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

//    @Query(
//            "select e from Event as e " +
//                    "join fetch e.initiator as i " +
//                    "join fetch e.category as c " +
//                    "where (:users is null or i.id in :users) " +
//                    "and (:states is null or e.state in :states) " +
//                    "and (:categories is null or c.id in :categories) " +
//                    "and e.eventDate > :rangestart"
//    )
//    List<Event> findAdminEvents(@Param("users") List<Long> users,
//                                @Param("states") List<EventState> states,
//                                @Param("categories") List<Long> categories,
//                                @Param("rangeStart") LocalDateTime rangeStart,
//                                PageRequest pageable);

    //TODO Рабочий ли запрос(если нет - см выше)?LM
    List<Event> findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateIsAfter(List<Long> users, List<EventState> states, List<Long> categories,
                                                                                   LocalDateTime rangeStart, PageRequest pageable);

    @Query("select e from Event as e " +
            "where (upper(e.annotation) like upper(concat('%', :text, '%')) " +
            "or upper(e.description) like upper(concat('%', :text, '%')) or :text is null) " +
            "and (:categories is null or e.category.id in (:categories)) " +
            "and (:paid is null or e.paid = :paid) " +
            "and (cast(:rangeStart as timestamp) is null or e.eventDate >= :rangeStart) " +
            "and (cast(:rangeEnd as timestamp) is null or e.eventDate <= :rangeEnd)")
    List<Event> getEventsSort(@Param("text") String text,
                              @Param("categories") List<Long> categories,
                              @Param("paid") Boolean paid,
                              @Param("rangeStart") LocalDateTime rangeStart,
                              @Param("rangeEnd") LocalDateTime rangeEnd,
                              Pageable pageable);
}
