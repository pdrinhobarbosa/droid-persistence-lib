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

5 - Create your Entities:

	Important: The lib will work with this with created cascade and lazy for queries.
	
To do this, create a new class that extends BaseEntity from this lib.

The BaseEntity class has the attribute '_id' required by provider. 
Within are the default methods to insert, update, delete and query using reflection to read your entity attributes to fill ContentValues or your object attributes.

To create a relationships for your entities, follow the best step:
- One_To_One: (use @DplObject annotation)

	For this situation, your database must have a column for User at People's table.

	The lib will put the '_id' attribute at this column.

	If you create a new People, the lib will create a new User if this not exist.

	But to get a People from database, the lib will put only a User object with '_id' attribute of User loaded.

	Use @DplObject(save=false) to define relationship with sub-object, but don't save on cascade.
	
```java
public class People extends BaseEntity {
	private String name;
	
	@DplObject
	private User user;
}
```
```java
public class User extends BaseEntity {
	private String login;
	private String password;
}
```
- Many_To_One and Many_To_Many: (use @DplList)
	
	The @DplList Annotation is used to identify your Collections

- Many_To_One:

```java
public class People extends BaseEntity {
	private String name;
	
	@DplList
	private ArrayList<Phone> phones;
}
```
```java
public class Phone extends BaseEntity {
	private String number;
	private People people;
}
```
- Many_To_Many:
	 
	This example will use a relationship table.

```java
public class User extends BaseEntity {
	private String login;

	@DplList
	private ArrayList<UserRole> roles;
}
```
```java
public class Role extends BaseEntity {
	private String name;

	@DplList
	private ArrayList<UserRole> users;
}
```
```java
public class UserRole extends BaseEntity {
	@DplObject
	private User user;

	@DplObject
	private Role role;
}
```

6 - Notifying JOINS:

	This example use the USER_CODE and ROLE_CODE mapped on URI_MATCHER to notify UserRole uri that user or role data has changed.
	
	And use the following call to get content uri of UserRole.class:
```java	
DplProvider.getContentUri(getContext(), UserRole.class)
```
```java
switch (URI_MATCHER.match(uri)) {
	case USER_CODE:
		getContext().getContentResolver().notifyChange(DplProvider.getContentUri(getContext(), UserRole.class), null);
		break;
	
	case ROLE_CODE:
		getContext().getContentResolver().notifyChange(DplProvider.getContentUri(getContext(), UserRole.class), null);
		break;

	default:
		break;
}
```

7 - Delete cascade:


