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