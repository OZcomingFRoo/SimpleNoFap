package com.example.simplenofap.notifications

object ScheduledNotificationValidator {
    const val MinTimeMinutesOfDay = 0
    const val MaxTimeMinutesOfDay = 1_439

    fun validate(daysOfWeekMask: Int, timeMinutesOfDay: Int) {
        require(DaysOfWeekMask.isValid(daysOfWeekMask)) {
            "daysOfWeekMask must be in ${DaysOfWeekMask.MinValidMask}..${DaysOfWeekMask.MaxValidMask}."
        }
        require(timeMinutesOfDay in MinTimeMinutesOfDay..MaxTimeMinutesOfDay) {
            "timeMinutesOfDay must be in $MinTimeMinutesOfDay..$MaxTimeMinutesOfDay."
        }
    }
}
