package com.ira.formation.services;

import com.ira.formation.dto.QuestionResponseDTO;
import com.ira.formation.entities.Question;
import com.ira.formation.entities.Test;
import com.ira.formation.exceptions.ResourceNotFoundException;
import com.ira.formation.exceptions.UnauthorizedException;
import com.ira.formation.repositories.QuestionRepository;
import com.ira.formation.repositories.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TestRepository testRepository;

    // ✅ ADD QUESTION
    public QuestionResponseDTO addQuestion(Long testId, String texte, String email){

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test non trouvé"));

        // ✅ check formateur
        if(!test.getFormation().getFormateur().getEmail().equals(email)){
            throw new UnauthorizedException("Accès refusé");
        }

        // ✅ check duplication
        if(questionRepository.existsByTestIdAndTexte(testId, texte)){
            throw new RuntimeException("Question déjà existante pour ce test");
        }

        Question question = Question.builder()
                .texte(texte)
                .test(test)
                .build();

        Question saved = questionRepository.save(question);

        return QuestionResponseDTO.builder()
                .id(saved.getId())
                .texte(saved.getTexte())
                .testId(testId)
                .build();
    }
    // ✅ DELETE QUESTION
    public void deleteQuestion(Long questionId, String email){

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question non trouvée"));

        if(!question.getTest().getFormation().getFormateur().getEmail().equals(email)){
            throw new UnauthorizedException("Accès refusé");
        }

        questionRepository.delete(question);
    }
}