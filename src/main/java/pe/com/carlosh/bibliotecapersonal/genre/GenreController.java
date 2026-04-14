package pe.com.carlosh.bibliotecapersonal.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/genres")
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public Page<Genre> findAll(Pageable pageable) {
        return genreService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Genre findById(@PathVariable Long id) {
        return genreService.findById(id);
    }

    @GetMapping("/search")
    public Page<Genre> searchByName(@RequestParam String name, Pageable pageable) {
        return genreService.searchByName(name, pageable);
    }

    @PostMapping
    public Genre create(@RequestBody Genre created) {
        return genreService.create(created);
    }

    @PutMapping("/{id}")
    public Genre update(@PathVariable Long id, @RequestBody Genre updated) {
        return genreService.update(id, updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        genreService.delete(id);
    }

    @PatchMapping("/{id}")
    public void enable(@PathVariable Long id) {
        genreService.enable(id);
    }
}