package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto saveCategory(NewCategoryDto newCategoryDto) {
        log.info("Добавление новой категории {}", newCategoryDto);
        Category category = CategoryMapper.INSTANCE.toCategory(newCategoryDto);
        log.info("Категория {} сохранена", category);
        return CategoryMapper.INSTANCE.toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public void deleteCategory(Long catId) {
        log.info("Удаление категории с id = {}", catId);
        checkCategory(catId);
        if (!eventRepository.findByCategoryId(catId).isEmpty()) {
            log.info("Невозможно удалить категорию. Существуют события, связанные с категорией.");
            throw new ConflictException("Невозможно удалить категорию. Существуют события, связанные с категорией.");
        }
        categoryRepository.deleteById(catId);
        log.info("Категория с id = {} удалена.", catId);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(Long catId, NewCategoryDto newCategoryDto) {
        log.info("Изменение категории {} с id = {}", newCategoryDto, catId);
        Category category = checkCategory(catId);
        category.setName(newCategoryDto.getName());
        log.info("Категория с id = {} обновлена", catId);
        return CategoryMapper.INSTANCE.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        log.info("Получение списка всех категорий: from = {}, size = {}", from, size);
        PageRequest page = PageRequest.of(from, size, Sort.by("id").descending());
        return categoryRepository.findAll(page).stream()
                .map(CategoryMapper.INSTANCE::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(Long catId) {
        log.info("Получение категории с id = {}", catId);
        Category category = checkCategory(catId);
        return CategoryMapper.INSTANCE.toCategoryDto(category);
    }

    private Category checkCategory(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(() -> {
                    log.warn("Категория c id = {} не найдена.", catId);
                    return new NotFoundException(String.format("Категория c id = %d не найдена", catId));
                }
        );
    }

}
