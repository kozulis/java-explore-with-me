package ru.practicum.main.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.mapper.CategoryMapper;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;

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
        Category category = CategoryMapper.INSTANCE.toCategory(newCategoryDto);
        log.info("Категория {} сохранена", category);
        return CategoryMapper.INSTANCE.toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public void deleteCategory(Long catId) {
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
        Category category = checkCategory(catId);
        log.info("Получение категории с id = {}", catId);
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
