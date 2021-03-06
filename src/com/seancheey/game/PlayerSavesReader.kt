package com.seancheey.game

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.ObjectInputStream
import java.security.MessageDigest
import java.util.*


/**
 * Created by Seancheey on 29/05/2017.
 * GitHub: https://github.com/Seancheey
 */
class PlayerSavesReader(val name: String, val pass: String) {
    var objin: ObjectInputStream? = null
    var hasSaves: Boolean = true
    private val player: Player?

    companion object {
        fun encryptPass(str: String): ByteArray {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(str.toByteArray(charset("UTF-8")))
            return md.digest()
        }
    }

    init {
        try {
            val fin = FileInputStream(Config.playerSavePath(name))
            objin = ObjectInputStream(fin)
        } catch (e: FileNotFoundException) {
            hasSaves = false
        }
        player = objin?.readObject() as Player?
    }

    fun passwordCorrect(): Boolean {
        return Arrays.equals(player?.pass_SHA, encryptPass(pass))
    }

    fun readPlayer(): Player? {
        if (passwordCorrect())
            return player
        else
            return null
    }

}