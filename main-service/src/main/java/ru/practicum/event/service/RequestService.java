package ru.practicum.event.service;

import ru.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequest(Long userId, Long eventId,
                                                      EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    List<ParticipationRequestDto> getPrivateEventRequests(Long userId);

    ParticipationRequestDto addPrivateEventRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelPrivateEventRequest(Long userId, Long requestId);

}
