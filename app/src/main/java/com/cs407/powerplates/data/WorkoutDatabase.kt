package com.cs407.powerplates.data

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import com.cs407.powerplates.R
import java.util.Date

// User Entity with a unique ID on user name
// Define the User entity with a unique index on userName
@Entity(
    indices = [Index(
        value = ["userName"], unique = true
    )]
)
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0, // Auto-generated primary key for User
    val userName: String = "" // The username field, unique due to the index
)

// TODO: uncomment if need converter classes
// Converter class to handle Date <-> Long type conversions
// Converter class to handle conversion between custom type Data and SQL-compatible type Long
//class Converters {
//    // Converts a timestamp (Long) to a Date object
//    @TypeConverter
//    fun fromTimestamp(value: Long): Date {
//        return Date(value)
//    }
//
//    // Converts a Date object to a timestamp (Long)
//    @TypeConverter
//    fun dateToTimestamp(date: Date): Long {
//        return date.time
//    }
//}

// Exercise Entity
// Define the Exercise entity with a primary key and various fields, including nullable fields
@Entity
data class Exercise(
    @PrimaryKey(autoGenerate = true) val exerciseId: Int = 0, // Auto-generated primary key for Exercise
    val exerciseName: String, // Name of exercise
    // TODO: FILL IN EXERCISE VALUES
//    val noteAbstract: String, // Short summary of the note
//    // Detailed content of the note (optional, might be null)
//    @ColumnInfo(typeAffinity = ColumnInfo.TEXT) val noteDetail: String?,
//    val notePath: String?, // Path to the note's file (optional)
//    val lastEdited: Date // Date of the last edit of the note
)

// UserExerciseRelation
// Define a many-to-many relationship between User and Exercise via UserExerciseRelation
@Entity(
    primaryKeys = ["userId", "exerciseId"], // composite primary key combining userId and exerciseId
    foreignKeys = [ForeignKey(
        entity = User::class, // Foreign key referencing User
        parentColumns = ["userId"], // Parent column in User entity
        childColumns = ["userId"], // Corresponding child column in this entity
        onDelete = ForeignKey.CASCADE // Cascade delete UserExerciseRelation when User is deleted
    ), ForeignKey(
        entity = Exercise::class, // Foreign key referencing Exercise
        parentColumns = ["exerciseId"], // Parent column in Exercise entity
        childColumns = ["exerciseId"], // Corresponding child column in this entity
        onDelete = ForeignKey.CASCADE // Cascade delete UserExerciseRelation when Exercise is deleted
    )]
)
data class UserExerciseRelation(
    val userId: Int, // Foreign key to User
    val exerciseId: Int // Foreign key to Exercise
)

// TODO: uncomment and fill in if we want exercise summaries
// Summary projection of the Exercise entity
// a summary projection of the Exercise entity, for displaying limited fields in queries
data class ExerciseSummary(
    val exerciseId: Int, // ID of the exercise
//    val noteTitle: String, // Title of the note
//    val noteAbstract: String, // Summary of the note
//    val lastEdited: Date // Date of the last edit
)

// DAO for interacting with the User Entity
// DAO (Data Access Object) for interacting with the User entity in the database
@Dao
interface UserDao {
    // Query to get a User by their userName
    @Query("SELECT * FROM user WHERE userNAME = :name")
    suspend fun getByName(name: String): User

    // Query to get a User by their userId
    @Query("SELECT * FROM user WHERE userId = :id")
    suspend fun getById(id: Int): User

    // Query to get a list of ExerciseSummary for a user, ordered by lastEdited
    @Query(
        """SELECT * FROM User, Exercise, UserExerciseRelation
                WHERE User.userId = :id
                AND UserExerciseRelation.userId = User.userId
                AND Exercise.exerciseId = UserExerciseRelation.exerciseId
                ORDER BY Exercise.exerciseName DESC""" // TODO: COULD ORDER BY BEGINNER INTERMEDIATE ADVANCED
    )
    suspend fun getUsersWithExerciseListsById(id: Int): List<ExerciseSummary>

