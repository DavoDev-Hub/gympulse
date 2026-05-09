package com.example.gympulse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WorkoutEntity::class,
        ExerciseEntity::class,
        ExerciseSetEntity::class,
        RoutineEntity::class,           // ← nuevo
        RoutineExerciseEntity::class    // ← nuevo
    ],
    version = 2,                        // ← incrementar versión
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseSetDao(): ExerciseSetDao
    abstract fun routineDao(): RoutineDao  // ← nuevo

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "gympulse.db")
                    .fallbackToDestructiveMigration() // ← agrega esto por el cambio de versión
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.exerciseDao()?.insertAll(defaultExercises)
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val defaultExercises = listOf(
            ExerciseEntity(name = "Lagartijas",          category = "Pecho"),
            ExerciseEntity(name = "Press banca",         category = "Pecho"),
            ExerciseEntity(name = "Aperturas",           category = "Pecho"),
            ExerciseEntity(name = "Sentadilla",          category = "Piernas"),
            ExerciseEntity(name = "Sentadilla frontal",  category = "Piernas"),
            ExerciseEntity(name = "Peso muerto",         category = "Piernas"),
            ExerciseEntity(name = "Abdominales",         category = "Abdominales"),
            ExerciseEntity(name = "Plancha",             category = "Abdominales"),
            ExerciseEntity(name = "Crunch",              category = "Abdominales"),
            ExerciseEntity(name = "Dominadas",           category = "Espalda"),
            ExerciseEntity(name = "Remo con barra",      category = "Espalda"),
            ExerciseEntity(name = "Jalón al pecho",      category = "Espalda"),
            ExerciseEntity(name = "Curl con barra",      category = "Bíceps"),
            ExerciseEntity(name = "Curl mancuernas",     category = "Bíceps"),
            ExerciseEntity(name = "Fondos",              category = "Tríceps"),
            ExerciseEntity(name = "Press francés",       category = "Tríceps"),
            ExerciseEntity(name = "Press militar",       category = "Hombros"),
            ExerciseEntity(name = "Elevaciones lat.",    category = "Hombros"),
            ExerciseEntity(name = "Correr",              category = "Cardio"),
            ExerciseEntity(name = "Bicicleta",           category = "Cardio"),
            ExerciseEntity(name = "Saltar la cuerda",    category = "Cardio"),
        )
    }
}