package com.cs407.powerplates.data

import android.content.Context
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
import org.json.JSONArray
import java.util.Date

// User Entity with a unique ID on user name
@Entity(
    indices = [Index(
        value = ["userName"], unique = true
    )]
)
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0, // Auto-generated primary key for User
    val userName: String = "" // The username field, unique due to the index
)

// History entity with primary key on userId
@Entity
data class History(
    @PrimaryKey(autoGenerate = true) val historyId: Int = 0, // Auto-generated primary key for History
    val userId: Int,
    val category: String,
    val exercise1: String,
    val exercise2: String,
    val exercise3: String,
    val date: String
)

// RankedPrefernces entity with primary key on userId
@Entity(
    primaryKeys = ["userId"], // composite primary key combining userId and exerciseId
    foreignKeys = [ForeignKey(
        entity = User::class, // Foreign key referencing User
        parentColumns = ["userId"], // Parent column in User entity
        childColumns = ["userId"], // Corresponding child column in this entity
        onDelete = ForeignKey.CASCADE // Cascade delete UserExerciseRelation when User is deleted
    )]
)
data class RankedPrefs(
    val userId: Int,
    val r1: String,
    val r2: String,
    val r3: String,
    val r4: String,
    val r5: String,
)

// TODO: uncomment if need converter classes
// Converter class to handle Date <-> Long type conversions
// Converter class to handle conversion between custom type Data and SQL-compatible type Long
class Converters {
    // Converts a timestamp (Long) to a Date object
    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }

    // Converts a Date object to a timestamp (Long)
    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }
}

