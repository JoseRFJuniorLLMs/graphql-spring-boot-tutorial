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