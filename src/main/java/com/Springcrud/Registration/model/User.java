package com.Springcrud.Registration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String gender;
    private String country;
    private LocalDate dateOfBirth;
    private String profilePicturePath;
    private String supportingDocumentPath;

    @ElementCollection(fetch = FetchType.EAGER) // Stores skills as a collection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private Set<String> skills; // Set of skills selected by the user


}
