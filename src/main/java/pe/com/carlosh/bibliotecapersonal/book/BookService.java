package pe.com.carlosh.bibliotecapersonal.book;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.carlosh.bibliotecapersonal.author.Author;
import pe.com.carlosh.bibliotecapersonal.author.AuthorRepository;
import pe.com.carlosh.bibliotecapersonal.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findByActiveTrue(pageable);
    }

    public Book findById(Long id) {
        return bookRepository.findBookByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
    }

    public Page<Book> searchByTitle(String title, Pageable pageable) {
        return bookRepository.searchByTitle(title, pageable);
    }

    public Page<Book> findByAuthor(Long authorId, Pageable pageable) {
        return bookRepository.findByAuthorIdAndActiveTrue(authorId, pageable);
    }

    @Transactional
    public Book create(Book received) {
        Author author = resolveAuthor(received);
        Book book = new Book(received.getTitle(), received.getPublishedDate(), received.getPages(), author);
        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Long id, Book updated) {
        Book book = bookRepository.findBookByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
        book.update(updated);
        if (updated.getAuthor() != null && updated.getAuthor().getId() != null) {
            book.changeAuthor(resolveAuthor(updated));
        }
        return bookRepository.save(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = bookRepository.findBookByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
        book.disable();
    }

    @Transactional
    public void enable(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
        book.enable();
    }

    private Author resolveAuthor(Book received) {
        if (received.getAuthor() == null || received.getAuthor().getId() == null) {
            throw new ResourceNotFoundException("Autor requerido");
        }
        return authorRepository.findAuthorByIdAndActiveTrue(received.getAuthor().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado"));
    }
}