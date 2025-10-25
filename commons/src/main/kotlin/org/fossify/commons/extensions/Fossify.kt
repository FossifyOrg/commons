package org.fossify.commons.extensions

import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_SIGNATURES
import android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
import android.content.pm.Signature
import android.util.Base64
import android.util.Base64.NO_PADDING
import org.fossify.commons.helpers.isPiePlus
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest

private const val FOSSIFY_HASH =
    "MjkyZmY2ZDBjM2MxNjhjNjg3MmEwMmU2MDM0YWJmNDE1MGFiY2FlOWFjYmQ4OWYyYzAyNzRmM2Q0MjFiZGZmYQ"

@ExperimentalStdlibApi
fun PackageManager.checkSignature(packageName: String?): Boolean {
    if (packageName.isNullOrEmpty()) return false
    val signatures = getSignatures(packageName).orEmpty()
    val expectedHash = Base64.decode(FOSSIFY_HASH, NO_PADDING).toString(UTF_8)
    for (signature in signatures) {
        val messageDigest = MessageDigest.getInstance("SHA256")
            .apply {
                update(signature.toByteArray())
            }

        val hash = messageDigest.digest().toHexString().reversed()
        if (hash == expectedHash) {
            return true
        }
    }

    return false
}

fun PackageManager.getSignatures(packageName: String): Array<out Signature>? {
    @Suppress("DEPRECATION")
    return if (isPiePlus()) {
        getPackageInfo(packageName, GET_SIGNING_CERTIFICATES).signingInfo?.apkContentsSigners
    } else {
        getPackageInfo(packageName, GET_SIGNATURES).signatures
    }
}
