package pe.com.carlosh.bibliotecapersonal.author;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "authors")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    private LocalDate birthday;

    @Column(nullable = false)
    private Boolean active;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Author(String firstName, String lastName, LocalDate birthday) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.active = true;
    }

    public void update(Author updated) {
        if(updated.firstName!=null)this.firstName = updated.firstName;
        if(updated.lastName!=null)this.lastName = updated.lastName;
        if(updated.birthday!=null)this.birthday = updated.birthday;
    }

    public void enable(){
        this.active=true;
    }

    public void disable(){
        this.active=false;
    }

    public Integer getAge(){
        if(birthday == null) return null;
        return Period.between(birthday,LocalDate.now()).getYears();
    }
}
