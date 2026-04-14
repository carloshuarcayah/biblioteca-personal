package pe.com.carlosh.bibliotecapersonal.book;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public Page<Book> findAll(Pageable pageable) {
        return bookService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Book findById(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @GetMapping("/search")
    public Page<Book> searchByTitle(@RequestParam String title, Pageable pageable) {
        return bookService.searchByTitle(title, pageable);
    }

    @GetMapping("/by-author/{authorId}")
    public Page<Book> findByAuthor(@PathVariable Long authorId, Pageable pageable) {
        return bookService.findByAuthor(authorId, pageable);
    }

    @PostMapping
    public Book create(@RequestBody Book created) {
        return bookService.create(created);
    }

    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @RequestBody Book updated) {
        return bookService.update(id, updated);
    }

    @PostMapping("/{id}/genres/{genreId}")
    public Book addGenre(@PathVariable Long id, @PathVariable Long genreId) {
        return bookService.addGenre(id, genreId);
    }

    @DeleteMapping("/{id}/genres/{genreId}")
    public Book removeGenre(@PathVariable Long id, @PathVariable Long genreId) {
        return bookService.removeGenre(id, genreId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        bookService.delete(id);
    }

    @PatchMapping("/{id}")
    public void enable(@PathVariable Long id) {
        bookService.enable(id);
    }
}
