package pe.com.carlosh.bibliotecapersonal.author;

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
import pe.com.carlosh.bibliotecapersonal.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;


    @Nested
    @DisplayName("Camino feliz")
    class HappyPath {

        @Test
        @DisplayName("findAll - retorna solo autores activos")
        void findAll_returnsActiveAuthors() {
            Pageable pageable = PageRequest.of(0, 10);
            Author author = new Author("Gabriel", "García Márquez", LocalDate.of(1927, 3, 6));
            Page<Author> page = new PageImpl<>(List.of(author));

            when(authorRepository.findByActiveTrue(pageable)).thenReturn(page);

            Page<Author> result = authorService.findAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getFirstName()).isEqualTo("Gabriel");
            verify(authorRepository).findByActiveTrue(pageable);
        }

        @Test
        @DisplayName("findById - retorna autor activo existente")
        void findById_returnsAuthor() {
            Author author = new Author("Mario", "Vargas Llosa", LocalDate.of(1936, 3, 28));

            when(authorRepository.findAuthorByIdAndActiveTrue(1L)).thenReturn(Optional.of(author));

            Author result = authorService.findById(1L);

            assertThat(result.getFirstName()).isEqualTo("Mario");
            assertThat(result.getLastName()).isEqualTo("Vargas Llosa");
        }

        @Test
        @DisplayName("searchByName - busca por nombre parcial")
        void searchByName_returnsMatches() {
            Pageable pageable = PageRequest.of(0, 10);
            Author author = new Author("Gabriel", "García Márquez", null);
            Page<Author> page = new PageImpl<>(List.of(author));

            when(authorRepository.searchByName("gabriel", pageable)).thenReturn(page);

            Page<Author> result = authorService.searchByName("gabriel", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getFirstName()).isEqualTo("Gabriel");
        }

        @Test
        @DisplayName("create - guarda y retorna el autor")
        void create_savesAndReturnsAuthor() {
            Author author = new Author("Jorge Luis", "Borges", LocalDate.of(1899, 8, 24));

            when(authorRepository.save(author)).thenReturn(author);

            Author result = authorService.create(author);

            assertThat(result.getFirstName()).isEqualTo("Jorge Luis");
            assertThat(result.getActive()).isTrue();
            verify(authorRepository).save(author);
        }

        @Test
        @DisplayName("update - actualiza campos del autor")
        void update_modifiesAuthor() {
            Author existing = new Author("Mario", "Vargas", LocalDate.of(1936, 3, 28));
            Author updated = new Author("Mario", "Vargas Llosa", null);

            when(authorRepository.findAuthorByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
            when(authorRepository.save(existing)).thenReturn(existing);

            Author result = authorService.update(1L, updated);

            assertThat(result.getLastName()).isEqualTo("Vargas Llosa");
            verify(authorRepository).save(existing);
        }

        @Test
        @DisplayName("delete - desactiva el autor (soft delete)")
        void delete_disablesAuthor() {
            Author author = new Author("César", "Vallejo", LocalDate.of(1892, 3, 16));

            when(authorRepository.findAuthorByIdAndActiveTrue(1L)).thenReturn(Optional.of(author));

            authorService.delete(1L);

            assertThat(author.getActive()).isFalse();
        }

        @Test
        @DisplayName("enable - reactiva un autor desactivado")
        void enable_activatesAuthor() {
            Author author = new Author("César", "Vallejo", LocalDate.of(1892, 3, 16));
            author.disable();

            when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

            authorService.enable(1L);

            assertThat(author.getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Camino malo")
    class ErrorPath {

        @Test
        @DisplayName("findById - lanza excepción si no existe")
        void findById_throwsWhenNotFound() {
            when(authorRepository.findAuthorByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authorService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("no encontrado");
        }

        @Test
        @DisplayName("update - lanza excepción si el autor no existe")
        void update_throwsWhenNotFound() {
            Author updated = new Author("Test", "Test", null);

            when(authorRepository.findAuthorByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authorService.update(99L, updated))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(authorRepository, never()).save(any());
        }

        @Test
        @DisplayName("delete - lanza excepción si el autor no existe o está inactivo")
        void delete_throwsWhenNotFound() {
            when(authorRepository.findAuthorByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authorService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("enable - lanza excepción si el autor no existe")
        void enable_throwsWhenNotFound() {
            when(authorRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authorService.enable(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("update - no modifica campos nulos del request")
        void update_ignoresNullFields() {
            Author existing = new Author("Mario", "Vargas Llosa", LocalDate.of(1936, 3, 28));
            Author updated = new Author(null, null, null);

            when(authorRepository.findAuthorByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
            when(authorRepository.save(existing)).thenReturn(existing);

            Author result = authorService.update(1L, updated);

            assertThat(result.getFirstName()).isEqualTo("Mario");
            assertThat(result.getLastName()).isEqualTo("Vargas Llosa");
            assertThat(result.getBirthday()).isEqualTo(LocalDate.of(1936, 3, 28));
        }

        @Test
        @DisplayName("getAge - retorna null si birthday es null")
        void getAge_returnsNullWhenNoBirthday() {
            Author author = new Author("Anónimo", null, null);

            assertThat(author.getAge()).isNull();
        }

        @Test
        @DisplayName("getAge - calcula edad correctamente")
        void getAge_calculatesCorrectly() {
            LocalDate birthday = LocalDate.now().minusYears(30);
            Author author = new Author("Test", "Author", birthday);

            assertThat(author.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("findAll - retorna página vacía si no hay autores activos")
        void findAll_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Author> emptyPage = new PageImpl<>(List.of());

            when(authorRepository.findByActiveTrue(pageable)).thenReturn(emptyPage);

            Page<Author> result = authorService.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("delete seguido de findById - autor eliminado no se encuentra")
        void deleteThenFind_throwsException() {
            Author author = new Author("César", "Vallejo", LocalDate.of(1892, 3, 16));

            when(authorRepository.findAuthorByIdAndActiveTrue(1L))
                    .thenReturn(Optional.of(author)) //1- cuando lo vamos a eliminar (aún existe)
                    .thenReturn(Optional.empty()); // 2- cuando lo buscamos y ya ha sido eliminado

            authorService.delete(1L);

            assertThatThrownBy(() -> authorService.findById(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}