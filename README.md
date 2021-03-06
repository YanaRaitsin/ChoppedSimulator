# ChoppedSimulator
Machine learning based simulator that recommends recipes depending on the user's preferences. <br />
The user must register to the simulation and choose his preferences and allergies for the recommendation: <br />
![Registration](1.png) <br />
Based on the data that the user entered (food preferences, allergies, etc), the simulation builds recipes for different types of users (for example: people who have allergies to certain ingredients, diabetes, or personal preferences). <br />
The user must login with the email and the password that he register with: <br />
![Login](2.png) <br />
If the user forgot his password, he can reset it by pressing "Forgot Password?". <br />
Atfer the user entered his email and password, the simulation will check if the user exsits in the system. <br />
If the user exists, he will enter to the main menu of the simulation: <br />
![main](3.png) <br />
Start button - will enter the user to recipe selection so he can start the preparation. <br />
Profile button - will show to the user all the recipes that he prepared in the simulation. <br />
Settings - the user can turn off or on vSync and see the controls guide of the simulation. <br />
Exit - will exit the simulation. <br />
Continue - if the simulation stopped working because of an error, the user can continue the preparation of the recipe where he stopped. <br />
Start: first, the user need to select from the recipes that the simulationtha recommended based on the user's data: <br />
![selection](4.png) <br />
During the preparation of the recipe, the user can control the portion of each ingredient. In order to receive a full score, the user must select the amount of ingredients according to the instructions of the recipe that he selected. The user is measured on time according to the type of the recipe he chose to prepare. <br />
![preparation](5.png) <br />
After the user done preparing the recipe, he will receive a score: <br />
![score](6.png) <br />
Profile: will show the history of all the recipes that the user selected: <br />
![profile](7.png) <br />
Settings: the settings and the conrtols of the simulation: <br />
![settings](8.png) <br />
<br />
Chopped Simulator was developed on Java using JDBC API, LWJGL (OpenGL) and Decision Tree ID3 algorithm. <br />

