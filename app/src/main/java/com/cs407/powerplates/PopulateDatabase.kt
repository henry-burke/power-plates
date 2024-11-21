package com.cs407.powerplates

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cs407.powerplates.data.Exercise
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import com.cs407.powerplates.data.ExerciseDatabase
import com.cs407.powerplates.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PopulateDatabase(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.Main).launch {
            fillWithStartingExercises(context)
        }
    }

    private fun loadJSONArray(context: Context): JSONArray {

        val inputStream = context.resources.openRawResource(R.raw.exercise_list)

        BufferedReader(inputStream.reader()).use {
            return JSONArray(it.readText())
        }
    }

    private suspend fun fillWithStartingExercises(context: Context){
        val dao = ExerciseDatabase.getDatabase(context).exerciseDao()

        try {
            val exercises = loadJSONArray(context)
            for (i in 0 until exercises.length()){
                val item = exercises.getJSONObject(i)
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
                dao.upsert(exerciseEntity)
            }
        }

        catch (e: JSONException) {
             Log.v("fillWithStartingNotes: $e", e.toString())
        }
    }
}