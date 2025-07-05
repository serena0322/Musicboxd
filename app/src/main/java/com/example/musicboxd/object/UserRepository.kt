package com.example.musicboxd.`object`

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicboxd.local.UserActivity
import com.example.musicboxd.local.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

//Intermediario tra l’interfaccia utente (UI) e Firebase Firestore

data class UserWithActivities(
    val user: User?,
    val activities: List<UserActivity>
)

object UserRepository {
    private val _currentUser = MutableLiveData<UserWithActivities?>()
    val currentUser: LiveData<UserWithActivities?> = _currentUser

    suspend fun loadUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        val userDoc = firestore.collection("User").document(uid).get().await()
        val user = userDoc.toObject(User::class.java)

        val activityDocs = firestore.collection("Activity")
            .whereEqualTo("sourceUserId", uid)
            .get()
            .await()

        val activities = activityDocs.mapNotNull { it.toObject(UserActivity::class.java) }

        _currentUser.postValue(UserWithActivities(user, activities))
    }

    //Permette di aggiornare uno specifico campo del documento utente in Firestore
    fun updateField(
        field: String,
        value: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("User")
            .document(uid)
            .set(mapOf(field to value), SetOptions.merge())
            .addOnSuccessListener {
                val current = _currentUser.value
                val user = current?.user
                if (user != null) {
                    when (field) {
                        "username" -> user.username = value
                        "firstName" -> user.firstName = value
                        "lastName" -> user.lastName = value
                    }

                    // Ricostruisce il nuovo oggetto aggiornato e lo pubblica
                    _currentUser.postValue(
                        UserWithActivities(
                            user = user,
                            activities = current.activities
                        )
                    )
                }
                onSuccess()
            }.addOnFailureListener { onFailure(it) }
    }
}
