package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

class User private constructor(
    val firstName: String,
    val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
) {
    val userInfo: String
    private val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .capitalize()
    private val initials: String
        get() = listOfNotNull(firstName, lastName)
            .map{ it.first().toUpperCase()}
            .joinToString(" ")
    private var phone: String? = null
        set(value){
            field = value?.replace("[^+\\d]".toRegex(), "")
        }
    private var _login: String? = null
    var login: String
        set(value){
            _login = value.toLowerCase(Locale.getDefault())
        }
        get() = _login!!
    private var salt: String
    private lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null


    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ): this(firstName, lastName, email = email, meta = mapOf("auth" to "password")){
        passwordHash = encrypt(password)
    }

    constructor(
        firstName: String,
        lastName: String?,
        phone: String
    ): this(firstName, lastName, rawPhone = phone, meta = mapOf("auth" to "sms")){
        generateAccessCodeAndSend()
    }

    constructor(
        firstName: String,
        lastName: String?,
        phone: String? = null,
        email: String? = null,
        salt: String,
        hash: String
    ): this(firstName, lastName, rawPhone = phone, email = email, meta = mapOf("src" to "csv")){
        this.salt = salt
        this.passwordHash = hash
    }


    fun generateAccessCodeAndSend() {
        if (phone.isNullOrBlank()) throw java.lang.IllegalArgumentException("User has no phone!")

        val code = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code

        sendAccessCodeToUser(phone!!, code)
    }

    init {

        check(!firstName.isBlank()){"First name must be not blank"}
        check(email.isNullOrBlank() || rawPhone.isNullOrBlank()){"Email or phone must be not blank"}

        salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()

        phone = rawPhone
        login = email ?: phone!!

        userInfo = """
          firstName: $firstName
          lastName: $lastName
          login: $login
          fullName: $fullName
          initials: $initials
          email: $email
          phone: $phone
          meta: $meta
        """.trimIndent()
    }


    fun checkPassword(pass: String) = encrypt(pass) == passwordHash

    fun changePassword(oldPass: String, newPass: String){
        if (checkPassword(oldPass)) passwordHash = encrypt(newPass)
        else throw java.lang.IllegalArgumentException("The entered password does not much the current password")
    }


    private fun sendAccessCodeToUser(phone: String, code: String) {
        println("..... Sending access code: $code on $phone")
    }


    private fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        return StringBuilder().apply {
            repeat(6){
                (possible.indices).random().also{index->
                    append(possible[index])
                }
            }
        }.toString()
    }

    private fun encrypt(password: String) = salt.plus(password).md5()

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }


    companion object Factory{
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null,
            saltPass: String? = null
        ): User {
            val (firstName, lastName) = fullName.fullNameToPair()
            val (salt, hash) = (saltPass?:":").split(":")

            return when {
                !saltPass.isNullOrBlank() -> User(firstName, lastName, phone, email, salt, hash)
                !phone.isNullOrBlank() -> User(firstName, lastName, phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(firstName, lastName, email, password)
                else -> throw java.lang.IllegalArgumentException("Email or phone must be not null or empty")
            }
        }

        private fun String.fullNameToPair(): Pair<String, String?>{
            return this.split(" ")
                .filter{ it.isNotBlank()}
                .run{
                    when(size){
                        1 -> return@run first() to null
                        2 -> return@run first() to last()
                        //else -> return@run first() to drop(1).joinToString(" ")
                        else -> throw IllegalArgumentException("Fullname must contain only first name and last name, current split result $this ")
                    }
                }
        }
    }

}