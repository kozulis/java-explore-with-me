package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto saveUser(NewUserRequest newUserRequest);

    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    void deleteUserById(Long id);

}
