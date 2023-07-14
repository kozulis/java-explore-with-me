package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatDto;
import ru.practicum.server.mapper.HitMapper;
import ru.practicum.server.mapper.StatsMapper;
import ru.practicum.server.model.Stat;
import ru.practicum.server.repository.StatsServerRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatServiceImpl implements StatService {

    private final StatsServerRepository statsServerRepository;

    @Transactional
    @Override
    public void saveStats(HitDto hitDto) {
        statsServerRepository.save(HitMapper.toHit(hitDto));
    }

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<Stat> stats = (uris == null || uris.isEmpty())
                ? unique
                ? statsServerRepository.findAllUnique(start, end)
                : statsServerRepository.findAll(start, end)
                : unique
                ? statsServerRepository.findAllUnique(start, end, uris)
                : statsServerRepository.findAll(start, end, uris);
        return stats.stream().map(StatsMapper::toStatDto).collect(Collectors.toList());
    }

}
