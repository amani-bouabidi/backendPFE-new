package com.ira.formation.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👤 utilisateur (apprenant)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Utilisateur user;

    // 🎯 id de l'élément (module / video / document)
    private Long elementId;

    // 📌 type de l'élément favori
    @Enumerated(EnumType.STRING)
    private FavoriteType type;
}