// Define the Exercise entity with a primary key and various fields, including nullable fields
@Entity
data class Exercise(
    @PrimaryKey(autoGenerate = true) val exerciseId: Int = 0, // Auto-generated primary key for Exercise
    val exerciseName: String,
    val primaryMuscle: String,
    val secondaryMuscle: String,
    val compound: Boolean, // true if compound, false if not compound
    val type: String, // strength, mass, mobility, stamina, body fat
    val type2: String,
    val level: String, // beginner, intermediate, advanced
    val progressionType: String, // reps, weight, or time
    val category: String, // push, pull, legs, abs, or cardio
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

// Summary projection of the Exercise entity, for displaying limited fields in queries
data class ExerciseSummary(
    val exerciseId: Int,
    val primaryMuscle: String,
    val type: String,
    val level: String,
    val category: String
)

// DAO for interacting with the User Entity
@Dao
interface UserDao {
    // Query to get a User by their userName
    @Query("SELECT * FROM User WHERE userName = :name")
    suspend fun getByName(name: String): User

    // Query to get a User by their userId
    @Query("SELECT * FROM User WHERE userId = :id")
    suspend fun getById(id: Int): User

    // Query to get a list of ExerciseSummary for a user, ordered by lastEdited
    @Query(
        """SELECT * FROM User u, Exercise e, UserExerciseRelation ue
                WHERE u.userId = :id
                AND ue.userId = u.userId
                AND e.exerciseId = ue.exerciseId
                ORDER BY e.exerciseName DESC"""
    // TODO: ORDER BY BEGINNER INTERMEDIATE ADVANCED
    )
    suspend fun getUsersWithExerciseListsById(id: Int): List<ExerciseSummary>

    // Insert a new user into the database
    @Insert(entity = User::class)
    suspend fun insert(user: User)
}

// DAO for interacting with the Exercise Entity
@Dao
interface ExerciseDao {
    // get primary muscle group given exercise name
//    @Query("SELECT e.primaryMuscle FROM exercise e WHERE e.exerciseName == :name")
//    suspend fun getPrimaryMuscleFromName(name: String): String

    // get difficulty level given exercise name
    @Query("SELECT e.level FROM exercise e WHERE e.exerciseName == :name")
    suspend fun getLevelFromName(name: String): String

    @Query("SELECT e.progressionType FROM exercise e WHERE e.exerciseName == :name")
    suspend fun getProgTypeFromName(name: String): String

    // Query to count all exercises
    @Query("SELECT COUNT(*) FROM exercise")
    suspend fun getExerciseCount(): Int

    // Query to get all exercise names
    @Query("SELECT e.exerciseName FROM Exercise e")
    suspend fun getAllNames(): List<String>

    // Query to get all exercise primary muscle group
    @Query("SELECT e.primaryMuscle FROM Exercise e")
    suspend fun getAllPrimaryMuscles(): List<String>

    // Query to get all exercise levels
    @Query("SELECT e.level FROM Exercise e")
    suspend fun getAllLevels(): List<String>

    @Query("SELECT e.exerciseName FROM Exercise e WHERE e.category == :category")
    suspend fun getAllNamesByCategory(category: String): List<String>

    @Query("SELECT e.primaryMuscle FROM Exercise e WHERE e.category == :category")
    suspend fun getAllPrimaryMusclesByCategory(category: String): List<String>

    @Query("SELECT e.level FROM Exercise e WHERE e.category == :category")
    suspend fun getAllLevelsByCategory(category: String): List<String>

    // Query to get a Exercise by its exerciseId
//    @Query("SELECT * FROM exercise WHERE exerciseId = :id")
//    suspend fun getById(id: Int): Exercise

    // Query to get a Exercise's ID by its rowId (SQLite internal ID)
    @Query("SELECT exerciseId FROM exercise WHERE rowid = :rowId")
    suspend fun getByRowId(rowId: Long): Int

    // Query to get Exercise object from exerciseName
    @Query("SELECT e.exerciseId FROM exercise e WHERE exerciseName = :exerciseName")
    suspend fun getIdFromName(exerciseName: String): Int

    // Insert or update a Exercise (upsert operation)
    @Upsert(entity = Exercise::class)
    suspend fun upsert(exercise: Exercise): Long

    // Insert a relation between a user and a exercise
    @Insert
    suspend fun insertRelation(userAndExercise: UserExerciseRelation)

    // Insert or update a Exercise and create a relation to the User if it's a new Exercise
    @Transaction
    suspend fun upsertExercise(userId: Int, exercise: Exercise) {
        val rowId = upsert(exercise)
        if (exercise.exerciseId == 0) { // New exercise
            val exerciseId = getByRowId(rowId)
            insertRelation(UserExerciseRelation(userId, exerciseId))
        }
    }

    @Query("""SELECT Exercise.exerciseName FROM Exercise, UserExerciseRelation
                WHERE UserExerciseRelation.userId = :userId
                AND UserExerciseRelation.exerciseId == Exercise.exerciseId""")
    suspend fun getUerExercisesByUID(userId: Int): List<String>

    @Transaction
    suspend fun insertUserExerciseRelation(userId: Int, exerciseName: String) {
        val exerciseId = getIdFromName(exerciseName)
        insertRelation(UserExerciseRelation(userId, exerciseId))
    }

    @Query("SELECT COUNT(*) FROM UserExerciseRelation uer WHERE uer.userId = :userId AND uer.exerciseId = :exerciseId")
    suspend fun countUerByUIDandEID(userId: Int, exerciseId: Int): Int

    // Query to get number of UserExerciseRelations based on exercise category and user ID
    @Query("""
        SELECT COUNT(*) FROM UserExerciseRelation uer 
        WHERE uer.exerciseId IN
            (SELECT e.exerciseId FROM Exercise e WHERE e.category == :category)
            AND uer.userId == :userId
        """)
    suspend fun getUserExerciseCountByCategory(userId: Int, category: String): Int

    @Query("""
        SELECT e.exerciseName FROM UserExerciseRelation uer, Exercise e
        WHERE uer.userId == :userId
        AND uer.exerciseId == e.exerciseId
        AND e.category == :category
    """)
    suspend fun getSavedWorkoutsByCategory(userId: Int, category: String): List<String>

    // Query to count the number of exercises a user has
    @Query(
        """SELECT COUNT(*) FROM User, Exercise, UserExerciseRelation
                WHERE User.userId = :userId
                AND UserExerciseRelation.userId = User.userId
                AND Exercise.exerciseId = UserExerciseRelation.exerciseId
                AND Exercise.category = :category
            """
    )
    suspend fun userExerciseCount(userId: Int, category: String): Int
}

// DAO to handle adding and editing user's ranked preferences
@Dao
interface RankedDao {
    @Insert
    suspend fun insertPrefs(userPrefs: RankedPrefs)

    @Query("""
        SELECT * FROM RankedPrefs rp
        WHERE userId == :userId
    """)
    fun getUserPreferences(userId: Int): RankedPrefs
}

// DAO to handle adding workouts to history
@Dao
interface HistoryDao {
    @Insert
    suspend fun insertExercise(exercise: History)
}

// DAO to handle deleting a user and its related exercises
@Dao
interface DeleteDao {
    // delete user preferences based on userId
    @Query("""
        DELETE FROM RankedPrefs
        WHERE userId == :userId
    """)
    suspend fun removeUserPreferences(userId: Int)

    // Query to delete an exercise from a user's relation based on its name
    @Query("""DELETE FROM UserExerciseRelation 
                WHERE UserExerciseRelation.exerciseId IN 
                (SELECT e.exerciseId FROM Exercise e WHERE e.exerciseName == :exerciseName)""")
    suspend fun deleteExerciseFromUERelation(exerciseName: String)


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
@Database(entities = [User::class, Exercise::class, UserExerciseRelation::class, RankedPrefs::class, History::class], version = 6)
// Database class with all entities and DAOs
@TypeConverters(Converters::class)
abstract class ExerciseDatabase : RoomDatabase() {
    // Provide DAOs to access the database
    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun deleteDao(): DeleteDao
    abstract fun rankedDao(): RankedDao
    abstract fun historyDao(): HistoryDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null

        // Get or create the database instance
        fun getDatabase(context: Context): ExerciseDatabase {
            // if the INSTANCE is not null, then return it,
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseDatabase::class.java,
                    context.getString(R.string.exercise_database), // Database name from resources
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance // if it is null, then create the database
            }
        }
    }
}

// read exercise_list.json to populate db with Exercise objects
fun readJsonFromAssets(context: Context): JSONArray {
    val json = context.assets.open("exercise_list.json").bufferedReader().use { JSONArray(it.readText()) }
    return json
}

// populate database with existing exercise data
suspend fun populateExercises(context: Context, exerciseDao: ExerciseDao) {
    // read the JSON file from assets
    val exercisesJson = readJsonFromAssets(context)

    // map the JSON objects to Exercise objects and upsert into db
    for (i in 0 until exercisesJson.length()){
        val item = exercisesJson.getJSONObject(i)
        val exerciseName = item.getString("Exercise")
        val primaryMuscle = item.getString("Primary Muscle")
        val secondaryMuscle = item.getString("Secondary Muscle")
        val compound = item.getBoolean("Compound")
        val type = item.getString("Type")
        val type2 = item.getString("Type 2")
        val level = item.getString("Level")
        val progressionType = item.getString("Progression Type")
        val category = item.getString("Category")

        val exerciseEntity = Exercise(
            i, exerciseName, primaryMuscle, secondaryMuscle, compound, type, type2, level, progressionType, category
        )
        exerciseDao.upsert(exerciseEntity)
    }
}
