package com.sovereingschool.back_streaming.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_streaming.Models.Preset;

@Repository
public interface PresetRepository extends MongoRepository<Preset, String> {

}
