package pe.com.carlosh.bibliotecapersonal.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByActiveTrue(Pageable pageable);

    Optional<Book> findBookByIdAndActiveTrue(Long id);

    @Query("SELECT b FROM Book b WHERE b.active = true AND " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Book> searchByTitle(@Param("title") String title, Pageable pageable);

    Page<Book> findByAuthorIdAndActiveTrue(Long authorId, Pageable pageable);
}