package com.elpuntazo.backend.repository;

import com.elpuntazo.backend.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("SELECT c FROM Client c WHERE c.active = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR c.identification LIKE CONCAT('%', :search, '%'))")
    Page<Client> search(@Param("search") String search, Pageable pageable);

    boolean existsByIdentificationAndIdentificationIsNotNull(String identification);
}
