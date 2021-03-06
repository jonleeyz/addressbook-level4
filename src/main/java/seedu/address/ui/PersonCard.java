package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * An UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    //@@author jonleeyz-reused
    private static final String[] TAG_COLOUR_STYLES =
        {"teal", "red", "yellow", "blue", "orange", "brown", "green", "pink", "black", "grey"};
    //@@author
    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private Label personType;
    @FXML
    private FlowPane tags;

    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        personType.setText(person.getType().name());
        initTags(person);
    }

    //@@author jonleeyz-reused
    // given a tagName, returns the String representation of a colour style
    private String getTagColourStyleFor(String tagName) {
        // hash code of tag name used to generate random colour
        // colour of tags changes between different runs of the application
        // might want to tweak this behaviour in the LoanShark Tycoon context
        return TAG_COLOUR_STYLES[Math.abs(tagName.hashCode()) % TAG_COLOUR_STYLES.length];
    }

    /**
     * Creates the Labels fot a given {@code Person}.
     * 1. Creates a new Label object for each tag, initialised with the respective tag.
     * 2. Adds a style colour attribute to each Label based on its tag.
     * 3. Adds each properly initialised Label to the containing FlowPane object.
     */
    private void initTags(Person person) {
        person.getTags().forEach(tag -> {
            Label tagLabel = new Label(tag.tagName);
            tagLabel.getStyleClass().add(getTagColourStyleFor(tag.tagName));    // getStyleClass(): Node class method
            tags.getChildren().add(tagLabel);
        });
    }
    //@@author

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof PersonCard)) {
            return false;
        }

        // state check
        PersonCard card = (PersonCard) other;
        return id.getText().equals(card.id.getText())
                && person.equals(card.person);
    }
}
