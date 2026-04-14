package pe.com.carlosh.bibliotecapersonal.genre;

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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreService genreService;

    @Nested
    @DisplayName("Camino feliz")
    class HappyPath {

        @Test
        @DisplayName("findAll - retorna solo géneros activos")
        void findAll_returnsActiveGenres() {
            Pageable pageable = PageRequest.of(0, 10);
            Genre genre = new Genre("Fantasía", "Mundos ficticios");
            Page<Genre> page = new PageImpl<>(List.of(genre));

            when(genreRepository.findByActiveTrue(pageable)).thenReturn(page);

            Page<Genre> result = genreService.findAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getName()).isEqualTo("Fantasía");
            verify(genreRepository).findByActiveTrue(pageable);
        }

        @Test
        @DisplayName("findById - retorna género activo existente")
        void findById_returnsGenre() {
            Genre genre = new Genre("Terror", "Miedo");

            when(genreRepository.findGenreByIdAndActiveTrue(1L)).thenReturn(Optional.of(genre));

            Genre result = genreService.findById(1L);

            assertThat(result.getName()).isEqualTo("Terror");
        }

        @Test
        @DisplayName("searchByName - busca por nombre parcial")
        void searchByName_returnsMatches() {
            Pageable pageable = PageRequest.of(0, 10);
            Genre genre = new Genre("Ciencia Ficción", null);
            Page<Genre> page = new PageImpl<>(List.of(genre));

            when(genreRepository.searchByNameContainingIgnoreCaseAndActiveTrue("cien", pageable)).thenReturn(page);

            Page<Genre> result = genreService.searchByName("cien", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getName()).contains("Cien");
        }

        @Test
        @DisplayName("create - guarda y retorna el género")
        void create_savesAndReturnsGenre() {
            Genre genre = new Genre("Novela", "Prosa extensa");

            when(genreRepository.save(genre)).thenReturn(genre);

            Genre result = genreService.create(genre);

            assertThat(result.getName()).isEqualTo("Novela");
            assertThat(result.getActive()).isTrue();
            verify(genreRepository).save(genre);
        }

        @Test
        @DisplayName("update - actualiza campos del género")
        void update_modifiesGenre() {
            Genre existing = new Genre("Novela", "Prosa");
            Genre updated = new Genre(null, "Prosa extensa");

            when(genreRepository.findGenreByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
            when(genreRepository.save(existing)).thenReturn(existing);

            Genre result = genreService.update(1L, updated);

            assertThat(result.getName()).isEqualTo("Novela");
            assertThat(result.getDescription()).isEqualTo("Prosa extensa");
        }

        @Test
        @DisplayName("delete - desactiva el género (soft delete)")
        void delete_disablesGenre() {
            Genre genre = new Genre("Poesía", null);

            when(genreRepository.findGenreByIdAndActiveTrue(1L)).thenReturn(Optional.of(genre));

            genreService.delete(1L);

            assertThat(genre.getActive()).isFalse();
        }

        @Test
        @DisplayName("enable - reactiva un género desactivado")
        void enable_activatesGenre() {
            Genre genre = new Genre("Poesía", null);
            genre.disable();

            when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));

            genreService.enable(1L);

            assertThat(genre.getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Camino malo")
    class ErrorPath {

        @Test
        @DisplayName("findById - lanza excepción si no existe")
        void findById_throwsWhenNotFound() {
            when(genreRepository.findGenreByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> genreService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("no encontrado");
        }

        @Test
        @DisplayName("update - lanza excepción si el género no existe")
        void update_throwsWhenNotFound() {
            Genre updated = new Genre("X", "Y");

            when(genreRepository.findGenreByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> genreService.update(99L, updated))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(genreRepository, never()).save(any());
        }

        @Test
        @DisplayName("delete - lanza excepción si el género no existe o está inactivo")
        void delete_throwsWhenNotFound() {
            when(genreRepository.findGenreByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> genreService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("enable - lanza excepción si el género no existe")
        void enable_throwsWhenNotFound() {
            when(genreRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> genreService.enable(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("update - no modifica campos nulos del request")
        void update_ignoresNullFields() {
            Genre existing = new Genre("Fantasía", "Mundos ficticios");
            Genre updated = new Genre(null, null);

            when(genreRepository.findGenreByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
            when(genreRepository.save(existing)).thenReturn(existing);

            Genre result = genreService.update(1L, updated);

            assertThat(result.getName()).isEqualTo("Fantasía");
            assertThat(result.getDescription()).isEqualTo("Mundos ficticios");
        }

        @Test
        @DisplayName("findAll - retorna página vacía si no hay géneros activos")
        void findAll_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Genre> emptyPage = new PageImpl<>(List.of());

            when(genreRepository.findByActiveTrue(pageable)).thenReturn(emptyPage);

            Page<Genre> result = genreService.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("delete seguido de findById - género eliminado no se encuentra")
        void deleteThenFind_throwsException() {
            Genre genre = new Genre("Poesía", null);

            when(genreRepository.findGenreByIdAndActiveTrue(1L))
                    .thenReturn(Optional.of(genre))
                    .thenReturn(Optional.empty());

            genreService.delete(1L);

            assertThatThrownBy(() -> genreService.findById(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}