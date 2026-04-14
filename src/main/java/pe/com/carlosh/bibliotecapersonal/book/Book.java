package pe.com.carlosh.bibliotecapersonal.book;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.com.carlosh.bibliotecapersonal.author.Author;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;


    private LocalDate publishedDate;

    private Integer pages;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    public Book(String title, LocalDate publishedDate, Integer pages, Author author) {
        this.title = title;
        this.publishedDate = publishedDate;
        this.pages = pages;
        this.author = author;
        this.active = true;
    }

    public void update(Book updated) {
        if (updated.title != null) this.title = updated.title;
        if (updated.publishedDate != null) this.publishedDate = updated.publishedDate;
        if (updated.pages != null) this.pages = updated.pages;
    }

    public void changeAuthor(Author author){
        this.author = author;
    }

    public void enable(){
        this.active=true;
    }

    public void disable(){
        this.active=false;
    }
}