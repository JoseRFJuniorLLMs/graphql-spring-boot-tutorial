
package uk.co.benskin.graphql_spring_boot_tutorial.repositories;
    
import org.springframework.data.repository.CrudRepository;
import uk.co.benskin.graphql_spring_boot_tutorial.entities.Pet;
    
public interface PetRepository extends CrudRepository<Pet, Long> {}