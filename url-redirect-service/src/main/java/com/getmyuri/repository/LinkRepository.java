package com.getmyuri.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.getmyuri.model.DataObjectFormat;

@Repository
public interface LinkRepository extends MongoRepository<DataObjectFormat, String> {
    Optional<DataObjectFormat> findByAlias(String alias);

    List<DataObjectFormat> findAll();

    boolean existsById(String id);

    void deleteById(String id);
}