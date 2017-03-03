# README

This is a repository of the Aplha (demo) version of Xplore, an assistant for hikers.

This repo was created for the sole reason to make backing up and version control easier for me.


### Requirements

Minimum Android SDK Version: 16  (v4.1 Jelly Bean)

Target SDK Version: 25

Max SDK Version: 25 (v7.1.1 Nougat)


### Setting Up Dev Environment

This repo doesn't include private keys and temporary files.

For anyone working on Xplore, write to nikaoto@gmail.com to request environment files.


### Installation

Install *xplore_release* APK on a compatible android device.

If it doesn't work, install *xplore_debug* APK instead. 


### Libraries Used

Here's a small snippet of code from *build.gradle* which lists every library used in Xplore.
```java
    dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    compile 'com.android.support:appcompat-v7:25.0.0'
    compile 'com.android.support:design:25.0.0'
    compile 'com.google.android.gms:play-services:10.0.1'
    compile 'com.squareup.picasso:picasso:2.5.2'
    compile 'com.google.maps.android:android-maps-utils:0.4+'
    compile 'com.google.firebase:firebase-database:10.0.1'
    compile 'com.google.firebase:firebase-auth:10.0.1'
    compile 'com.google.code.gson:gson:2.8.0'
    compile 'jp.wasabeef:picasso-transformations:2.1.0'
    testCompile 'junit:junit:4.12'
	}

	apply plugin: 'com.google.gms.google-services'
```
### Who do I talk to?

Repo owner, Nika Otiashvili at nikaoto@gmail.com
