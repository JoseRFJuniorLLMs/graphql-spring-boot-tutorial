---
title: HowTo: Build GraphQL Services in Java with Spring Boot - Part 1
published: true
description: A tutorial for building a simple GraphQL service in Java with Spring Boot
tags: beginners, graphql, java, springboot
---

Let's start by taking a quick look at the tech involved.


## GraphQL

Compared to REST, GraphQL is the new kid on the block, but I think the improvements are so big that I'm moving any new APIs I write to use it.  I've seen so many APIs that returned huge amounts of data when all the user needed was a set of identifiers to loop through and query a second endpoint for some other data related to those identifiers, which highlights the second issue.  Just writing that sounds mad, I just want to make one call to the API and get everything and only what I need.

I won't go into much detail on why GraphQL is better, there's lots of articles out there that will let you decide that for yourself.  All I will say is, there are two big reasons I've decided to make the switch: the ability to easily include in your request the structure of the data you want to receive; and the ability to combine what would have been multiple REST requests into a single GraphQL query.  Both are huge improvements over REST.  Sure, you could try and write your REST endpoints this way, but then they really wouldn't be REST anymore and GraphQL is built to work this way.

Ok now that is out of the way, let's get on with writing some code.  We're going to build a simple barebones GraphQL API in Java using [Spring Boot](https://projects.spring.io/spring-boot)

## Spring Boot

Coming from a largely PHP background, I've only recently discovered the joy of the Spring framework and Spring Boot in particular.  It makes setting up a new project extremely easy; taking an opinionated view of how to configure and structure a lot of the traditional boilerplate code for controllers, data access, etc, but will get out of the way when you want to configure it how you want.  In this example, we won't need to write any controllers; just our entity model, types and the GraphQL schema.

## Pre-requisites

For this project, we're going to use Java 8, I tried with Java 10 and then 9, but there's an [issue with the lombok dependancy](https://github.com/rzwitserloot/lombok/issues/1572) so had to fallback to 8 for this tutorial.  I'll update it to 10 when that's fixed.  We'll use Spring Boot 2 which uses version 5 of the Spring Framework and sets it all up for you.  For simplicity, we'll also use the Maven build framework for managing our java dependencies.  We'll also use the excellent GraphQL-Java library spring boot starter to give us the GraphQL and GraphIQL endpoints (more on that later).  Finally, I've added Project Lombok which allows you to annotate classes, methods, variables, etc to provide boilerplate functionality.

Here are the exact versions I'll be using:

- [Java 8 (1.8.0_172)](https://java.com)
- [Spring Boot 2.0.2](https://projects.spring.io/spring-boot)
- [GraphQL-Java Spring Boot Starter 4.0.0](https://github.com/graphql-java/graphql-spring-boot)
- [Maven 3.5.2](https://maven.apache.org/)
- [Project Lombok 1.16.20](https://projectlombok.org/)


## Let's Go!

All of the code for this tutorial can be found on [GitHub](https://github.com/sambenskin/graphql-spring-boot-tutorial)

First of all, create a new folder and open it in your chosen IDE.  I'm using [Microsoft Visual Studio Code](https://code.visualstudio.com/).  It really is the best free code editor out there, sorry Atom.

Create a new file called pom.xml and put this inside:

    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>

        <groupId>uk.co.benskin</groupId>
        <artifactId>graphql_spring_boot_tutorial</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <packaging>jar</packaging>

        <name>graphql_spring_boot_tutorial</name>
        <description>Learn how to build a graphql spring boot based java service</description>

        <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>2.0.2.RELEASE</version>
            <relativePath/>
        </parent>

        <properties>
            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
            <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
            <java.version>1.8</java.version>
        </properties>

        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-devtools</artifactId>
            </dependency>
            <dependency>
                <groupId>com.graphql-java</groupId>
                <artifactId>graphql-java-tools</artifactId>
                <version>4.3.0</version>
            </dependency>
            <dependency>
                <groupId>com.graphql-java</groupId>
                <artifactId>graphql-spring-boot-starter</artifactId>
                <version>4.0.0</version>
            </dependency>
            <dependency>
                <groupId>com.graphql-java</groupId>
                <artifactId>graphiql-spring-boot-starter</artifactId>
                <version>4.0.0</version>
            </dependency>
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.16.20</version>
            </dependency>
        </dependencies>
    </project>

The above file defines our project in the first half, so change the project name, description, etc to your own project details.  In the second half, we've defined six dependencies:
- Spring Boot Web - provides the functionality to support our endpoints through web protocols
- Spring Boot DevTools - useful for development building and debugging
- GraphQL-Java Tools - Loads and powers our GraphQL schema
- GraphQL-Java Spring boot starter for GraphQL - hosts our schema at the /graphql endpoint in our spring context
- GraphQL-Java Spring boot starter for GraphIQL - a web based UI for interacting with the /graphql endpoint, with knowledge of the schema at the endpoint
- Project Lombok for reducing boilerplate in our java code

Go ahead and install all the dependencies

    mvn install

After a large list of output from installing the dependencies for the first time, you should see a message that says "BUILD SUCCESS".

Ok, now we've got everything we need to get started.

Create a folder

    src/main/java/uk/co/benskin/graphql_spring_boot_tutorial/

Inside that, create a file

    GraphQLSpringBootTutorialApplication.java

Put the following contents

    package uk.co.benskin.graphql_spring_boot_tutorial;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;

    @SpringBootApplication
    public class GraphQLSpringBootTutorialApplication {

        public static void main(String[] args) {
            SpringApplication.run(GraphQLSpringBootTutorialApplication.class, args);
        }
    }

This loads up a new Spring Applicaton Context integrated automagically with our GraphQL-Java starter dependancies.

Now let's try starting our app and see what we get

    mvn spring-boot:run

You should see a lot of info output and hopefully a few lines saying

    INFO 64612 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
    INFO 64612 --- [  restartedMain] b.g.GraphQLSpringBootTutorialApplication : Started GraphQLSpringBootTutorialApplication in 4.886 seconds (JVM

Now open a browser and go to http://localhost:8080

You should see an error page like this

    Whitelabel Error Page
    This application has no explicit mapping for /error, so you are seeing this as a fallback.

    [DATE]
    There was an unexpected error (type=Not Found, status=404).
    No message available

This is good! the server is running but we've not told how to respond to requests at the root "/", so now try http://localhost:8080/graphiql and you should see a nice UI for interacting with your (yet-to-be-built) endpoint.


## Building the GraphQL Schema

Now onto the fun part, the GraphQL schema.  I won't go into much detail on how GraphQL works, please take a look at GraphQL tutorials for that.

Create this file

    src/main/resources/petshop.graphqls

And put in the contents below

    type Query {
        pets: [Pet]
    }

    type Pet {
        id: Int
        type: Animal
        name: String
        age: Int
    }

    enum Animal {
        DOG
        CAT
        BADGER
        MAMMOTH
    }

Above we've defined the main Query which will return "pets" as an array of Pet types.  The type of the Pet is an enum of type Animal, defined below it.


## Building the API

Moving back to the java code now.  We're going to create a few folders to help us organsie our code.  There's no restriction on what you can call these and where you put them, but I strongly suggest you use subfolders to better organise your code, countless future developers will thank you.

Create this folder

    src/main/java/uk/co/benskin/graphql_spring_boot_tutorial/enums

And Create this file

    src/main/java/uk/co/benskin/graphql_spring_boot_tutorial/enums/Animal.java

And here's the content

    package uk.co.benskin.graphql_spring_boot_tutorial.enums;

    public enum Animal {
        DOG,
        CAT,
        BADGER,
        MAMMOTH
    }

That's defined our enum, now onto the Pet entity model

Create this folder

    src/main/java/uk/co/benskin/graphql_spring_boot_tutorial/entities

Create this file

    src/main/java/uk/co/benskin/graphql_spring_boot_tutorial/entities/Pet.java

And here's the contents

    package uk.co.benskin.graphql_spring_boot_tutorial.entities;

    import lombok.Data;
    import uk.co.benskin.graphql_spring_boot_tutorial.enums.Animal;

    @Data
    public class Pet {
        private long id;

        private String name;

        private Animal type;

        private int age;
    }

This is a simple POJO with an @Data annotation to take care of the boilerplate getters, setters and the constructor.

Now make the directory for the GraphQL resolvers

    src/main/java/uk/co/benskin/graphql-spring-boot-tutorial/resolvers

And create the file

    src/main/java/uk/co/benskin/graphql_spring_boot_tutorial/resolvers/Query.java

And fill it with this

    package uk.co.benskin.graphql_spring_boot_tutorial.resolvers;

    import java.util.ArrayList;
    import java.util.List;
    import com.coxautodev.graphql.tools.GraphQLQueryResolver;
    import org.springframework.stereotype.Component;
    import uk.co.benskin.graphql_spring_boot_tutorial.entities.Pet;
    import uk.co.benskin.graphql_spring_boot_tutorial.enums.Animal;

    @Component
    public class Query implements GraphQLQueryResolver {

        public List<Pet> pets() {
            List<Pet> pets = new ArrayList<>();

            Pet aPet = new Pet();
            aPet.setId(1l);
            aPet.setName("Bill");
            aPet.setAge(9);
            aPet.setType(Animal.MAMMOTH);

            pets.add(aPet);

            return pets;
        }
    }

Ok, that should be everything we need to call our GraphQL endpoint to get a list of pets (one in this case)

Restart your build by stopping maven (ctrl/cmd + c) and starting it again using mvn spring-boot:run

To see it running, go to http://localhost:8080/graphiql and you'll get a nice UI to play around with.  Again I won't go into much detail on how to use the GraphIQL interface.

In the box just below the GraphIQL title, to the right of the History box, is where you enter the request. Copy and paste the below there:

    {
        pets {
            name
            age
            type
        }
    }

Then click the play icon (next to the GraphIQL title).  You should see a reponse like below:

    {
        "data": {
            "pets": [
                {
                    "name": "Bill",
                    "age": 9,
                    "type": "MAMMOTH"
                }
            ]
        }
    }

Congratulations! You've just written a GraphQL service in Java and SpringBoot.  Ok so it's not going to return anything else apart from that one record, so in Part 2 (coming soon) I'll introduce database access through the [Spring Data](https://projects.spring.io/spring-data/) project which utilises the Java Persistence API (JPA)

Thank you very much for reading this article! If you liked it, please comment to let me know or if you have any suggestions for improvements; and make sure to click the heart/unicorn/bookmark buttons below, I always really appreciate it :)