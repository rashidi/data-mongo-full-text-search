# Spring Data MongoDB: Full Text Search
Implement [MongoDB Full Text Search](https://docs.mongodb.com/manual/text-search/) with [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb).

## Background
MongoDB full text search provides the flexibility to perform search entries through multiple fields. In this example 
we will explore how to implement full text search with Spring Data MongoDB.

## Verification
Given we have the following entries in `Character`

|       Name       |  Publisher  |
|------------------|-------------|
|  Captain Marvel  |    Marvel   |
|     Joker        |      DC     |
|     Thanos       |    Marvel   |

When searching for `captain marvel` then the following results should be returned

|       Name       |  Publisher  |
|------------------|-------------|
|  Captain Marvel  |    Marvel   |
|     Thanos       |    Marvel   |

This is demonstrated in [CharacterRepositoryTests.java](src/test/java/scratches/boot/mongodb/fts/character/CharacterRepositoryTests.java).

## Implementation

### Document
We will start by defining [Character class](src/main/java/scratches/boot/mongodb/fts/character/Character.java)

```java
@Data
@Document
public class Character {

    @Id
    private String id;

    @NonNull
    @TextIndexed
    private String name;

    @NonNull
    @TextIndexed
    private String publisher;

}
```

### With Predefined Index
If respective fields are already `indexed` then we can utilise Spring Data query generation.

We will proceed to create `findAllBy` in [CharacterRepository.java](src/main/java/scratches/boot/mongodb/fts/character/CharacterRepository.java) which
will be responsible to search `indexed` fields; i.e. `Character.name` and `Character.publisher`

```java
public interface CharacterRepository extends MongoRepository<Character, String> {

    List<Character> findAllBy(TextCriteria criteria, Sort sort);

}
```

This implementation then can be used in the following manner:

```java
@DataMongoTest
class CharacterRepositoryTests {

    @Autowired
    private CharacterRepository repository;

    @Test
    @DisplayName("Generated query: Search for 'captain marvel' should return 'Captain Marvel' and 'Thanos'")
    void findAllBy() {
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

}
```

### Without Predefined Index
Without predefined index, we will need to implement a custom repository implementation. We will start by defining a 
custom repository interface, [CharacterReadOnlyRepository.java](src/main/java/scratches/boot/mongodb/fts/character/CharacterReadOnlyRepository.java)

```java
public interface CharacterReadOnlyRepository {

    List<Character> findByText(String text, Sort sort);

}
```

Next is to inform `CharacterRepository` about methods provided by `CharacterReadOnlyRepository`

```java
public interface CharacterRepository extends MongoRepository<Character, String>, CharacterReadOnlyRepository {
    
}
```

Next, implement search mechanism that will allow us to search for entries by text in `Character` in 
[CharacterRepositoryImpl.java](src/main/java/scratches/boot/mongodb/fts/character/CharacterRepositoryImpl.java)

```java
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
```

This implementation will `indexed` searchable fields, i.e. `name` and `publisher` before searching the `Document`.

Finally, we will verify our custom implementation through integration test

```java
@DataMongoTest
class CharacterRepositoryTests {

    @Autowired
    private CharacterRepository repository;

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
```
