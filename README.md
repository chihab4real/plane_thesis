# Aircraft Deplaining Optimization

This repository contains the source code and documentation for my thesis project: **Optimization of Aircraft Deplaining Strategies using Heuristic and Metaheuristic Algorithms**.

## Project Overview
This project analyzes and implements various aircraft deplaning strategies to find optimal sequences that minimize total deplaining time. It explores a range of methods, from common heuristics like "Front-to-Back" to advanced metaheuristic optimizers like Genetic Algorithms, Simulated Annealing, and Tabu Search. The primary goal is to reduce passenger interference and aisle congestion, leading to faster and more efficient deplaning.

## Features
* **Heuristic Strategies:** Implements and compares standard deplaning methods including `Front-to-Back`, `Back-to-Front`, `Aisle-Middle-Window`, and `Zigzag`.
* **Advanced Metaheuristics:** Provides advanced optimization using `Genetic Algorithm`, `Simulated Annealing`, and `Tabu Search` to discover near-optimal deplaning sequences.
* **Performance Simulation:** Includes a detailed simulator to evaluate and compare the deplaning time for any given passenger sequence.
* **Data Analysis & Export:** Generates detailed metrics and exports results to CSV for further analysis.

## Tech Stack
* **Language:** Java
* **Build Tool:** Gradle
* **Logging:** Logback

## Repository Structure
* `src/main/java/put/plane/deplaning/`: Main source code for the application, simulator, and optimizers.
* `src/main/java/put/plane/deplaning/optimizing/optimizers/basic/`: Implementation of basic heuristic strategies.
* `src/main/java/put/plane/deplaning/optimizing/optimizers/advanced/`: Implementation of metaheuristic algorithms.
* `src/main/resources/`: Application configuration files.

## Installation & Usage

1.  **Clone the repo:**
    ```bash
    git clone [https://github.com/patrickmolina1/plane_thesis.git](https://github.com/patrickmolina1/plane_thesis.git)
    ```
2.  **Navigate to the project directory:**
    ```bash
    cd plane_thesis
    ```
3.  **Build the project using Gradle:**
    ```bash
    ./gradlew build
    ```
4.  **Run the application:**
    ```bash
    ./gradlew run
    ```

## Running Experiments
The main application logic is located in `Application.java`. You can configure which optimizers to run and their parameters within the `AppConfig.java` file. The results of the simulations will be saved in the `results` directory.
