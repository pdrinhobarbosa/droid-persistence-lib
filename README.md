droid-persistence-lib
=====================

A lib to help you make your Android app persist data on SQLite via ContentProvider

Developed By
============

* Pedro Barosa - <pdrinhobarbosa@gmail.com>

Steps
=====

1 - Put the follow tags at AndroidManifest.xml application tag:
```xml
<meta-data android:name="DB_NAME" android:value="Application"/>
<meta-data android:name="DB_VERSION" android:value="1"/>
```
2 - Create a script to make your database.
  
This script must be in 'raw' folder at 'res' Android's application folder. 
It must have the follow name: '<DB_NAME><DB_VERSION>.sql'
  
For example: if you use the same name and version from tags of 1º step, your script file must have the follow name: 
  'Application1.sql'

All tables must contains the column '_id' that is required for provider.

This script must be formatted in UTF-8 encoding and must use UNIX format.

At Notepad++ you can put your script with this specifications as bellow:

- [1] Open your script on notepad++
