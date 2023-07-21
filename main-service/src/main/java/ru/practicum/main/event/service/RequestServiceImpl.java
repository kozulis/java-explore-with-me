package ru.practicum.main.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.event.dto.ParticipationRequestDto;
import ru.practicum.main.event.mapper.RequestMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.Request;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.event.repository.RequestRepository;
import ru.practicum.main.event.utils.RequestStatus;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        checkUser(userId);
        checkEvent(eventId);
        List<Request> requests = requestRepository.findAllByEvent_InitiatorIdAndEventId(userId, eventId);
        log.info("Получение информации о запросах на участии в событии с id = {} " +
                "от пользователя с id = {}.", eventId, userId);
        return requests.stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequest(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        checkUser(userId);
        Event event = checkEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("Изменить данные события может только организатор.");
            throw new ConflictException("Изменить данные события может только организатор.");
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            log.warn("Лимит заявок равен 0 или отключена пре-модерация заявок.");
            throw new ConflictException("Лимит заявок равен 0 или отключена пре-модерация заявок.");
        }

        List<Long> requestIds = eventRequestStatusUpdateRequest.getRequestIds();
        switch (eventRequestStatusUpdateRequest.getStatus()) {
            case REJECTED:
                return addStatusRejected(requestIds);
            case CONFIRMED:
                return addStatusConfirmed(requestIds, event);
            default:
                log.warn("Указан неверный статус.");
                throw new ConflictException("Указан неверный статус");
        }
    }

    private EventRequestStatusUpdateResult addStatusRejected(List<Long> requestIds) {
        List<Request> requests = requestRepository.findAllById(requestIds);
        checkStatusIsPending(requests);
        requests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
        requestRepository.saveAll(requests);
        List<ParticipationRequestDto> rejectedRequests = requests
                .stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .collect(Collectors.toList());
        return new EventRequestStatusUpdateResult(List.of(), rejectedRequests);
    }

    private EventRequestStatusUpdateResult addStatusConfirmed(List<Long> requestIds, Event event) {
        long confirmedRequestsCount = event.getConfirmedRequests();
        int limit = event.getParticipantLimit();

        if (limit > 0 && confirmedRequestsCount == limit) {
            log.warn("Лимит заявок для события исчерпан.");
            throw new ConflictException("Лимит заявок для события исчерпан.");
        }
        List<Request> confirmedRequests;
        if (requestIds.size() > (limit - confirmedRequestsCount)) {
            confirmedRequests = requestRepository.findAllById(requestIds
                    .stream()
                    .limit(limit - confirmedRequestsCount)
                    .collect(Collectors.toList()));
        } else {
            confirmedRequests = requestRepository.findAllById(requestIds);
        }
        checkStatusIsPending(confirmedRequests);
        for (Request req : confirmedRequests) {
            req.setStatus(RequestStatus.CONFIRMED);
            confirmedRequestsCount++;
        }
        List<Request> rejectedRequests = new ArrayList<>();
        List<Long> listId = confirmedRequests.stream().map(Request::getId).collect(Collectors.toList());
        if (limit == confirmedRequestsCount) {
            rejectedRequests = requestRepository.findAllByEvent_IdAndIdNotInAndStatus(
                            event.getId(), listId, RequestStatus.PENDING)
                    .stream()
                    .peek(request -> request.setStatus(RequestStatus.REJECTED))
                    .collect(Collectors.toList());
        }
        List<Request> updateRequests = new ArrayList<>(confirmedRequests);
        updateRequests.addAll(rejectedRequests);
        requestRepository.saveAll(updateRequests);
        event.setConfirmedRequests(confirmedRequestsCount);
        eventRepository.save(event);
        return new EventRequestStatusUpdateResult(
                confirmedRequests.stream()
                        .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                        .collect(Collectors.toList()),
                rejectedRequests.stream()
                        .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                        .collect(Collectors.toList()));
    }

    private void checkStatusIsPending(List<Request> requests) {
        boolean isConfirmedRequest = requests.stream()
                .anyMatch(request -> !request.getStatus().equals(RequestStatus.PENDING));
        if (isConfirmedRequest) {
            log.warn("Статус можно изменить только у заявок, находящихся в состоянии ожидания.");
            throw new ConflictException("Статус можно изменить только у заявок, находящихся в состоянии ожидания.");
        }
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
                    log.warn("Пользователь с id = {} не найден.", userId);
                    return new NotFoundException(String.format("Пользователь с id %d не найден.", userId));
                }
        );
    }

    private Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> {
                    log.warn("Событие с id = {} не найдена.", eventId);
                    return new NotFoundException(String.format("Событие с id %d не найдено.", eventId));
                }
        );
    }

}
