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