package com.example.gympulse.data

import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val db: AppDatabase) {

    // --- Workouts ---
    fun getAllWorkouts(): Flow<List<WorkoutEntity>> =
        db.workoutDao().getAll()

    suspend fun insertWorkout(workout: WorkoutEntity): Long =
        db.workoutDao().insert(workout)

    suspend fun updateWorkout(workout: WorkoutEntity) =
        db.workoutDao().update(workout)

    suspend fun deleteWorkout(workout: WorkoutEntity) =
        db.workoutDao().delete(workout)

    // --- Ejercicios ---
    fun getAllExercises(): Flow<List<ExerciseEntity>> =
        db.exerciseDao().getAll()

    // --- Series ---
    fun getSetsForWorkout(workoutId: Long): Flow<List<ExerciseSetEntity>> =
        db.exerciseSetDao().getSetsForWorkout(workoutId)

    suspend fun insertSet(set: ExerciseSetEntity) =
        db.exerciseSetDao().insert(set)

    suspend fun insertSets(sets: List<ExerciseSetEntity>) =
        db.exerciseSetDao().insertAll(sets)

    suspend fun deleteSet(set: ExerciseSetEntity) =
        db.exerciseSetDao().delete(set)

    suspend fun deleteAllSetsFromWorkout(workoutId: Long) =
        db.exerciseSetDao().deleteAllFromWorkout(workoutId)

    fun getAllRoutines(): Flow<List<RoutineEntity>> =
        db.routineDao().getAll()

    fun getExercisesForRoutine(routineId: Long): Flow<List<RoutineExerciseEntity>> =
        db.routineDao().getExercisesForRoutine(routineId)

    suspend fun insertRoutine(routine: RoutineEntity): Long =
        db.routineDao().insertRoutine(routine)

    suspend fun insertRoutineExercises(exercises: List<RoutineExerciseEntity>) =
        db.routineDao().insertExercises(exercises)

    suspend fun deleteRoutine(routine: RoutineEntity) {
        db.routineDao().deleteExercisesFromRoutine(routine.id)
        db.routineDao().deleteRoutine(routine)
    }
}