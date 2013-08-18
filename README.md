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
It must have the follow name: 
```
<DB_NAME><DB_VERSION>.sql
```

For example: if you use the same name and version from tags of 1º step, your script file must have the follow name: 
```
Application1.sql
```

All tables must contains the column '_id' that is required for provider.
This script must be formatted in UTF-8 encoding and must use UNIX format.

At Notepad++ you can put your script with this specifications as bellow:

    1 - Open your script on notepad++
    2 - Go to menu Format > convert to UTF-8
    3 - Go to menu Edit > convert end line > convert to format UNIX 
    
3 - Implement your provider:

Create a new class that extends 'DplProvider' from this lib.

Implement the methods to map your database tables and 'MATCHER_URI' from provider and the method that return your application's 'R.class'.

- Authority:

	By default, the lib will create the authority for your provider with the follow name: 
```
<your_app_package>.provider
```
	You can change this sub-writing the method 'getAuthority()' from DplProvider.
	
- CONTENT_URI:

	By default, the lib will create the CONTENT_URI from yours Entity with the follow name:
```
CONTENT: "content://";
Authority: described on Authority;
SEPARATOR: '/';
'CONTENT + Authority + SEPARATOR + <your class name>';
```
	You can change this sub-writing the method 'getContentUri()' from ProviderHelper.

4 - Register your provider.
```xml
<provider
	android:name="<YOUR_PROVIDER_CLASS>"
	android:authorities="<Authority>"
	android:exported="false"
	android:multiprocess="true" />
```
- You can read more about 'Content Providers' here: 
	http://developer.android.com/guide/topics/providers/content-providers.html

