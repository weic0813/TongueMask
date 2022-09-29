# TongueMask

## Introduction
**TongueMask** app is a graphic GUI for our automatic tongue diagnosis,which was running on `android` phone.
It was build with `Kotlin` in **Android Studio**.
* * *

## Environment Setup/Android  Requirements
To check the environment setup,you can check `settings.gradle` and others `gradle` file.
The app runs on at least `Android` **9.0** or higher.
* * *

##Mobile App Implementation
The camera user interface in our app is built with the `CameraX` API library.
We add a frame in the form of an `XML` file for users be able to align their tongue to it.
We implement the album function by using an `explicit intent` to call the album app in mobile phones.
* * *

## App Execution
First, the user enters the app main screen, then he/she can choose to click `the camera icon` to shoot a tongue photo directly, or to click `the album icon` to select tongue pictures stored in the album. Both ways are used to decide images to upload and the app shows the image on the screen for second confirmation. After confirmation, the user needs to send the image to our computing server by clicking `the upload icon`. The app allows the user to change the photo before sending it by clicking `the back button` to go back to the previous screen

Once the image is uploaded to the computing server, a transition screen appears to inform the user that the image is being processed in the computing server. When the server finishes the process successfully, the segmentation and coating color classification results will be returned to the app, otherwise a warning message will be shown.

When the app receives the segmentation and classification results, it demonstrates them on the screen. The user can click a button to read more knowledge and details shown on the screen about the color classification results. Moreover, the app allows users to store and share the segmented tongue images. This function permits users to record the results and to discuss with doctors through other **communication software**. We expect the mobile app in the proposed system to offer a communication interface between users and doctors in TCM for further tongue symptom discussion.
***

## App Implementation
We connect the mobile app with the computing server through the `socket` protocol. 
The implementation of the socket protocol needs to include an **IP address** and a connection **port** for data transmission and reception.
To change the socket settings you can check the `CameraActivity.kt`.
When the server returns the results back to the mobile app, the app can store the segmented image or share it with other communication software by calling an intent.Users can choose to click the **save** or **share** buttons to execute the above-mentioned functions, or they can choose to click the **home** button to enter the home screen of the app.
* * *

## Execution Flow Chart
![](https://github.com/weic0813/TongueMask/blob/main/Flow_Chart.jpg?raw=true)
