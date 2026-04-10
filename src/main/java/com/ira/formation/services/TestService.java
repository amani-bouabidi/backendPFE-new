package com.ira.formation.services;

import com.ira.formation.dto.ChoixApprenantDTO;
import com.ira.formation.dto.ChoixResponseDTO;
import com.ira.formation.dto.QuestionApprenantDTO;
import com.ira.formation.dto.QuestionResponseDTO;
import com.ira.formation.dto.TestApprenantDTO;
import com.ira.formation.dto.TestResponseDTO;
import com.ira.formation.entities.Choix;
import com.ira.formation.entities.Formation;
import com.ira.formation.entities.Inscription;
import com.ira.formation.entities.Question;
import com.ira.formation.entities.Test;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.exceptions.ResourceNotFoundException;
import com.ira.formation.exceptions.UnauthorizedException;
import com.ira.formation.repositories.FormationRepository;
import com.ira.formation.repositories.InscriptionRepository;
import com.ira.formation.repositories.TestRepository;
import com.ira.formation.repositories.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final FormationRepository formationRepository;
    private final InscriptionRepository inscriptionRepository;
    private final UtilisateurRepository utilisateurRepository;

    // =================== CREATE ===================
    public TestResponseDTO createTest(Long formationId, String titre, String email){
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation non trouvée"));

        if(!formation.getFormateur().getEmail().equals(email)){
            throw new UnauthorizedException("Accès refusé");
        }

        if(testRepository.existsByFormationId(formationId)){
            throw new RuntimeException("Un test existe déjà pour cette formation");
        }

        Test test = Test.builder()
                .titre(titre)
                .formation(formation)
                .build();

        Test saved = testRepository.save(test);

        return mapToDTO(saved); // formateur voit tout
    }

 // =================== GET BY FORMATION (FORMATEUR) ===================
    public TestResponseDTO getTestByFormation(Long formationId, String email){

        Test test = testRepository.findByFormationId(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Test non trouvé"));

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // ❌ sécurité
        if(!"FORMATEUR".equals(user.getRole().getNom())){
            throw new UnauthorizedException("Accès réservé au formateur");
        }

        // ❌ vérifier propriétaire
        if(!test.getFormation().getFormateur().getEmail().equals(email)){
            throw new UnauthorizedException("Accès refusé");
        }

        return mapToDTO(test); // ✅ فقط formateur
    }

 // =================== GET BY FORMATION (APPRENANT) ===================
    public TestApprenantDTO getTestByFormationForApprenant(Long formationId, String email){

        Test test = testRepository.findByFormationId(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Test non trouvé"));

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // ❌ sécurité
        if(!"APPRENANT".equals(user.getRole().getNom())){
            throw new UnauthorizedException("Seul un apprenant peut accéder à ce test");
        }

        return mapToDTOForApprenant(test); // ✅ هنا صحيح
    }
    // =================== UPDATE ===================
    public TestResponseDTO updateTest(Long testId, String titre, String email){
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test non trouvé"));

        if(!test.getFormation().getFormateur().getEmail().equals(email)){
            throw new UnauthorizedException("Accès refusé");
        }

        test.setTitre(titre);
        Test updated = testRepository.save(test);

        return mapToDTO(updated);
    }

    // =================== DELETE ===================
    public void deleteTest(Long testId, String email){
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test non trouvé"));

        if(!test.getFormation().getFormateur().getEmail().equals(email)){
            throw new UnauthorizedException("Accès refusé");
        }

        testRepository.delete(test);
    }

    // =================== PASSER TEST ===================
    public Map<String, Object> passerTest(Long formationId, Map<Long, Long> reponses, String email){
        Utilisateur apprenant = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if(!"APPRENANT".equals(apprenant.getRole().getNom())){
            throw new UnauthorizedException("Seul un apprenant peut passer le test");
        }

        Test test = testRepository.findByFormationId(formationId)
                .orElseThrow(() -> new RuntimeException("Test non trouvé"));

        Formation formation = test.getFormation();

        int total = test.getQuestions().size();
        if(total == 0){
            throw new RuntimeException("Le test n'a pas encore de questions. Contactez le formateur.");
        }

        Inscription inscription = inscriptionRepository
                .findByApprenantAndFormation(apprenant, formation)
                .orElse(Inscription.builder()
                        .apprenant(apprenant)
                        .formation(formation)
                        .tentativesTest(0)
                        .scoreTest(0.0)
                        .valide(false)
                        .build());

        if(inscription.isValide()){
            throw new RuntimeException("Vous avez déjà réussi ce test et êtes inscrit.");
        }

        // Calcul du score
        int correct = 0;
        for (Question q : test.getQuestions()) {
            Long choixId = reponses.get(q.getId());
            if(choixId == null) continue;
            for (Choix c : q.getChoix()){
                if(c.getId().equals(choixId) && c.isCorrect()){
                    correct++;
                }
            }
        }

        double score = ((double) correct / total) * 100;
        boolean success = score >= 50;

        inscription.setTentativesTest(inscription.getTentativesTest() + 1);
        inscription.setScoreTest(score);
        inscription.setValide(success);
        inscription.setDateDernierTest(LocalDateTime.now());
        inscriptionRepository.save(inscription);

        Map<String,Object> result = new HashMap<>();
        result.put("score", score);
        result.put("totalQuestions", total);
        result.put("correctAnswers", correct);
        result.put("success", success);
        result.put("tentatives", inscription.getTentativesTest());
        result.put("message", success ?
                "Félicitations ! Vous avez réussi le test et vous êtes inscrit à la formation." :
                "Vous n'avez pas réussi le test. Vous pouvez retenter plus tard.");

        return result;
    }

    // =================== MAPPERS ===================
    private TestResponseDTO mapToDTO(Test test){
        return TestResponseDTO.builder()
                .id(test.getId())
                .titre(test.getTitre())
                .formationId(test.getFormation().getId())
                .formationTitre(test.getFormation().getTitre())
                .questions(test.getQuestions().stream()
                        .map(q -> QuestionResponseDTO.builder()
                                .id(q.getId())
                                .texte(q.getTexte())
                                .testId(test.getId())
                                .choix(q.getChoix().stream()
                                        .map(c -> ChoixResponseDTO.builder()
                                                .id(c.getId())
                                                .texte(c.getTexte())
                                                .correct(c.isCorrect())
                                                .questionId(q.getId())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .build();
    }

    private TestApprenantDTO mapToDTOForApprenant(Test test){
        return TestApprenantDTO.builder()
                .id(test.getId())
                .titre(test.getTitre())
                .formationId(test.getFormation().getId())
                .formationTitre(test.getFormation().getTitre())
                .questions(test.getQuestions().stream()
                        .map(q -> QuestionApprenantDTO.builder()
                                .id(q.getId())
                                .texte(q.getTexte())
                                .testId(test.getId())
                                .choix(q.getChoix().stream()
                                        .map(c -> ChoixApprenantDTO.builder()
                                                .id(c.getId())
                                                .texte(c.getTexte())
                                                .questionId(q.getId())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .build();
    }
}