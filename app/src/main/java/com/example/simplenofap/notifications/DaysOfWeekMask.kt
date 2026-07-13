package com.example.simplenofap.notifications

object DaysOfWeekMask {
    const val MinValidMask = 1
    const val MaxValidMask = 127

    fun of(vararg days: NotificationWeekday): Int {
        return days.fold(0) { mask, day -> mask or day.maskBit }
    }

    fun contains(mask: Int, day: NotificationWeekday): Boolean {
        require(isValid(mask)) { "daysOfWeekMask must be in 1..127." }
        return mask and day.maskBit != 0
    }

    fun isValid(mask: Int): Boolean = mask in MinValidMask..MaxValidMask
}
