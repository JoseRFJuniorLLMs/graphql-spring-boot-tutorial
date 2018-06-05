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