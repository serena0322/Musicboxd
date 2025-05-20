package com.example.musicboxd.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicboxd.classes.User
import com.example.musicboxd.interfaces.UserDao

//fa da intermediario tra DAO e UI, gestisce il flusso dati,
// mantiene lo stato e fornisce dati tramite LiveData osservabili
//conosce la UI, ma non si occupa direttamente di accesso dati, delega al DAO

class UserViewModel (private val userDao: UserDao) : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    fun getAllUsers() {
        userDao.getAll { userList ->    //il ViewModel non dipende direttamente da Firebase, ma solo dall’interfaccia DAO
            _users.postValue(userList)
        }
    }

}

