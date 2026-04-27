package ru.practicum.categories;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.service.CategoryService;
import ru.practicum.dto.categories.CategoryDto;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto dto) {
        CategoryDto created = categoryService.createCategory(dto);
        return ResponseEntity.ok(created);
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody CategoryDto dto
    ) {
        CategoryDto updated = categoryService.updateCategory(catId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
        return ResponseEntity.noContent().build(); // 204
    }
}