    // Same query but returns a PagingSource for pagination
    @Query(
        """SELECT * FROM User, Exercise, UserExerciseRelation
                WHERE User.userId = :id
                AND UserExerciseRelation.userId = User.userId
                AND Exercise.exerciseId = UserExerciseRelation.exerciseId
                ORDER BY Exercise.exerciseName DESC"""
    )
    fun getUsersWithExerciseListsByIdPaged(id: Int): PagingSource<Int, ExerciseSummary>

    // Insert a new user into the database
    @Insert(entity = User::class)
    suspend fun insert(user: User)
}

// DAO for interacting with the Exercise Entity
@Dao
interface ExerciseDao {
    // Query to get a Exercise by its exerciseId
    @Query("SELECT * FROM exercise WHERE exerciseId = :id")
    suspend fun getById(id: Int): Exercise

    // Query to get a Exercise's ID by its rowId (SQLite internal ID)
    @Query("SELECT exerciseId FROM exercise WHERE rowid = :rowId")
    suspend fun getByRowId(rowId: Long): Int

    // Insert or update a Exercise (upsert operation)
    @Upsert(entity = Exercise::class)
    suspend fun upsert(exercise: Exercise): Long

    // Insert a relation between a user and a exercise
    @Insert
    suspend fun insertRelation(userAndExercise: UserExerciseRelation)

    // Insert or update a Exercise and create a relation to the User if it's a new Exercise
    @Transaction
    suspend fun upsertExercise(exercise: Exercise, userId: Int) {
        val rowId = upsert(exercise)
        if (exercise.exerciseId == 0) { // New exercise
            val exerciseId = getByRowId(rowId)
            insertRelation(UserExerciseRelation(userId, exerciseId))
        }
    }

    // Query to count the number of exercises a user has
    @Query(
        """SELECT COUNT(*) FROM User, Exercise, UserExerciseRelation
                WHERE User.userId = :userId
                AND UserExerciseRelation.userId = User.userId
                AND Exercise.exerciseId = UserExerciseRelation.exerciseId
            """
    )
    suspend fun userExerciseCount(userId: Int): Int
}

// DAO to handle deleting a user and its related exercises
@Dao
interface DeleteDao {
    // Delete a user by their userId
    @Query("DELETE FROM user WHERE userId = :userId")
    suspend fun deleteUser(userId: Int)

    // Query to get all exercise IDs related to a user
    @Query(
        """SELECT Exercise.exerciseId FROM User, Exercise, UserExerciseRelation
            WHERE User.userId = :userId
            AND UserExerciseRelation.userId = User.userId
            AND Exercise.exerciseId = UserExerciseRelation.exerciseId"""
    )
    suspend fun getAllExerciseIdsByUser(userId: Int): List<Int>

    // Delete exercises by their IDs
    @Query("DELETE FROM exercise WHERE exerciseId IN (:exercisesIds)")
    suspend fun deleteExercises(exercisesIds: List<Int>)

    // Transaction to delete a user and all their exercises
    @Transaction
    suspend fun delete(userId: Int) {
        deleteExercises(getAllExerciseIdsByUser(userId))
        deleteUser(userId)
    }
}

// Room Database Class with ALL Entities and DAO
// Database class with all entities and DAOs
@Database(entities = [User::class, Exercise::class, UserExerciseRelation::class], version = 1)
// Database class with all entities and DAOs
//@TypeConverters(Converters::class)
abstract class ExerciseDatabase : RoomDatabase() {
    // Provide DAOs to access the database
    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun deleteDao(): DeleteDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null

        // Get or create the database instance
        fun getDatabase(context: Context): ExerciseDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is null, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseDatabase::class.java,
                    context.getString(R.string.workout_database), // Database name from resources
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
