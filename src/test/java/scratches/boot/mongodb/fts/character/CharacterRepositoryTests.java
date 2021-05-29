package scratches.boot.mongodb.fts.character;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition.TextIndexDefinitionBuilder;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rashidi Zin
 */
@DataMongoTest
@Testcontainers
class CharacterRepositoryTests {

    @Container
    static MongoDBContainer CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", CONTAINER::getReplicaSetUrl);
    }

    @Autowired
    private MongoTemplate template;

    @Autowired
    private CharacterRepository repository;

    @AfterEach
    void dropCollection() {
        template.dropCollection(Character.class);
    }

    @Test
    @DisplayName("Generated query: Search for 'captain marvel' should return 'Captain Marvel' and 'Thanos'")
    void findAllBy() {
        // Simulate predefined index
        template.indexOps(Character.class).ensureIndex(new TextIndexDefinitionBuilder().onFields("name", "publisher").build());

        repository.insert(List.of(
                new Character("Captain Marvel", "Marvel"),
                new Character("Joker", "DC"),
                new Character("Thanos", "Marvel")
        ));

        var characters = repository.findAllBy(new TextCriteria().matchingAny("captain marvel"), Sort.by("name"));

        assertThat(characters)
                .hasSize(2)
                .extracting("name")
                .containsOnly("Captain Marvel", "Thanos")
                .doesNotContain("Joker");
    }

    @Test
    @DisplayName("Custom implementation: Search for 'captain marvel' should return 'Captain Marvel' and 'Thanos'")
    void findByText() {
        repository.insert(List.of(
                new Character("Captain Marvel", "Marvel"),
                new Character("Joker", "DC"),
                new Character("Thanos", "Marvel")
        ));

        var characters = repository.findByText("captain marvel", Sort.by("name"));

        assertThat(characters)
                .hasSize(2)
                .extracting("name")
                .containsOnly("Captain Marvel", "Thanos")
                .doesNotContain("Joker");
    }

}
