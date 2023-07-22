package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryPublicController {

    private final CategoryService categoryService;

    @GetMapping
    List<CategoryDto> getAllCategory(@RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                     @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Запрос на получение всех категорий: from = {}, size = {}", from, size);
        return categoryService.getAll(from, size);
    }

    @GetMapping("/{catId}")
    CategoryDto getCategoryById(@PathVariable Long catId) {
        log.info("Запрос на получение категории с id = {}", catId);
        return categoryService.getById(catId);
    }
}
