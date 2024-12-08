package com.cs407.powerplates
import kotlin.random.Random

class RepCount {

    private var mass: Int
    private var strength: Int
    private var stamina: Int
    private var experienceLevel: String
    private var workoutType: String

    constructor(mass: Int, strength: Int, stamina: Int, bodyWeight: Int, experienceLevel: String, workoutType: String){
        this.mass = mass
        this.strength = strength
        this.stamina = stamina
        this.experienceLevel = experienceLevel
        this.workoutType = workoutType
    }

    fun calculateReps(): String {
        // Safety check if input is valid
        if (mass !in 1..5 || strength !in 1..5 || stamina !in 1..5) {
            return "Rankings must be between 1 and 5."
        }

        if (experienceLevel !in listOf("beg", "inter", "adv")) {
            return "Experience level must be 'beginner', 'intermediate', or 'advanced'."
        }

        if (workoutType !in listOf("push", "pull", "legs")) {
            return "Workout type must be 'push', 'pull', or 'legs'."
        }

        // Define base reps
        val levels = mapOf(
            "beg" to mapOf("push" to 10, "pull" to 12, "legs" to 13),
            "inter" to mapOf("push" to 10, "pull" to 8, "legs" to 11),
            "adv" to mapOf("push" to 6, "pull" to 6, "legs" to 9)
        )

        // Overall score
        val combinedScore = mass + strength + stamina

        // Base number of reps
        var baseReps = levels[experienceLevel]?.get(workoutType) ?: return "Invalid workout type."

        // Adjust number of reps
        baseReps = when {
            combinedScore >= 13 -> baseReps + 1
            combinedScore < 10 -> baseReps - 1
            else -> baseReps
        }

        // Return the number of reps as a string
        return baseReps.toString()
    }
}