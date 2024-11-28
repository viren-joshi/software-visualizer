# Software Dependency Visualizer

Software Dependency Visualizer is a tool aimed at identifying the class level dependency information of JARs with the help of inheritance, implementation and composition. It provides this in an easily visualized manner via flash and graphical based navigations allowing users to visualize the structure of their software projects. Further, the tool can help in project management by enabling its users to store their dependency analyses and then load them for further evaluation and modification.

---

1. [**Setup Configurations**](#setup-configurations)

    [1.1 Dependencies](#dependencies)
    
    [1.2 Build instructions](#build_instructions)
    
    [1.3 Usage Scenario](#usage_scenario)
    
2. [**CI/CD**](#ci/cd)

    [2.1 Build](#build)

    [2.2 Test](#test1)
    
    [2.3 Code Quality](#code_quality)
    
    [2.4 Code Coverage](#code_coverage)
    
3. [**Test**](#test)

    [3.1 Coverage](#coverage)

    [3.2 Integration tests](#integration_test)
    
    [3.3 Test best practices](#test_best)

    [3.4 TDD adherence](#tdd_adherence)
    
4. [**Quality**](#quality)

    [4.1 Design principles](#design_principles)
    
    [4.1 Design smells](#design_smells)

5. [**Webapp Features**](#feat)



# Setup Configurations

## 1.1 Dependencies <a name = "dependencies"></a>

This section shows the external dependencies used in the front-end and the back-end part of the project. These dependencies were imported to use the functionality provided by them in our project.

### FrontEnd Dependencies

<p>• <strong>@emotion/react</strong>: ^11.13.3 - Library for CSS-in-JS styling, offering great performance and flexibility.</p>  
<p>• <strong>@emotion/styled</strong>: ^11.13.0 - Styled components library built on Emotion for CSS-in-JS design.</p>  
<p>• <strong>@mui/icons-material</strong>: ^6.1.6 - Material Design icons for use with Material-UI.</p>  
<p>• <strong>@mui/material</strong>: ^6.1.3 - Core Material-UI library for building robust React UI components.</p>  
<p>• <strong>@testing-library/jest-dom</strong>: ^5.17.0 - Provides custom Jest matchers for testing DOM nodes.</p>  
<p>• <strong>@testing-library/react</strong>: ^13.4.0 - Utilities for testing React components.</p>  
<p>• <strong>@testing-library/user-event</strong>: ^13.5.0 - Simulates user interactions with React components in tests.</p>  
<p>• <strong>@types/jest</strong>: ^27.5.2 - Type definitions for the Jest testing framework.</p>  
<p>• <strong>@types/node</strong>: ^16.18.112 - TypeScript definitions for Node.js modules.</p>  
<p>• <strong>@types/react</strong>: ^18.3.11 - Type definitions for the React library.</p>  
<p>• <strong>@types/react-dom</strong>: ^18.3.0 - Type definitions for the ReactDOM library.</p>  
<p>• <strong>@xyflow/react</strong>: ^12.3.2 - A graph visualization library for React applications.</p>  
<p>• <strong>axios</strong>: ^1.7.7 - Promise-based HTTP client for making API requests.</p>  
<p>• <strong>dagre</strong>: ^0.8.5 - JavaScript library for graph layout in directed acyclic graphs.</p>  
<p>• <strong>dotenv</strong>: ^16.4.5 - Loads environment variables from a `.env` file into `process.env`.</p>  
<p>• <strong>firebase</strong>: ^11.0.1 - SDK for integrating Firebase services like Firestore, Auth, and more.</p>  
<p>• <strong>front-end</strong>: file: - Placeholder for linking a local frontend package.</p>  
<p>• <strong>jszip</strong>: ^3.10.1 - JavaScript library for creating, reading, and editing ZIP files.</p>  
<p>• <strong>react</strong>: ^18.3.1 - Core library for building user interfaces.</p>  
<p>• <strong>react-d3-tree</strong>: ^3.6.2 - React component for rendering tree diagrams using D3.js.</p>  
<p>• <strong>react-dom</strong>: ^18.3.1 - Provides DOM-specific methods for React components.</p>  
<p>• <strong>react-router-dom</strong>: ^6.27.0 - Declarative routing for React web applications.</p>  
<p>• <strong>react-scripts</strong>: 5.0.1 - Configuration and scripts for Create React App.</p>  
<p>• <strong>typescript</strong>: ^4.9.5 - A superset of JavaScript with static typing.</p>  
<p>• <strong>web-vitals</strong>: ^2.1.4 - Library for measuring Core Web Vitals metrics.</p>  


### Backend Dependencies

<p>•	<strong>gson</strong>: A Java serialization/deserialization library to convert Java Objects into JSON and back.</p>
<p>•	<strong>firebase-admin</strong>: A Firebase client library that provides tools and services to integrate Firebase functionalities such as authentication, and real-time database operations</p>
<p>•	<strong>spring-boot-starter-web</strong>: Provides the Spring MVC library and its dependencies to support the development of web applications</p>
<p>•	<strong>dotenv-java</strong>: A library that loads environment variables from a `.env` file into the application's runtime environment, enabling secure and manageable configuration handling</p>
<p>•	<strong>asm</strong>: A Java library used for reading, writing, and transforming Java bytecode, providing tools for low-level class manipulation and analysis. Useful to extract alkk the information about classes and its members</p>
<p>•	<strong>junit</strong>: A widely used testing framework for Java that provides annotations and assertions to write and run repeatable unit tests efficiently.</p>
<p>•	<strong>maven-model</strong>: A library that represents the structure of a Maven POM (Project Object Model) file, allowing developers to programmatically interact with and manipulate Maven project configurations.</p>
<p>•	<strong>lombok</strong>: A Java library that reduces boilerplate code by automatically generating common methods like getters, setters, constructors, and more through annotations.</p>
<p>•	<strong>maven-failsafe-plugin</strong>: A Maven plugin designed to execute integration tests in a build lifecycle, ensuring the stability and reliability of the application during the testing phase</p>
<p>•	<strong>spring-boot-starter</strong>: A core starter that provides default configurations and essential dependencies for building Spring Boot applications, simplifying the setup process.</p>
<p>•	<strong>spring-boot-starter-test</strong>: A starter that includes dependencies for testing Spring Boot applications, providing tools like JUnit, Mockito, and Spring Test for unit and integration testing.</p>

### Prerequisites
<b>Install the following:</b> 
- Java 17
- Maven 
- Node.js 

<b>After installing the above</b>
- Clone the repository: Start by cloning the repository containing the React application to your local machine. You can use Git to clone the repository by running the following command in your terminal:
`git clone https://github.com/CSCI5308/course-project-g08.git` (Branch : main)


## 1.2 Build/Deployment instructions <a name = "build_instructions"></a>

We have deployed the front-end and the back-end on the university servers. 

To build and run the back-end (in local system):

1. Go to the `Back-end` folder of the project.
2. Create a `.env` file in the root folder of the project (course-project-g08).
3. Add `GOOGLE_APPLICATION_CREDENTIALS=/src/main/java/com/g8/configuration/firebase-config.json` in the newly created `.env` file.
4. Now, go to the `configuration` package and add this file in it. [Link to the file](https://drive.google.com/file/d/1d8Reh5-Y_Hq6X8X6gqvFRli96WbsaOBs/view?usp=sharing).
5. Next, go to the `FirebaseConfig` class and change replace the statement `String credentialsPath...` to`String credentialsPath = dotenv.get("GOOGLE_APPLICATION_CREDENTIALS");`
6. You should be good to go now. Open the terminal and go to the Back-end folder and type this command to run the back-end on localhost.
`mvn spring-boot:run`

To build and run the front-end (in local system):

1. Go to the `front-end` folder.
2. Open the terminal at the folder and type the following command: `npm install` to install required dependencies.
3. Now, go to the `.env` file of the project and change the `REACT_APP_SERVER_URL` to `http://localhost:8080`.
4. You're good to go now! Execute `npm start` command to run the front-end on the local-host.

To run the project from remote:

1. Go to `http://csci5308-vm8.research.cs.dal.ca`.

## 1.3 Usage Scenario <a name = "usage_scenario"></a>

<p>1)	The users can register on Sign Up page and Sign In to view their saved projects or upload new projects.</p>
<p>2)	Once the user is registered, they will be directed towards the upload file page. If the user Signs In, they will be given an option to load existing projects or upload a new project.</p>
<p>3)	Once the chosen project is uploaded the user will be able to see the list of classes and the relationship they share in a graph view.</p>
<p>4)	The user can choose to see only inheritance, implementation, or composition dependency using the buttons on the screen.</p>
<p>5)	The user can see the information related to a particular class by clicking on the desired class in the internal dependency section.</p>
<p>6)	Users can make the changes in the shown graph to visualize the changes before they implement them in their code.</p>

# 2. CI/CD <a name = "ci/cd"></a>

## 2.1 Build <a name = "build"></a>

The build process for the application is managed using Maven. By running the `mvn clean package` command, the project is built, and the required dependencies are resolved, followed by generating the final application artifacts.

![Screenshot 2024-11-28 at 4.13.47 PM](https://hackmd.io/_uploads/H1n-1LUQJx.png)

## 2.2 Test <a name = "test1"></a>

We utilize JUnit, a widely used open-source testing framework for Java, to ensure the quality of our application. JUnit tests are integrated into our CI pipeline by executing the mvn test command, which leverages the Maven build tool to run the tests.

![Screenshot 2024-11-28 at 4.10.33 PM](https://hackmd.io/_uploads/HyjpASIQ1x.png)

## 2.3 Code Quality <a name = "code_quality"></a>

The Continuous Integration (CI) Pipeline of the application includes a stage for code quality assurance. 

The backend code quality is assessed using [DesigniteJava](https://www.designite-tools.com/products-dj), a code smell detection tool that generates reports highlighting potential issues in the code. These reports are stored on the server and can be downloaded later for detailed analysis.

![Screenshot 2024-11-28 at 4.00.35 PM](https://hackmd.io/_uploads/r1hO2HImyg.png)

## 2.4 Code Coverage <a name = "code_coverage"></a>

The Continuous Integration (CI) Pipeline of the application includes a stage for code quality assurance. 

The backend code coverage is meaasured using [Jacoco](https://www.eclemma.org/jacoco/), a code coverage library for Java. The code coverage report generated by Jacoco is also stored in the server which can be accessed reotely.

![Screenshot 2024-11-28 at 4.05.45 PM](https://hackmd.io/_uploads/rJ0X6S8mke.png)

# 3. Test <a name = "test"></a>

## 3.1 Coverage <a name = "coverage"></a>
Jacoco is used to show code coverage of the test cases. The project's service layer has 87% Line Coverage, and 92% method coverage.
![image](https://hackmd.io/_uploads/rJkz_uIXJl.png)
Figure 1: Code coverage is averaged at 90% (Service, Controller, and Utils)

## 3.2 Integration tests <a name = "integration_test"></a>
We have performed integration test for service layer. We tested that the database operations work correctly and the dependencies are identified for a given project.
## 3.3 Test best practices <a name = "test_best"></a>
We have followed best practices for mocking the dependent class when required. The tests also run in isolation from eachother (unit tests).
## 3.4 TDD adherence <a name = "tdd_adherance"></a> 
We followed the Test Driven Development (TDD) for most of the code we wrote after the midterm.

```bash 
TDD Commit                               - Code Implementation Commit 
____________________________________________________________________________________

7df05af680905a5df4d6ecb412ab9cee55b38c0c - 7aceae964d3dce30cb8e5a0bf154c6e827be0567
8002d0a2f8eda991c717d098aed6a23f53e41ae2 - d355d6c5c735f3472f1d60e191286a06ecd4ba59
80ff72c01b8e6481a849ebef007336abde0d0c54 - 7bd5d6fa23bfc770445a71391b7ab997841ec51a
9bd18f5566355d525593dcd53d7a3d515397213b - 606a5923b9a745c5d56c50bc26c14cb8240ac4ff
c5def1ae2ae92da6def0b9833c2d01a000c8672b - 4534ed75262d18f97e20f16a562ae78261ba5106
2dd5374c73db78df97395f788548eab3624e253f - 8cd8912eebfd5fb27403404e8b5be487d79d5981
a8418f3bb7dbb719a5acd439c9a760a3694b8ae4 - b3b757d5f41bccfdfe7e77b3e4d7028e7835ceae
1247874ed2e3c63fcc11e5314b65f0e229eebc63 - 59d4b617dfffd947af9a74b8e17d1ab6bad2e594
3e1a087b8b56186697ebed8b4d977e285677e3d6 - 9db66136bc1a6d3353ea01fe096da2df6166c29e
a589a5dcb9846e5d1ce7018faf58179dd9440085 - d17d282a91383d8525074018282055ca27dbfbcd
c21904fe62b812ebda8f8983d1ca42b5209dbecc - 344e8214e4e219548f437d0e027e9755daf72393
8d968a7e67c389f4fe7572092c7e37965093a7e5 - 499a031589cb49aad2a64370ca4d16eddb832521
b86927de296d1da6575cd19b0c08a9c0d64fb5fb - c316dfe6dc5aeb892cf68b868ab0ac6d9da783bf
```

# 4. Quality <a name = "quality"></a>

## 4.1 Design principles <a name = "design_principles"></a>

<h4>Single Responsibility Principle</h4>

<p>The Single Responsibility Principle states that a class should have only one responsibility, encouraging the separation of concerns and making the code easier to modify, test, and reuse. We followed this principle by creating dedicated controllers and services for different stakeholders, ensuring clear and focused functionality.</p>

![Screenshot 2024-11-28 at 4.44.42 PM](https://hackmd.io/_uploads/rJ0SULImkx.png)
Figure 1: Different classes for different functionalities

![Screenshot 2024-11-28 at 4.52.43 PM](https://hackmd.io/_uploads/r1aruU87yl.png)
Figure 2: Upload controller which has file related methods

<h4>Open/Closed Principle (OCP)</h4>

<p>The principle "A class should be open for extension but closed for modification" means that a class's functionality can be enhanced without altering its existing source code. We don't have any classes in our project that needs to be extended because there is no use of variations in our project. 
    
<h4>Dependency Inversion Principle (DIP)</h4>

<p>To enhance modularity and extensibility, this principle advocates relying on abstractions like interfaces and abstract classes, minimizing dependencies on specific implementations and decoupling components. In our application, we implement numerous interfaces and abstract classes and leverage Spring Boot's dependency injection mechanism to manage components as beans. This allows us to further reduce tight coupling and ensure a scalable design. Since Spring Boot inherently adheres to SOLID principles, it simplifies the implementation of this principle in our architecture.</p>

Our project's requirement is such that it does not need interfaces to be extended by multiple classes

## 4.2 Design Smells <a name = "design_smells"></a>

1. Architecture Smells
![Screenshot 2024-11-28 at 7.22.59 PM](https://hackmd.io/_uploads/r1gviuLQye.png)
Figure 1: Architecture code smells - extracted from Designite
2. Design Smells
![Screenshot 2024-11-28 at 7.24.29 PM](https://hackmd.io/_uploads/BJO2sOL71x.png)
Figure 2: Design code smells and their status - extracted from Designite
3. Implementation Smells
![Screenshot 2024-11-28 at 7.12.38 PM](https://hackmd.io/_uploads/BJmlF_Lmkl.png)
Figure 3: Implementation code smells and their status - extracted from Designite

# 5. Webapp Features <a name = "feat"></a>

## 5.1 Upload .jar file
![Screenshot 2024-11-28 at 6.06.16 PM](https://hackmd.io/_uploads/HJuDFD8XJx.png)
Figure 1: User can upload the .jar file of their SpringBoot project 

## 5.2 Class List and Dependency Graph View
![Screenshot 2024-11-28 at 6.09.33 PM](https://hackmd.io/_uploads/Byj79DL7kg.png)
Figure 2: User can view and search from list of classes. The classes are shown in an interactive-graphical format on the right side. 

## 5.3 Dependency Filter
![Screenshot 2024-11-28 at 6.17.57 PM](https://hackmd.io/_uploads/ryQQhwL7ye.png)
Figure 3: User can filter the nodes and edges according to the type of dependency.

## 5.4 Class definitions
![Screenshot 2024-11-28 at 6.20.13 PM](https://hackmd.io/_uploads/S1cj3wLm1x.png)
Figure 4: User can see methods and variables for each class.

## 5.5 Custom Class View
![Screenshot 2024-11-28 at 6.24.37 PM](https://hackmd.io/_uploads/SJW36P8myl.png)
Figure 5: User can create a custom view of nodes and edges for better understanding of their class relationships.

## 5.6 User's Saved Projects 
![Screenshot 2024-11-28 at 6.42.10 PM](https://hackmd.io/_uploads/BJlCWOUQye.png)
Figure 6: User can see the saved projects after clicking the 'save view'

