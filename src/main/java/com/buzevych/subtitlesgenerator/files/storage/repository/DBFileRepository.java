package com.buzevych.subtitlesgenerator.files.storage.repository;

import com.buzevych.subtitlesgenerator.files.model.DBFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *  Repository to store files that came as a
 */
@Repository
public interface DBFileRepository extends JpaRepository<DBFile, Long> {

}
