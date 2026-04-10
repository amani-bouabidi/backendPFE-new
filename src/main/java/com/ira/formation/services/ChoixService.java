package com.ira.formation.services;

import com.ira.formation.dto.ChoixResponseDTO;
import com.ira.formation.entities.Choix;
import com.ira.formation.entities.Question;
import com.ira.formation.exceptions.ResourceNotFoundException;
import com.ira.formation.exceptions.UnauthorizedException;
import com.ira.formation.repositories.ChoixRepository;
import com.ira.formation.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChoixService {

    private final ChoixRepository choixRepository;
    private final QuestionRepository questionRepository;

    public ChoixResponseDTO addChoix(Long questionId, String texte, boolean correct, String email){

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question non trouvée"));

        // ✅ sécurité
        if(!question.getTest().getFormation().getFormateur().getEmail().equals(email)){
            throw new UnauthorizedException("Accès refusé");
        }

        // ✅ duplication
        if(choixRepository.existsByQuestionIdAndTexte(questionId, texte)){
            throw new RuntimeException("Choix déjà existant pour cette question");
        }

        Choix choix = Choix.builder()
                .texte(texte)
                .correct(correct)
                .question(question)
                .build();

        Choix saved = choixRepository.save(choix);

        return ChoixResponseDTO.builder()
                .id(saved.getId())
                .texte(saved.getTexte())
                .correct(saved.isCorrect())
                .questionId(questionId)
                .build();
    }

    public void deleteChoix(Long id, String email){

        Choix choix = choixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Choix non trouvé"));

        if(!choix.getQuestion().getTest().getFormation().getFormateur().getEmail().equals(email)){
            throw new UnauthorizedException("Accès refusé");
        }

        choixRepository.delete(choix);
    }
}