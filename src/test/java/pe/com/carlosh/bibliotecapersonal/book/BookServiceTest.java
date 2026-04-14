package pe.com.carlosh.bibliotecapersonal.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pe.com.carlosh.bibliotecapersonal.author.Author;
import pe.com.carlosh.bibliotecapersonal.author.AuthorRepository;
import pe.com.carlosh.bibliotecapersonal.exception.ResourceNotFoundException;
import pe.com.carlosh.bibliotecapersonal.genre.Genre;
import pe.com.carlosh.bibliotecapersonal.genre.GenreRepository;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private BookService bookService;

    private static Author authorWithId(Long id, String firstName, String lastName) {
        Author author = new Author(firstName, lastName, null);
        setId(author, id);
        return author;
    }

    private static Genre genreWithId(Long id, String name) {
        Genre genre = new Genre(name, null);
        setId(genre, id);
        return genre;
    }

    private static void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Book bookWith(Author author) {
        return new Book("Cien años de soledad", LocalDate.of(1967, 5, 30), 471, null, author);
    }

    @Nested
    @DisplayName("Camino feliz")
    class HappyPath {

        @Test
        @DisplayName("findAll - retorna solo libros activos")
        void findAll_returnsActiveBooks() {
            Pageable pageable = PageRequest.of(0, 10);
            Book book = bookWith(authorWithId(1L, "Gabriel", "García Márquez"));
            Page<Book> page = new PageImpl<>(List.of(book));

            when(bookRepository.findByActiveTrue(pageable)).thenReturn(page);

            Page<Book> result = bookService.findAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Cien años de soledad");
            verify(bookRepository).findByActiveTrue(pageable);
        }

        @Test
        @DisplayName("findById - retorna libro activo existente")
        void findById_returnsBook() {
            Book book = bookWith(authorWithId(1L, "Gabriel", "García Márquez"));

            when(bookRepository.findBookByIdAndActiveTrue(1L)).thenReturn(Optional.of(book));

            Book result = bookService.findById(1L);

            assertThat(result.getTitle()).isEqualTo("Cien años de soledad");
            assertThat(result.getAuthor().getFirstName()).isEqualTo("Gabriel");
        }

        @Test
        @DisplayName("searchByTitle - busca por título parcial")
        void searchByTitle_returnsMatches() {
            Pageable pageable = PageRequest.of(0, 10);
            Book book = bookWith(authorWithId(1L, "Gabriel", "García Márquez"));
            Page<Book> page = new PageImpl<>(List.of(book));

            when(bookRepository.searchByActiveTrueAndTitleContainingIgnoreCase("soledad", pageable)).thenReturn(page);

            Page<Book> result = bookService.searchByTitle("soledad", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getTitle()).contains("soledad");
        }

        @Test
        @DisplayName("findByAuthor - retorna libros del autor")
        void findByAuthor_returnsBooks() {
            Pageable pageable = PageRequest.of(0, 10);
            Book book = bookWith(authorWithId(1L, "Gabriel", "García Márquez"));
            Page<Book> page = new PageImpl<>(List.of(book));

            when(bookRepository.findByAuthorIdAndActiveTrue(1L, pageable)).thenReturn(page);

            Page<Book> result = bookService.findByAuthor(1L, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(bookRepository).findByAuthorIdAndActiveTrue(1L, pageable);
        }

        @Test
        @DisplayName("create - resuelve autor y guarda el libro activo")
        void create_savesAndReturnsBook() {
            Author author = authorWithId(1L, "Jorge Luis", "Borges");
            Book received = bookWith(author);

            when(authorRepository.findAuthorByIdAndActiveTrue(1L)).thenReturn(Optional.of(author));
            when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

            Book result = bookService.create(received);

            assertThat(result.getTitle()).isEqualTo("Cien años de soledad");
            assertThat(result.getAuthor()).isSameAs(author);
            assertThat(result.isActive()).isTrue();
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("update - actualiza campos del libro sin tocar autor si no viene")
        void update_modifiesBook() {
            Author author = authorWithId(1L, "Gabriel", "García Márquez");
            Book existing = bookWith(author);
            Book updated = new Book("El amor en los tiempos del cólera", null, 400, null, null);

            when(bookRepository.findBookByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
            when(bookRepository.save(existing)).thenReturn(existing);

            Book result = bookService.update(1L, updated);

            assertThat(result.getTitle()).isEqualTo("El amor en los tiempos del cólera");
            assertThat(result.getPages()).isEqualTo(400);
            assertThat(result.getAuthor()).isSameAs(author);
            verify(authorRepository, never()).findAuthorByIdAndActiveTrue(any());
        }

        @Test
        @DisplayName("update - cambia de autor cuando viene author.id en el payload")
        void update_changesAuthor() {
            Author original = authorWithId(1L, "Gabriel", "García Márquez");
            Author nuevo = authorWithId(2L, "Mario", "Vargas Llosa");
            Book existing = bookWith(original);
            Book updated = new Book(null, null, null, null, nuevo);

            when(bookRepository.findBookByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
            when(authorRepository.findAuthorByIdAndActiveTrue(2L)).thenReturn(Optional.of(nuevo));
            when(bookRepository.save(existing)).thenReturn(existing);

            Book result = bookService.update(1L, updated);

            assertThat(result.getAuthor()).isSameAs(nuevo);
        }

        @Test
        @DisplayName("create - resuelve y asigna géneros cuando vienen en el payload")
        void create_assignsGenres() {
            Author author = authorWithId(1L, "J.R.R.", "Tolkien");
            Genre fantasia = genreWithId(10L, "Fantasía");
            Book received = bookWith(author);
            received.replaceGenres(Set.of(fantasia));

            when(authorRepository.findAuthorByIdAndActiveTrue(1L)).thenReturn(Optional.of(author));
            when(genreRepository.findGenreByIdAndActiveTrue(10L)).thenReturn(Optional.of(fantasia));
            when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

            Book result = bookService.create(received);

            assertThat(result.getGenres()).containsExactly(fantasia);
        }

        @Test
        @DisplayName("addGenre - agrega un género al libro")
        void addGenre_attachesGenre() {
            Book book = bookWith(authorWithId(1L, "J.R.R.", "Tolkien"));
            Genre aventura = genreWithId(20L, "Aventura");

            when(bookRepository.findBookByIdAndActiveTrue(1L)).thenReturn(Optional.of(book));
            when(genreRepository.findGenreByIdAndActiveTrue(20L)).thenReturn(Optional.of(aventura));
            when(bookRepository.save(book)).thenReturn(book);

            Book result = bookService.addGenre(1L, 20L);

            assertThat(result.getGenres()).containsExactly(aventura);
        }

        @Test
        @DisplayName("removeGenre - quita un género del libro")
        void removeGenre_detachesGenre() {
            Book book = bookWith(authorWithId(1L, "J.R.R.", "Tolkien"));
            Genre aventura = genreWithId(20L, "Aventura");
            book.addGenre(aventura);

            when(bookRepository.findBookByIdAndActiveTrue(1L)).thenReturn(Optional.of(book));
            when(genreRepository.findGenreByIdAndActiveTrue(20L)).thenReturn(Optional.of(aventura));
            when(bookRepository.save(book)).thenReturn(book);

            Book result = bookService.removeGenre(1L, 20L);

            assertThat(result.getGenres()).isEmpty();
        }

        @Test
        @DisplayName("delete - desactiva el libro (soft delete)")
        void delete_disablesBook() {
            Book book = bookWith(authorWithId(1L, "César", "Vallejo"));

            when(bookRepository.findBookByIdAndActiveTrue(1L)).thenReturn(Optional.of(book));

            bookService.delete(1L);

            assertThat(book.isActive()).isFalse();
        }

        @Test
        @DisplayName("enable - reactiva un libro desactivado")
        void enable_activatesBook() {
            Book book = bookWith(authorWithId(1L, "César", "Vallejo"));
            book.disable();

            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

            bookService.enable(1L);

            assertThat(book.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Camino malo")
    class ErrorPath {

        @Test
        @DisplayName("findById - lanza excepción si no existe")
        void findById_throwsWhenNotFound() {
            when(bookRepository.findBookByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("no encontrado");
        }

        @Test
        @DisplayName("create - lanza excepción si author es null")
        void create_throwsWhenAuthorNull() {
            Book received = new Book("Sin autor", null, 100, null, null);

            assertThatThrownBy(() -> bookService.create(received))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Autor requerido");

            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("create - lanza excepción si el autor no existe o está inactivo")
        void create_throwsWhenAuthorMissing() {
            Author author = authorWithId(99L, "Fantasma", "Desconocido");
            Book received = bookWith(author);

            when(authorRepository.findAuthorByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.create(received))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("update - lanza excepción si el libro no existe")
        void update_throwsWhenNotFound() {
            Book updated = new Book("X", null, 1, null, authorWithId(1L, "x", "y"));

            when(bookRepository.findBookByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.update(99L, updated))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("update - lanza excepción si el autor nuevo no existe")
        void update_throwsWhenNewAuthorMissing() {
            Author original = authorWithId(1L, "Gabriel", "García Márquez");
            Book existing = bookWith(original);
            Book updated = new Book(null, null, null, null, authorWithId(99L, "x", "y"));

            when(bookRepository.findBookByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
            when(authorRepository.findAuthorByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.update(1L, updated))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("create - lanza excepción si un género no existe o está inactivo")
        void create_throwsWhenGenreMissing() {
            Author author = authorWithId(1L, "J.R.R.", "Tolkien");
            Genre fantasma = genreWithId(99L, "Inexistente");
            Book received = bookWith(author);
            received.replaceGenres(Set.of(fantasma));

            when(authorRepository.findAuthorByIdAndActiveTrue(1L)).thenReturn(Optional.of(author));
            when(genreRepository.findGenreByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.create(received))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("addGenre - lanza excepción si el género no existe")
        void addGenre_throwsWhenGenreMissing() {
            Book book = bookWith(authorWithId(1L, "J.R.R.", "Tolkien"));

            when(bookRepository.findBookByIdAndActiveTrue(1L)).thenReturn(Optional.of(book));
            when(genreRepository.findGenreByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.addGenre(1L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("addGenre - lanza excepción si el libro no existe")
        void addGenre_throwsWhenBookMissing() {
            when(bookRepository.findBookByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.addGenre(99L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("delete - lanza excepción si el libro no existe o está inactivo")
        void delete_throwsWhenNotFound() {
            when(bookRepository.findBookByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("enable - lanza excepción si el libro no existe")
        void enable_throwsWhenNotFound() {
            when(bookRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.enable(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("update - no modifica campos nulos del request")
        void update_ignoresNullFields() {
            Author author = authorWithId(1L, "Gabriel", "García Márquez");
            Book existing = bookWith(author);
            Book updated = new Book(null, null, null, null, null);

            when(bookRepository.findBookByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
            when(bookRepository.save(existing)).thenReturn(existing);

            Book result = bookService.update(1L, updated);

            assertThat(result.getTitle()).isEqualTo("Cien años de soledad");
            assertThat(result.getPublishedDate()).isEqualTo(LocalDate.of(1967, 5, 30));
            assertThat(result.getPages()).isEqualTo(471);
            assertThat(result.getAuthor()).isSameAs(author);
        }

        @Test
        @DisplayName("findAll - retorna página vacía si no hay libros activos")
        void findAll_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> emptyPage = new PageImpl<>(List.of());

            when(bookRepository.findByActiveTrue(pageable)).thenReturn(emptyPage);

            Page<Book> result = bookService.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("update - no toca géneros existentes")
        void update_keepsGenres() {
            Author author = authorWithId(1L, "J.R.R.", "Tolkien");
            Genre fantasia = genreWithId(10L, "Fantasía");
            Book existing = bookWith(author);
            existing.addGenre(fantasia);
            Book updated = new Book("Nuevo título", null, null, null, null);

            when(bookRepository.findBookByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
            when(bookRepository.save(existing)).thenReturn(existing);

            Book result = bookService.update(1L, updated);

            assertThat(result.getGenres()).containsExactly(fantasia);
            verify(genreRepository, never()).findGenreByIdAndActiveTrue(any());
        }

        @Test
        @DisplayName("create - ignora géneros sin id en el payload")
        void create_ignoresGenresWithoutId() {
            Author author = authorWithId(1L, "J.R.R.", "Tolkien");
            Genre sinId = new Genre("Huérfano", null);
            Book received = bookWith(author);
            received.replaceGenres(new HashSet<>(Set.of(sinId)));

            when(authorRepository.findAuthorByIdAndActiveTrue(1L)).thenReturn(Optional.of(author));
            when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

            Book result = bookService.create(received);

            assertThat(result.getGenres()).isEmpty();
            verify(genreRepository, never()).findGenreByIdAndActiveTrue(any());
        }

        @Test
        @DisplayName("delete seguido de findById - libro eliminado no se encuentra")
        void deleteThenFind_throwsException() {
            Book book = bookWith(authorWithId(1L, "César", "Vallejo"));

            when(bookRepository.findBookByIdAndActiveTrue(1L))
                    .thenReturn(Optional.of(book))
                    .thenReturn(Optional.empty());

            bookService.delete(1L);

            assertThatThrownBy(() -> bookService.findById(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}