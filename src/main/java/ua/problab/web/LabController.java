/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.problab.experiment.ExperimentCatalog;
import ua.problab.experiment.ExperimentConfig;
import ua.problab.experiment.ExperimentFactory;
import ua.problab.experiment.Theory;
import ua.problab.simulation.SimulationRequest;
import ua.problab.simulation.SimulationResult;
import ua.problab.simulation.SimulationService;

/** Лабораторія: теоретична ймовірність і моделювання. */
@RestController
@RequestMapping("/api")
public class LabController {

    private final SimulationService simulation;

    public LabController(SimulationService simulation) {
        this.simulation = simulation;
    }

    /** Довідник експериментів і подій для панелі налаштувань. */
    @GetMapping("/catalog")
    public ExperimentCatalog.Catalog catalog() {
        return ExperimentCatalog.catalog(SimulationService.MAX_TRIALS);
    }

    /** Точна ймовірність події з покроковим поясненням. */
    @PostMapping("/theory")
    public Theory theory(@RequestBody ExperimentConfig config) {
        return ExperimentFactory.create(config).theory();
    }

    /** Моделювання N випробувань. */
    @PostMapping("/simulate")
    public SimulationResult simulate(@RequestBody SimulationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Порожній запит");
        }
        int trials = request.trials() == null ? 1000 : request.trials();
        return simulation.simulate(request.config(), trials, request.seed());
    }
}
