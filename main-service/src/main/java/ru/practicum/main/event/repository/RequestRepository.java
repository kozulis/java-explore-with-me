package ru.practicum.main.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.event.model.Request;
import ru.practicum.main.event.utils.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEvent_InitiatorIdAndEventId(Long userId, Long eventId);

    List<Request> findAllByEvent_IdAndIdNotInAndStatus(Long eventId, List<Long> listId, RequestStatus status);
}
