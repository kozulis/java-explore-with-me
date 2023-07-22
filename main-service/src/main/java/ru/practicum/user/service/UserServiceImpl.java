package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto saveUser(NewUserRequest newUserRequest) {
        User newUser = userRepository.save(UserMapper.INSTANCE.toUser(newUserRequest));
        log.info("Пользователь {} сохранен.", newUser);
        return UserMapper.INSTANCE.toUserDto(newUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        List<User> userList;
        PageRequest page = PageRequest.of(from, size);

        if (ids == null) {
            userList = userRepository.findAll(page).toList();
        } else {
            userList = userRepository.findAllByIdIn(ids, page);
        }
        log.info("Получение списка пользователей: ids {}, from = {}, size = {}", ids, from, size);
        return userList.stream()
                .map(UserMapper.INSTANCE::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUserById(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> {
                    log.warn("Пользователь с id = {} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id %d не найден", userId));
                }
        );
        userRepository.deleteById(userId);
        log.info("Пользователь с id = {} удален.", userId);
    }
}
