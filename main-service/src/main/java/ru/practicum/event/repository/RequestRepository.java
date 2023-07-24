package ru.practicum.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.model.Request;
import ru.practicum.event.utils.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEvent_InitiatorIdAndEventId(Long userId, Long eventId);

    List<Request> findAllByEvent_IdAndIdNotInAndStatus(Long eventId, List<Long> listId, RequestStatus status);

    List<Request> findAllByRequesterId(Long userId);

    Boolean existsByRequesterIdAndEventId(Long userId, Long eventId);
}
