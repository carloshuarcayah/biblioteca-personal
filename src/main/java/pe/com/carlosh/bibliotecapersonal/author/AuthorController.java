package pe.com.carlosh.bibliotecapersonal.author;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/authors")
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping
    public Page<Author> findAll(Pageable pageable){
        return authorService.findAll(pageable);
    }

    //SI NO ESTA ELIMINADO (Soft)
    @GetMapping("/{id}")
    public Author findById(@PathVariable Long id){
        return authorService.findById(id);
    }

    @GetMapping("/search")
    public Page<Author> searchByName(@RequestParam String name,Pageable pageable){
        return authorService.searchByName(name, pageable);
    }

    @PostMapping
    public Author create(@RequestBody Author created){
        return authorService.create(created);
    }

    @PutMapping("/{id}")
    public Author update(@PathVariable Long id, @RequestBody Author updated){
        return  authorService.update(id,updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        authorService.delete(id);
    }

    @PatchMapping("/{id}")
    public void enable(@PathVariable Long id){
        authorService.enable(id);
    }
}
