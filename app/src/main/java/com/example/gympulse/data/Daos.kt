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
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getCount(): Int
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

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): RoutineEntity?

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY `order`")
    fun getExercisesForRoutine(routineId: Long): Flow<List<RoutineExerciseEntity>>

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY `order`")
    suspend fun getExercisesForRoutineOnce(routineId: Long): List<RoutineExerciseEntity>

    @Insert
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Insert
    suspend fun insertExercise(exercise: RoutineExerciseEntity): Long

    @Insert
    suspend fun insertExercises(exercises: List<RoutineExerciseEntity>)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteExercisesFromRoutine(routineId: Long)
}

@Dao
interface RoutineSetDao {

    @Insert
    suspend fun insert(set: RoutineSetEntity): Long

    @Insert
    suspend fun insertAll(sets: List<RoutineSetEntity>)

    @Query("SELECT * FROM routine_sets WHERE routineExerciseId = :exerciseId ORDER BY setNumber")
    suspend fun getSetsForExercise(exerciseId: Long): List<RoutineSetEntity>

    @Query("SELECT * FROM routine_sets WHERE routineExerciseId IN (:exerciseIds) ORDER BY setNumber")
    suspend fun getSetsForExercises(exerciseIds: List<Long>): List<RoutineSetEntity>

    @Query("DELETE FROM routine_sets WHERE routineExerciseId = :exerciseId")
    suspend fun deleteSetsForExercise(exerciseId: Long)

    @Delete
    suspend fun deleteSet(set: RoutineSetEntity)

    @Query("DELETE FROM routine_sets WHERE routineExerciseId IN (SELECT id FROM routine_exercises WHERE routineId = :routineId)")
    suspend fun deleteSetsForRoutine(routineId: Long)
}