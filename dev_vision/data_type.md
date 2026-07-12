# Data Types

The will use 2 different data Storages:
 - Database 
 - Jetpack DataStore Preferences

## User

This will be set in Jetpack DataStore Preferences:

- Name (String)
- Selected Theme required (Enum value of: Light (Default), Dark, and based on System)
- Selected Language required (Enum value of: English (Default), Hebrew, and based on System's language)
- StartNoFapAt required (Datetime  that records the last relapse the user did)

The value "StartNoFapAt" is the crurial part of the app

## Schedule Notifications

This will be a Database table that keeps all the notifications
The purpose of the notifications is make the user aware to not give in to the urge at hours where he's most susceptible to relapse.
Words of encouragement and warning should accompany the user.

Columns:

- NotifyAtDayOfTheWeek required (int column that represents 1-7 days of the week)
- NotifyAtTime required (time column)
- Message required (string. if not set by, default to a generated message by a presets of messages that are mutli-lang)
- Title optional (string)
- Icon (Enum of icons)
- Active (Boolean for turning on/off the notification)

## DayStreak (rewards that can be used)

This will be a Database table.
These reward play a crurial part for encouraging users to keep going, and not give in easily to one relapse.
Some users can hold off for 3 weeks, but might fail once.
Instead of simply resetting the counter to ZERO, let users use "DayStreak" to hold the reset until they manage to hold off or not.
The point is not to force users to reset on every small mistake but encourage them to slowly stop succumbing to the urge, as a smoker needs to smoke less and less as time goes on until he stops completely.

So, if a user manages to for 3 weeks to urge and then he does, let him use the "DaySteak" to save himself from resetting.
If the user has no un-used DayStreaks, or he no option but to reset.

Note - this database is managed by the system when it comes to creating and deleting.
The user obviously have limited options to manage it

Colums:

 - StreakName (String)
 - Achieved At (Datetime)
 - StreakType (Enum of Steaks which will be detailed in `day_streaks.md` day_streaks.md)
 - Used (boolean. This is the only field the user can set)
 - Icon (Enum)