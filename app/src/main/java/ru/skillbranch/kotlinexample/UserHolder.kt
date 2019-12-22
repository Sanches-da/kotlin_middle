package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.IllegalArgumentException
import java.util.*

object UserHolder {
    private val map = mutableMapOf<String, User>()

    private fun String.fixLogin(): String = this.trim().toLowerCase(Locale.getDefault())

    private fun validatePhone(phone: String):Boolean = phone.fixLogin().matches("^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}\$".toRegex())

    private fun validateEmail(email: String):Boolean = email.fixLogin().matches("^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}\$".toRegex())



    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User = when {
        map[email.fixLogin()]!=null -> throw IllegalArgumentException("A user with this email already exists")
        !validateEmail(email) -> throw IllegalArgumentException("Email is not valid")
        password.isBlank() -> throw IllegalArgumentException("Password must not be blank")
        else -> User.makeUser(fullName, email = email, password = password)
            .also { user -> map[email.fixLogin()] = user }
    }


    fun loginUser(login: String, password: String): String? {
        return map[login.fixLogin()]?.run{
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    fun clearHolder() {
        map.clear()
    }

    fun registerUserByPhone(
        fullName: String,
        phone: String
    ): User =
        when {
            map[phone.fixLogin()] != null -> throw IllegalArgumentException("A user with this phone already exists")
            !validatePhone(phone) -> throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
            else -> User.makeUser(fullName, phone=phone)
                .also { user -> map[phone.fixLogin()] = user }
        }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun requestAccessCode(phone: String) {
        map[phone.fixLogin()]?.generateAccessCodeAndSend()
    }

    fun importUsers(usersList: List<String>):List <User> =
        usersList.map {
            val (fullName, email, pass_salt, phone) = it.split(";")
            val mEmail:String? = if(email.isBlank()) null else email
            val mPhone:String? = if(phone.isBlank()) null else phone
            val mPassSalt = if(pass_salt.isBlank()) ":" else pass_salt
            if (mPhone!=null || mEmail!=null) {
                User.makeUser(fullName.trim(), email = mEmail, phone = mPhone, saltPass = mPassSalt)
                    .also { user -> map[(mPhone ?: mEmail)!!.fixLogin()] = user }
            }else{
                throw IllegalArgumentException("Email or phone must be filled")
            }

        }
}