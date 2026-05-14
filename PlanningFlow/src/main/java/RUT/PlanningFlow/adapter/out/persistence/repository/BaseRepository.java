package RUT.PlanningFlow.adapter.out.persistence.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<E, ID> extends Repository<E, ID> {
    E save(E entity);
    Optional<E> findById(ID id);
    List<E> findAll();
    void delete(E entity);
}