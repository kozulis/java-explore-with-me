package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto addNewCompilation(NewCompilationDto newCompilationDto) {
        log.info("Добавление подборки событий {}.", newCompilationDto);
        List<Event> eventList = newCompilationDto.getEvents() != null ? eventRepository.findAllById(
                newCompilationDto.getEvents()) : Collections.emptyList();
        Compilation compilation = CompilationMapper.INSTANCE.toCompilation(newCompilationDto, eventList);
        log.info("Подборка добавлена.");
        return CompilationMapper.INSTANCE.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки с id = {}.", compId);
        checkCompilation(compId);
        compilationRepository.deleteById(compId);
        log.info("Подборка с id = {} удалена.", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("Изменение данных подборки с id = {}", compId);
        Compilation compilation = checkCompilation(compId);
        Optional.ofNullable(updateCompilationRequest.getEvents()).ifPresent(longs ->
                compilation.setEvents(eventRepository.findAllById(longs)));
        Optional.ofNullable(updateCompilationRequest.getPinned()).ifPresent(compilation::setPinned);
        Optional.ofNullable(updateCompilationRequest.getTitle()).ifPresent(compilation::setTitle);
        log.info("Данные подборки с id = {} изменены.", compId);
        return CompilationMapper.INSTANCE.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение подборки событий по параметрам: pinned {}, from {}, size {}", pinned, from, size);
        PageRequest pageRequest = PageRequest.of(from, size);
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageRequest);
        }
        log.info("Получена подборка событий по параметрам: pinned {}, from {}, size {}", pinned, from, size);

        return compilations.stream()
                .map(CompilationMapper.INSTANCE::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Получение подборки событий с id = {}", compId);
        Compilation compilation = checkCompilation(compId);
        log.info("Получена подборка событий с id = {}", compId);
        return CompilationMapper.INSTANCE.toCompilationDto(compilation);
    }

    private Compilation checkCompilation(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(() -> {
                    log.warn("Подборка с id = {} не найдена.", compId);
                    return new NotFoundException(String.format("Подборка с id = %d не найдено.", compId));
                }
        );
    }

}
