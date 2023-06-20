package xyz.tgscan.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import xyz.tgscan.domain.RoomDoc;

public interface RoomDocRepository extends ElasticsearchRepository<RoomDoc, Long> {

}