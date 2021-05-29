package scratches.boot.mongodb.fts.character;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author Rashidi Zin
 */
public interface CharacterRepository extends MongoRepository<Character, String>, CharacterReadOnlyRepository {

    List<Character> findAllBy(TextCriteria criteria, Sort sort);

}
