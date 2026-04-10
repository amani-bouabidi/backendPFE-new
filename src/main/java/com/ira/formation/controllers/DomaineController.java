package com.ira.formation.controllers;

import com.ira.formation.dto.DomaineDTO;
import com.ira.formation.dto.DomaineFormationsDTO;
import com.ira.formation.dto.DomaineStatsDTO;
import com.ira.formation.services.DomaineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/domaines")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DomaineController {

    private final DomaineService domaineService;

    // ✅ CREATE
    @PostMapping
    public DomaineDTO createDomaine(@RequestBody DomaineDTO dto){
        return domaineService.createDomaine(dto);
    }

    // ✅ GET ALL (clean)
    @GetMapping
    public List<DomaineDTO> getAllDomaines(){
        return domaineService.getAllDomaines();
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public void deleteDomaine(@PathVariable Long id){
        domaineService.deleteDomaine(id);
    }

    // ✅ DOMAINES + FORMATIONS
    @GetMapping("/with-formations")
    public List<DomaineFormationsDTO> getDomainesWithFormations(){
        return domaineService.getDomainesWithFormations();
    }

    // ✅ STATS
    @GetMapping("/stats")
    public List<DomaineStatsDTO> getDomainesStats(){
        return domaineService.getDomainesStats();
    }
}