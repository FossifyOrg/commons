package org.fossify.commons.enums

enum class ConnectionTypes(val type: String) {
    SMB("SMB"),
    WebDav("WebDav"),
    DAVx5("DAVx5"),
    SFTP("SFTP"),
    FTP("FTP"),

    Default("Default");

    companion object {
        fun fromType(value: String): ConnectionTypes {
            return entries.find { it.type == value } ?: Default
        }
    }
}

