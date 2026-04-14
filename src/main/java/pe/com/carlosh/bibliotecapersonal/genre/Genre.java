package pe.com.carlosh.bibliotecapersonal.genre;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "genres")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Boolean active;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Genre(String name, String description) {
        this.name = name;
        this.description = description;
        this.active = true;
    }

    public void update(Genre updated) {
        if (updated.name != null) this.name = updated.name;
        if (updated.description != null) this.description = updated.description;
    }

    public void enable() {
        this.active = true;
    }

    public void disable() {
        this.active = false;
    }
}