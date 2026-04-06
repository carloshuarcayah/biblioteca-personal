package pe.com.carlosh.bibliotecapersonal.author;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.carlosh.bibliotecapersonal.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorService {
    private final AuthorRepository authorRepository;

    //solo usuarios activos
    public Page<Author> findAll(Pageable pageable){
        return authorRepository.findByActiveTrue(pageable);
    }

    public Author findById(Long id){
        return authorRepository.findAuthorByIdAndActiveTrue(id).orElseThrow(()->new ResourceNotFoundException("Usuario no encontrado"));
    }

    public Page<Author> searchByName(String name, Pageable pageable){
        return authorRepository.searchByName(name, pageable);
    }

    @Transactional
    public Author create(Author created){
        return authorRepository.save(created);
    }

    @Transactional
    public Author update(Long id, Author updated){
        Author author = authorRepository.findAuthorByIdAndActiveTrue(id).orElseThrow(()->new ResourceNotFoundException("Usuario no encontrado"));

        author.update(updated);

        return authorRepository.save(author);
    }

    @Transactional
    public void delete(Long id){
        Author author = authorRepository.findAuthorByIdAndActiveTrue(id).orElseThrow(()->new ResourceNotFoundException("Usuario no encontrado"));
        author.disable();
    }

    @Transactional
    public void enable(Long id){
        Author author = authorRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Usuario no encontrado"));
        author.enable();
    }

}
