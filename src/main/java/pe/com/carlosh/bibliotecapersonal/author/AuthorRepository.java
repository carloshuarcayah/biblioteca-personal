package pe.com.carlosh.bibliotecapersonal.author;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author,Long> {
    Page<Author> findByActiveTrue(Pageable pageable);
    Optional<Author> findAuthorByIdAndActiveTrue(Long id);

    @Query("SELECT a FROM Author a WHERE a.active = true AND " +
            "(LOWER(a.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Author> searchByName(@Param("name") String name, Pageable pageable);
}
