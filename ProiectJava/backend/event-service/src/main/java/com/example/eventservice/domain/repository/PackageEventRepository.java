package com.example.eventservice.domain.repository;

import com.example.eventservice.domain.model.EventEntity;
import com.example.eventservice.domain.model.PackageEntity;
import com.example.eventservice.domain.model.PackageEventEntity;
import com.example.eventservice.domain.model.PackageEventIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageEventRepository extends JpaRepository<PackageEventEntity, PackageEventIdEntity>
{
    List<PackageEventEntity> findByPachet(PackageEntity pachet);
    List<PackageEventEntity> findByEveniment(EventEntity eveniment);
    void deleteByPachet(PackageEntity pachet);
    void deleteByEveniment(EventEntity eveniment);
    void deleteByPachetAndEveniment(PackageEntity pachet, EventEntity eveniment);
}
