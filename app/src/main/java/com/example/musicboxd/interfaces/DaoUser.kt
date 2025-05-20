package com.example.musicboxd.interfaces
import com.example.musicboxd.classes.User


//si occupa solo di accedere ai dati (lettura/scrittura su Firestore)
//non conosce la UI, è solo una sorgente dati

interface UserDao {
    fun getAll(callback: (List<User>) -> Unit)
    fun insert(user: User, callback: (Boolean) -> Unit)
    fun update(user: User, callback: (Boolean) -> Unit)
    fun delete(user: User, callback: (Boolean) -> Unit)
}
