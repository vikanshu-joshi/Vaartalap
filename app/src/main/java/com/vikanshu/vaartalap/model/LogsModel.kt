package com.vikanshu.vaartalap.model

enum class LogType {
    OUTGOING,
    MISSED,
    RECIEVED,
    REJECTED,
    BLOCKED
}

class LogsModel(
    val ID: String,
    val UID: String,
    val NAME: String,
    val NUMBER: Long,
    val TYPE: LogType,
    val TIME: Long,
    val START: Long,
    val END: Long
)