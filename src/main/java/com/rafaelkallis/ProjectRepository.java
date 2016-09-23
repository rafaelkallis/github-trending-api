package com.rafaelkallis;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by rafaelkallis on 22.09.16.
 */
@Repository
public interface ProjectRepository extends CrudRepository<Project, String> {

    Page<Project> findAllByOrderByCommitsDesc(Pageable pageable);
    List<Project> findAll();
}
