package com.ira.formation.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Choix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String texte;

    private boolean correct;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
}