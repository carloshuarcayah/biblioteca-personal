package pe.com.carlosh.bibliotecapersonal.genre;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    Page<Genre> findByActiveTrue(Pageable pageable);

    Optional<Genre> findGenreByIdAndActiveTrue(Long id);

    Page<Genre> searchByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
}
