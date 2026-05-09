package com.example.gympulse.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAll(): Flow<List<WorkoutEntity>>

    @Insert
    suspend fun insert(workout: WorkoutEntity): Long

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Delete
    suspend fun delete(workout: WorkoutEntity)
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY category, name")
    fun getAll(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)
}

@Dao
interface ExerciseSetDao {
    @Query("SELECT * FROM exercise_sets WHERE workoutId = :workoutId")
    fun getSetsForWorkout(workoutId: Long): Flow<List<ExerciseSetEntity>>

    @Insert
    suspend fun insert(set: ExerciseSetEntity)

    @Insert
    suspend fun insertAll(sets: List<ExerciseSetEntity>)

    @Delete
    suspend fun delete(set: ExerciseSetEntity)

    @Query("DELETE FROM exercise_sets WHERE workoutId = :workoutId")
    suspend fun deleteAllFromWorkout(workoutId: Long)
}

@Dao
interface RoutineDao {

    @Query("SELECT * FROM routines ORDER BY name")
    fun getAll(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY `order`")
    fun getExercisesForRoutine(routineId: Long): Flow<List<RoutineExerciseEntity>>

    @Insert
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert
    suspend fun insertExercises(exercises: List<RoutineExerciseEntity>)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteExercisesFromRoutine(routineId: Long)
}