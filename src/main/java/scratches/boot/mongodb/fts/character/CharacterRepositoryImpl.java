package scratches.boot.mongodb.fts.character;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition.TextIndexDefinitionBuilder;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;

import java.util.List;

/**
 * @author Rashidi Zin
 */
@AllArgsConstructor
public class CharacterRepositoryImpl implements CharacterReadOnlyRepository {

    private final MongoTemplate template;

    @Override
    public List<Character> findByText(String text, Sort sort) {
        template.indexOps(Character.class)
                .ensureIndex(new TextIndexDefinitionBuilder().onFields("name", "publisher").build());

        var parameters = text.split(" ");
        var query = TextQuery.queryText(new TextCriteria().matchingAny(parameters)).with(sort);

        return template.find(query, Character.class);
    }

}
