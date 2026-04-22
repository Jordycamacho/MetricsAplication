package com.fitapp.appfit.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fitapp.appfit.feature.routine.database.dao.PendingSyncDao
import com.fitapp.appfit.feature.routine.database.dao.RoutineDao
import com.fitapp.appfit.feature.routine.database.dao.RoutineExerciseDao
import com.fitapp.appfit.feature.routine.database.dao.SetParameterDao
import com.fitapp.appfit.feature.routine.database.dao.SetTemplateDao
import com.fitapp.appfit.feature.workout.database.dao.WorkoutSessionDao
import com.fitapp.appfit.feature.workout.database.dao.WorkoutSetResultDao
import com.fitapp.appfit.core.database.dao.PendingSyncOperation
import com.fitapp.appfit.feature.routine.database.entity.RoutineEntity
import com.fitapp.appfit.feature.routine.database.entity.RoutineExerciseEntity
import com.fitapp.appfit.feature.routine.database.entity.SetParameterEntity
import com.fitapp.appfit.feature.routine.database.entity.SetTemplateEntity
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSessionEntity
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSetResultEntity

@Database(
    entities = [
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        SetTemplateEntity::class,
        SetParameterEntity::class,
        PendingSyncOperation::class,
        WorkoutSessionEntity::class,
        WorkoutSetResultEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun routineDao(): RoutineDao
    abstract fun routineExerciseDao(): RoutineExerciseDao
    abstract fun setTemplateDao(): SetTemplateDao
    abstract fun setParameterDao(): SetParameterDao
    abstract fun pendingSyncDao(): PendingSyncDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun workoutSetResultDao(): WorkoutSetResultDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tabla de sesiones de entrenamiento
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `routineId` INTEGER NOT NULL,
                        `userId` TEXT NOT NULL,
                        `startedAt` INTEGER NOT NULL,
                        `finishedAt` INTEGER NOT NULL,
                        `notes` TEXT,
                        `syncStatus` TEXT NOT NULL,
                        `lastModifiedLocally` INTEGER NOT NULL,
                        FOREIGN KEY(`routineId`) REFERENCES `routines`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent()
                )

                database.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_routineId` ON `workout_sessions` (`routineId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_syncStatus` ON `workout_sessions` (`syncStatus`)")

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_set_results` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `workoutSessionId` INTEGER NOT NULL,
                        `setTemplateId` INTEGER NOT NULL,
                        `parameterId` INTEGER NOT NULL,
                        `repetitions` INTEGER,
                        `numericValue` REAL,
                        `durationValue` INTEGER,
                        `integerValue` INTEGER,
                        `syncStatus` TEXT NOT NULL,
                        FOREIGN KEY(`workoutSessionId`) REFERENCES `workout_sessions`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent()
                )

                database.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_set_results_workoutSessionId` ON `workout_set_results` (`workoutSessionId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_set_results_setTemplateId` ON `workout_set_results` (`setTemplateId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_set_results_syncStatus` ON `workout_set_results` (`syncStatus`)")
            }
        }

        /**
         * Migración v3 → v4: columna exerciseId a workout_set_results.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE workout_set_results ADD COLUMN exerciseId INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * Migración v4 → v5:  columna remoteId a workout_sessions.
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE workout_sessions ADD COLUMN remoteId INTEGER")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE routine_exercises ADD COLUMN circuitGroupId TEXT")
                database.execSQL("ALTER TABLE routine_exercises ADD COLUMN circuitRoundCount INTEGER")
                database.execSQL("ALTER TABLE routine_exercises ADD COLUMN superSetGroupId TEXT")
                database.execSQL("ALTER TABLE routine_exercises ADD COLUMN amrapDurationSeconds INTEGER")
                database.execSQL("ALTER TABLE routine_exercises ADD COLUMN emomIntervalSeconds INTEGER")
                database.execSQL("ALTER TABLE routine_exercises ADD COLUMN emomTotalRounds INTEGER")
                database.execSQL("ALTER TABLE routine_exercises ADD COLUMN tabataWorkSeconds INTEGER")
                database.execSQL("ALTER TABLE routine_exercises ADD COLUMN tabataRestSeconds INTEGER")
                database.execSQL("ALTER TABLE routine_exercises ADD COLUMN tabataRounds INTEGER")
                database.execSQL("ALTER TABLE routine_exercises ADD COLUMN notes TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitapp_offline.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}