
---

# Aircraft Deplaning Optimization: A Microscopic Simulation Approach

This repository contains the source code, simulation engine, and experimental data for the Bachelor's thesis **"AIRCRAFTS DEPLANING OPTIMIZATION"** (Poznań University of Technology, 2026)[cite: 7, 8, 12].

## Overview
Aircraft deplaning is a critical bottleneck in airport **Turnaround Time (TAT)**. This project utilizes a **Discrete-Event Simulation (DES)** built on **Cellular Automata** to model passenger movement, "Luggage Factors," and aisle interference.

### Key Finding: The Efficiency Paradox
Our research tested 720 scenarios across various algorithms. Surprisingly, complex metaheuristics were outperformed by simple geometric rules:
* **Zigzag Strategy:** Reduced deplaning time by **71.3%** compared to the industry-standard "Front-to-Back" method.
* **Computational Efficiency:** The Zigzag rule is ~6,800x faster to calculate than Tabu Search while providing superior results.

---

## Technology Stack
The project is built with a modular Java architecture to ensure scientific reproducibility[cite: 261, 272].
* **Core:** Java 17+ (JDK) 
* **Build System:** Gradle 
* **Framework:** Spring (Inversion of Control for scenario injection) 
* **Visualization:** JavaFX (Standalone MVC-based playback tool) 
* **Utilities:** Project Lombok, Google Guava, Jackson/JAXB 

---

## Implemented Algorithms
The framework compares two tiers of deplaning strategies:

### 1. Deterministic Heuristics (Rule-Based)
* **Front-to-Back (Baseline):** The standard, sequential row-by-row release.
* **Aisle-First (Reverse WilMA):** "Peels" the cabin from the aisle out to the windows.
* **Zigzag (Reverse Steffen):** Interleaved row release to maximize parallel overhead bin access.

### 2. Stochastic Metaheuristics (Search-Based)
* **Genetic Algorithm (GA):** Evolves release sequences through crossover and mutation.
* **Simulated Annealing (SA):** Uses probabilistic acceptance to escape local optima.
* **Tabu Search (TS):** Uses memory-guided lists to prevent cycling during local searches.

---

## Experimental Results
The simulation measured **Total Deplaning Time (TDT)** and **Average Wait Time**.

| Method | Avg Time (ticks) | Improvement vs. Baseline |
| :--- | :--- | :--- |
| **Zigzag** | **516.7** | **+71.3%** |
| Tabu Search | 585.8 | +67.5% |
| Simulated Annealing | 1038.3 | +42.4% |
| Front-to-Back | 1801.5 | 0.0% |



### Insights on Luggage & Layout
* **The "Window-Seat Penalty":** Passengers in middle-window seats (Rows 7-11) face the highest interference.
* **Dual-Exit Impact:** Adding a rear exit "pops" the high-pressure bottleneck at the front and shifts it to a "stagnation point" at Row 16.
* **Robustness:** The Zigzag strategy maintains a significant lead even at 100% luggage saturation.

---

## Getting Started
1.  **Clone the repository:** `git clone [repo-url]`
2.  **Build the project:** `./gradlew build`
3.  **Run Simulation:** Use the `SimulatorService` to execute a deplaning scenario.
4.  **Visualize:** Open the JavaFX tool to replay `.xml` frame data exported by the engine.


---
