package com.ira.formation.controllers;

import com.ira.formation.dto.ApiResponse;
import com.ira.formation.dto.ModuleContentDTO;
import com.ira.formation.dto.ModuleDTO;
import com.ira.formation.services.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    // =================== CREATE ===================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<ModuleDTO> creerModule(@RequestBody ModuleDTO moduleDTO) {
        ModuleDTO created = moduleService.creerModule(moduleDTO);
        return ApiResponse.success(created, "Module créé avec succès");
    }

    // =================== GET MODULES BY FORMATION ===================
    @GetMapping("/formation/{formationId}")
    @PreAuthorize("hasAnyRole('ADMIN','FORMATEUR','APPRENANT')")
    public ApiResponse<List<ModuleDTO>> getModules(@PathVariable Long formationId,
                                                   Principal principal) {
        List<ModuleDTO> modules = moduleService.getModules(formationId, principal.getName());
        return ApiResponse.success(modules, "Modules récupérés avec succès");
    }

    // =================== GET MODULE CONTENT ===================
    @GetMapping("/{id}/content")
    @PreAuthorize("hasAnyRole('ADMIN','FORMATEUR','APPRENANT')")
    public ApiResponse<ModuleContentDTO> getModuleContent(
            @PathVariable Long id,
            @RequestParam String email
    ) {
        ModuleContentDTO content = moduleService.getModuleContent(id, email);
        return ApiResponse.success(content, "Contenu du module récupéré avec succès");
    }

    // =================== UPDATE ===================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<ModuleDTO> modifierModule(@PathVariable Long id,
                                                 @RequestBody ModuleDTO moduleDTO) {
        ModuleDTO updated = moduleService.modifierModule(id, moduleDTO);
        return ApiResponse.success(updated, "Module modifié avec succès");
    }

    // =================== DELETE ===================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> supprimerModule(@PathVariable Long id) {
        moduleService.supprimerModule(id);
        return ApiResponse.success(null, "Module supprimé avec succès");
    }
}