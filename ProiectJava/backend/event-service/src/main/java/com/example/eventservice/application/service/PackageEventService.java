package com.example.eventservice.application.service;

import com.example.eventservice.domain.model.EventEntity;
import com.example.eventservice.domain.model.PackageEntity;
import com.example.eventservice.domain.model.PackageEventEntity;
import com.example.eventservice.domain.model.PackageEventIdEntity;
import com.example.eventservice.domain.repository.PackageEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class PackageEventService {

    @Autowired
    private PackageEventRepository packageEventRepository;

    public List<PackageEventEntity> getAllRelations() {
        return packageEventRepository.findAll();
    }

    public Optional<PackageEventEntity> getRelation(PackageEntity pachet, EventEntity eveniment) {
        return packageEventRepository.findById(new PackageEventIdEntity(pachet.getId(), eveniment.getId()));
    }

    public List<PackageEventEntity> getPackagesForEvent(EventEntity eveniment) {
        return packageEventRepository.findByEveniment(eveniment);
    }

    public List<PackageEventEntity> getEventsForPackage(PackageEntity pachet) {
        return packageEventRepository.findByPachet(pachet);
    }

    public PackageEventEntity addEventToPackage(PackageEntity pachet, EventEntity eveniment) {
        PackageEventEntity relation = new PackageEventEntity(pachet, eveniment);
        return packageEventRepository.save(relation);
    }


    public void removeEventFromPackage(PackageEntity pachet, EventEntity eveniment) {
        packageEventRepository.deleteByPachetAndEveniment(pachet, eveniment);
    }
}
