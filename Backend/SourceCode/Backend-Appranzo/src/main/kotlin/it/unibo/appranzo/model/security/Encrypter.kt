package it.unibo.appranzo.model.security

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Encrypter {
    companion object EncrypterSha512{
        fun encrypt(message: String): String?{
            try {
                val digester: MessageDigest = MessageDigest.getInstance("SHA-512")
                val encryptedMessage = digester.digest(message.toByteArray())
                return BigInteger(1,encryptedMessage).toString(16)
            }
            catch (e : NoSuchAlgorithmException){
                return null
            }
        }

        fun compareStrings(string: String, stringHashed: String): Boolean{
            val encrypted = this.encrypt(string)
            return encrypted!=null && encrypted == stringHashed
        }
    }

}