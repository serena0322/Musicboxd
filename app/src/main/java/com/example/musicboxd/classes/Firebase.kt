package com.example.musicboxd.classes
import com.example.musicboxd.interfaces.UserDao
import com.google.firebase.firestore.FirebaseFirestore

//DAO (Data Access Object) specializzato per lavorare con Firebase
//Serve a centralizzare e astrarre l'accesso ai dati degli utenti memorizzati in Firebase Firestore

class FirebaseUserDaoImpl : UserDao {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    override fun getAll(callback: (List<User>) -> Unit) { //per notificare il chiamante dell'esito (successo o fallimento)
        usersCollection.get()
            .addOnSuccessListener { result ->
                val userList = result.documents.mapNotNull { it.toObject(User::class.java) }
                callback(userList)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    override fun insert(user: User, callback: (Boolean) -> Unit) {
        usersCollection.document(user.id).set(user)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    override fun update(user: User, callback: (Boolean) -> Unit) {
        usersCollection.document(user.id).set(user)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    override fun delete(user: User, callback: (Boolean) -> Unit) {
        usersCollection.document(user.id).delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}


