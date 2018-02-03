package com.eginez.kopass

import java.io.InputStream

/**
 * Created by eginez on 12/30/17.
 */

interface Concealer {
    fun setup()
    fun encrypt(message: ByteArray): ByteArray
    fun decrypt(message: ByteArray): ByteArray
}

