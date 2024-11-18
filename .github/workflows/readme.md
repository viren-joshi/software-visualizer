### Code Coverage
To get the code coverage report of the project locally run this command in the root of the **_repository_**.
```
mvn clean test jacoco:report --file Back-end/pom.xml
```
The output of `JaCoCo` will be stored in `Back-end/target/site/jacoco` . 
<br>
To see the output in a better understandable format go to `Back-end/target/site/jacoco/index.html`
