package scratches.boot.mongodb.fts.character;

import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * @author Rashidi Zin
 */
public interface CharacterReadOnlyRepository {

    List<Character> findByText(String text, Sort sort);

}
