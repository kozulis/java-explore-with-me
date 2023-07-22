package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Mapper(uses = {UserMapper.class, CategoryMapper.class},
        imports = {LocalDateTime.class, Location.class})
public interface EventMapper {

    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "createdOn", expression = "java(LocalDateTime.now())", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "lat", source = "newEventDto.location.lat")
    @Mapping(target = "lon", source = "newEventDto.location.lon")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "user")
    Event toEvent(NewEventDto newEventDto, Category category, User user);

    @Mapping(target = "location.lat", source = "lat")
    @Mapping(target = "location.lon", source = "lon")
    EventFullDto toEventFullDto(Event event);

    EventShortDto toEventShortDto(Event event);

}
