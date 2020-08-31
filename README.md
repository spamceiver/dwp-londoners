# dwp-londoners
The purpose of this project is to implement an API that returns people who are listed as either living in London, or
 whose current coordinates are within 50 miles of London.

The solution was implemented in Spring Boot. It can be deployed by following these steps:
1. Clone this github repository: `git clone https://github.com/nikosmarinos/dwp-londoners.git`.
2. Go to your local folder: `cd dwp-londoners/`.
3. Creat the executable JAR: `mvn package`.
4. Run the JAR:`java -jar target/dwp-londoners-api-1.0.jar`. 

It was developed with JDK11 but it is compatible with any JDK from 1.8 upwards.     

The HTTP GET method can be called by http://localhost:8080/dwp/users. The optional parameter of distance can
 be provided like this: http://localhost:8080/dwp/users?distance=40. The geographic coordinates and name of the city are
 configurable through the application.properties file and they default to London.
 
 This API is based on the invocation of two other APIs as per the instructions of this exercise. We assume that in case of 
 failure of either of these 2 APIs, our users will get a 500 error message. For reasons of generality, we haven't created a 
 POJO to host the APIs response. Instead, we use a general `List<Map<String, Object>>` structure where we get the attribute
 we need by name (e.g. `user.get("latitude")`). We noticed that the longitude/latitude attributes come either as Doubles 
 or Strings, so the code expects either.
 
   