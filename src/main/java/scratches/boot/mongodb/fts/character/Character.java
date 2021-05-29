package scratches.boot.mongodb.fts.character;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Rashidi Zin
 */
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
