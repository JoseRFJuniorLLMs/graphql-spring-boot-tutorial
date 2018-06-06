---
title: HowTo: Integrate a MySQL Database into your Java Spring Boot GraphQL Service
published: false
description: A tutorial for integrating a MySQL database into a simple GraphQL service in Java Spring Boot using the Spring Data JPA
tags: beginners, graphql, java, mysql
---

In my previous article, [HowTo: Build GraphQL Services in Java with Spring Boot](https://dev.to/sambenskin/howto-build-graphql-services-in-java-with-spring-boot---part-1-38b2), we learnt how to get started with Java Spring Boot and the GraphQL-Tools library.  We built a simple endpoint that returned a hard-coded single element array.  A great start with only a few files, but it doesn't do very much, so let's add a database.  

If you haven't read the first article, I would suggest you do as it sets the basis for this tutorial.  If you'd prefer, you can go to the "master" branch on the [GitHub repo](https://github.com/sambenskin/graphql-spring-boot-tutorial/tree/master) and download the code to start where I left off in my previous article.  There's also a ["part2" branch](https://github.com/sambenskin/graphql-spring-boot-tutorial/tree/part2) that has all the code completed for this tutorial if you want to read and run it.  I'd suggest going through this tutorial and writing the code yourself as it's a more effective way for most people to learn by doing.

We're going to the [Spring Data JPA](https://projects.spring.io/spring-data-jpa) starter which uses the [Java Persistence API](https://en.wikipedia.org/wiki/Java_Persistence_API) to save and retrieve your entity models to/from your chosen database.  This API gives you an easy to use abstraction layer so you can use any of a large number of different databases.  We're going to use [MySQL](https://www.mysql.com/) as it's the most popular database out there.


## Pre-requisites

For this guide, you'll need to have MySQL server and client installed.  You'll need a privileged user that can create a schema and users.  If you don't have this, you need to contact your database administrator to do the first three steps. 


## Security

Please don't use root in your app, it's a horrendous security issue.  We're going to create a non-priviledged user to use with your app.  

Don't try to commit these credentials to git because again this is a security risk.  I've purposefully added the file to the .gitignore so you don't accidentally do this.

When you want to deploy to another server, you need to manually put the file there.  Better still, invest time in understanding and creating an automated build pipeline.

You should never use your production database instance for development.  Always have another instance locally on your machine. 

Finally, use different credentials for your local and your production environment.



## MySQL Setup


First of all, as root or preferably an admin user, login to mysql on the command line:

    mysql -uYOUR_PRIVILEGED_USER -p

It will then ask for your password.  Once you're in to the MySQL command line, create the schema:

    CREATE SCHEMA `graphql_tutorial` DEFAULT COLLATE=`utf8_bin` DEFAULT CHARACTER SET=`utf8`;

And then create a user with access to that schema:

    GRANT ALL PRIVILEGES ON `graphql_tutorial`.* TO `graphql_tutorial_user`@`localhost` IDENTIFIED BY 'CHANGE_ME_TO_SOMETHING_SECURE';


Finally, create the table we need:

    CREATE TABLE `pets` (`id` INT(11) NOT NULL AUTO_INCREMENT, `name` VARCHAR(255) DEFAULT NULL, `age` TINYINT(3) DEFAULT NULL, `type` ENUM('DOG','CAT','BADGER','MAMMOTH'), PRIMARY KEY(`id`));

Let's create a few entries in the table so we've got something to retrieve:

    INSERT INTO `pets` (`name`,`age`,`type`) VALUES ('Steve', 5, 'BADGER'), ('Jeff', 88, 'MAMMOTH'), ('Oscar', 2, 'CAT');

Now let's check everything is in there:

    SELECT * FROM pets;

This should show the following result:

    +----+-------+------+---------+
    | id | name  | age  | type    |
    +----+-------+------+---------+
    |  1 | Steve |    5 | BADGER  |
    |  2 | Jeff  |   88 | MAMMOTH |
    |  3 | Oscar |    2 | CAT     |
    +----+-------+------+---------+
    3 rows in set (0.00 sec)

Great, that's all the MySQL setup done! Type "exit" and press enter to get out of MySQL and back to the command line.


## Spring Data JPA

Now we need to add the Spring Data JPA project to our app.  Add these lines in your pom.xml in the `<dependencies>` section:

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
        
Then run the install:

    mvn install

Now we need to tell our app how to connect to our database.  If you've checked-out the github repo, copy the application.properties.example file to application.properties and add in your own URL and credentials.  If you haven't, create this file:

    src/main/resources/application.properties

And put in this contents

    spring.datasource.url=jdbc:mysql://localhost:3306/graphql_tutorial
    spring.datasource.username=graphql_tutorial_user
    spring.datasource.password=ThePasswordYouCreatedAbove

This sets up our connection URL, the username and the password for Spring Data to connect to your database instance.

Now create this folder to store our repository:

    src/main/java/uk/co/benskin/graphql_spring_boot_tutorial/repositories

The repository acts as our interface between the pet model instance and the database table.  I'm following the convention of pluralising the database table name that relates to the entity model.  So "pets" table for the "Pet" entity model.  We'll need to define that in the Entity as the default is just the table name "pet".

Create this file

    
src/main/java/uk/co/benskin/graphql_spring_boot_tutorial/repositories/PetRepository.java

Then add this contents:

    package uk.co.benskin.graphql_spring_boot_tutorial.repositories;
    
    import org.springframework.data.repository.CrudRepository;
    import uk.co.benskin.graphql_spring_boot_tutorial.entities.Pet;
    
    public interface PetRepository extends CrudRepository<Pet, Long> {}

If you've never seen the Spring Data JPA classes before, you'll probably be thinking "Is that it?" and yes, that's all you need to create, read, update and delete your entities in a database.  Gone is all that boilerplate code you've had to write in the past, brilliant eh?!

Now go back to our Pet entity model and add the @Entity, @Table, @Id, @GeneratedValue and @Enumerated annotations as below, as well as their imports.  You're file should look like the below:

    package uk.co.benskin.graphql_spring_boot_tutorial.entities;

    import javax.persistence.Entity;
    import javax.persistence.EnumType;
    import javax.persistence.Enumerated;
    import javax.persistence.GeneratedValue;
    import javax.persistence.GenerationType;
    import javax.persistence.Id;
    import javax.persistence.Table;
    
    import lombok.Data;
    import uk.co.benskin.graphql_spring_boot_tutorial.enums.Animal;
    
    @Data
    @Entity
    @Table(name="pets")
    public class Pet {
        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private long id;
    
        private String name;
        
        @Enumerated(EnumType.STRING)
        private Animal type;
    
        private int age;
    }

Great! Those annotations tell Spring Data JPA that it can persist this entity to the database.  We need to tell spring data that our enum is a string otherwise it will default to thinking it's an integer.

Finally we need to update our GraphQL Query resolver in Query.java from a hard-coded array to fetching all pets from the repository.  Replace the contents of the file with the following:

    package uk.co.benskin.graphql_spring_boot_tutorial.resolvers;
    
    import com.coxautodev.graphql.tools.GraphQLQueryResolver;
    import org.springframework.stereotype.Component;
    import uk.co.benskin.graphql_spring_boot_tutorial.entities.Pet;
    import uk.co.benskin.graphql_spring_boot_tutorial.repositories.PetRepository;
    
    @Component
    public class Query implements GraphQLQueryResolver {
    
        private PetRepository PetRepository;
        
        public Query(PetRepository PetRepository) {
            this.PetRepository = PetRepository;
        }
        
        public Iterable<Pet> pets() {
            return PetRepository.findAll();
        }
    }

That's it! Now start up your app again:

    mvn spring-boot:start

Navigate to [http://localhost:8080/graphiql](http://localhost:8080/graphiql) and you should again see the GraphIQL UI.  

Run this query:

    {
	pets {
            name,
            age,
            type
	    }
    }

You should see this result:

    {
      "data": {
        "pets": [
          {
            "name": "Steve",
            "age": 5,
            "type": "BADGER"
          },
          {
            "name": "Jeff",
            "age": 88,
            "type": "MAMMOTH"
          },
          {
            "name": "Oscar",
            "age": 2,
            "type": "CAT"
          }
        ]
      }
    }

Congratulations, you're now reading all your data from the database!

Thank you very much for reading this article! If you enjoyed it, please comment to let me know or if you have any suggestions for improvements.  Please click the heart/unicorn/bookmark buttons below, I always really appreciate it :)