package pe.com.carlosh.bibliotecapersonal.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.carlosh.bibliotecapersonal.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenreService {
    private final GenreRepository genreRepository;

    public Page<Genre> findAll(Pageable pageable) {
        return genreRepository.findByActiveTrue(pageable);
    }

    public Genre findById(Long id) {
        return genreRepository.findGenreByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genero no encontrado"));
    }

    public Page<Genre> searchByName(String name, Pageable pageable) {
        return genreRepository.searchByNameContainingIgnoreCaseAndActiveTrue(name, pageable);
    }

    @Transactional
    public Genre create(Genre created) {
        return genreRepository.save(created);
    }

    @Transactional
    public Genre update(Long id, Genre updated) {
        Genre genre = genreRepository.findGenreByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genero no encontrado"));
        genre.update(updated);
        return genreRepository.save(genre);
    }

    @Transactional
    public void delete(Long id) {
        Genre genre = genreRepository.findGenreByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genero no encontrado"));
        genre.disable();
    }

    @Transactional
    public void enable(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genero no encontrado"));
        genre.enable();
    }
}