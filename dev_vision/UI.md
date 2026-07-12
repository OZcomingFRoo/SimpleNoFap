# Screens

The UI screen should have a burger side-menu to move across different screens.
And important note for designing the screens is 2 main things:
- Support for RTL language like Hebrew
- Support for chaning app's theme (Light and dark) 

The following are the main screens for the user to manage:

## Settings

Basic screen to set all of the data on the user such as:

- Name
- Theme
- App's language (English and Hebrew)

The screen should also display app's version and some little detailed if needed

## Counter Screen (Main)

This should mainly display the amonut of time passed since user begin his NoFap challange.
Below the counter, should be the recent DaySteaks the user aquired.
We'll display the last 5 earned

## Day-Strearks Achieve

A simple list that user will be able to filter.
The Day-Strearks list will be a grid of 3x3 that display the icons of the Day Streaks.
Each cell contains one time of DaySteak with a badge to the side of it that displays the amount of available streaks for the user to use in case of a relapse.

## Motification manager

The should display a simple list of notifications for the user to manage.
It will have the basic CRUD operations for the user.

To create a new notification or edit an existing one, the user will open a sheet form for the notification.

# Widget Counter

This widget should display to the user, in real time, the amount of days, hours, and seconds that passed since he begin the noFap.
What makes the UI special, is the fact that the look of the font of the digits that pass change to look more "cool" as the timer incearses the amount of time the user remains in the noFap.

---

# Note about the UI design

There's a log more to talk about, so we'll create a dedicated Github issue for each screen