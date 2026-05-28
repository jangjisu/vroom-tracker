package com.vroomtracker.service;

import com.vroomtracker.domain.RestStopEntity;
import com.vroomtracker.repository.RestStopRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestStopQueryService {

    private final RestStopRepository restStopRepository;

    @Transactional(readOnly = true)
    public List<RestStopEntity> findAll() {
        return restStopRepository.findAll();
    }
}
