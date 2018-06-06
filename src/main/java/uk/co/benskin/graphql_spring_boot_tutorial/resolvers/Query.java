package uk.co.benskin.graphql_spring_boot_tutorial.resolvers;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.co.benskin.graphql_spring_boot_tutorial.entities.Pet;
import uk.co.benskin.graphql_spring_boot_tutorial.repositories.PetRepository;

@Component
@RequiredArgsConstructor
public class Query implements GraphQLQueryResolver {

    private final PetRepository PetRepository;

    public Iterable<Pet> pets() {
        return PetRepository.findAll();
    }
}