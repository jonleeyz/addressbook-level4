package systemtests;

import static guitests.guihandles.WebViewUtil.waitUntilBrowserLoaded;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static seedu.address.ui.BrowserPanel.DEFAULT_PAGE;
import static seedu.address.ui.StatusBarFooter.SYNC_STATUS_INITIAL;
import static seedu.address.ui.StatusBarFooter.SYNC_STATUS_UPDATED;
import static seedu.address.ui.UiPart.FXML_FILE_FOLDER;
import static seedu.address.ui.testutil.GuiTestAssert.assertListMatching;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;

import guitests.GuiRobot;
import guitests.guihandles.BrowserPanelHandle;
import guitests.guihandles.CommandBoxHandle;
import guitests.guihandles.MainMenuHandle;
import guitests.guihandles.MainWindowHandle;
import guitests.guihandles.PersonListPanelHandle;
import guitests.guihandles.ResultDisplayHandle;
import guitests.guihandles.StatusBarFooterHandle;
import javafx.scene.input.KeyCode;
import seedu.address.MainApp;
import seedu.address.TestApp;
import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.SelectCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.testutil.TypicalPersons;
import seedu.address.ui.CommandBox;
import seedu.address.ui.ResultDisplay;
import seedu.address.ui.testutil.EventsCollectorRule;

/**
 * A system test class for AddressBook, which provides access to handles of GUI components and helper methods
 * for test verification.
 */
public abstract class AddressBookSystemTest {
    @ClassRule
    public static ClockRule clockRule = new ClockRule();

    private static final List<String> COMMAND_BOX_DEFAULT_STYLE = Arrays.asList("text-input", "text-field");
    private static final List<String> COMMAND_BOX_ERROR_STYLE =
            Arrays.asList("text-input", "text-field", CommandBox.ERROR_STYLE_CLASS);

    @Rule
    public final EventsCollectorRule eventsCollectorRule = new EventsCollectorRule();

    protected final GuiRobot guiRobot = new GuiRobot();

    private List<String> defaultStyleOfResultDisplay;
    private List<String> errorStyleOfResultDisplay;

    private MainWindowHandle mainWindowHandle;
    private TestApp testApp;
    private SystemTestSetupHelper setupHelper;

    @BeforeClass
    public static void setupBeforeClass() {
        SystemTestSetupHelper.initialize();
    }

    @Before
    public void setUp() {
        setupHelper = new SystemTestSetupHelper();
        testApp = setupHelper.setupApplication(this::getInitialData, getDataFileLocation());
        mainWindowHandle = setupHelper.setupMainWindowHandle();

        defaultStyleOfResultDisplay = mainWindowHandle.getResultDisplay().getStyleClass();

        errorStyleOfResultDisplay = mainWindowHandle.getResultDisplay().getStyleClass();
        errorStyleOfResultDisplay.add(ResultDisplay.ERROR_STYLE_CLASS);

        waitUntilBrowserLoaded(getBrowserPanel());
        assertApplicationStartingStateIsCorrect();
    }

    @After
    public void tearDown() throws Exception {
        setupHelper.tearDownStage();
        EventsCenter.clearSubscribers();
    }

    /**
     * Returns the data to be loaded into the file in {@link #getDataFileLocation()}.
     */
    protected AddressBook getInitialData() {
        return TypicalPersons.getTypicalAddressBook();
    }

    /**
     * Returns the directory of the data file.
     */
    protected String getDataFileLocation() {
        return TestApp.SAVE_LOCATION_FOR_TESTING;
    }

    public MainWindowHandle getMainWindowHandle() {
        return mainWindowHandle;
    }

    public CommandBoxHandle getCommandBox() {
        return mainWindowHandle.getCommandBox();
    }

    public PersonListPanelHandle getPersonListPanel() {
        return mainWindowHandle.getPersonListPanel();
    }

    public MainMenuHandle getMainMenu() {
        return mainWindowHandle.getMainMenu();
    }

    public BrowserPanelHandle getBrowserPanel() {
        return mainWindowHandle.getBrowserPanel();
    }

    public StatusBarFooterHandle getStatusBarFooter() {
        return mainWindowHandle.getStatusBarFooter();
    }

    public ResultDisplayHandle getResultDisplay() {
        return mainWindowHandle.getResultDisplay();
    }

    /**
     * Executes {@code command} in the application's {@code CommandBox}.
     * Method returns after UI components have been updated.
     */
    protected void executeCommand(String command) {
        rememberStates();
        // Injects a fixed clock before executing a command so that the time stamp shown in the status bar
        // after each command is predictable and also different from the previous command.
        clockRule.setInjectedClockToCurrentTime();

        mainWindowHandle.getCommandBox().run(command);

        waitUntilBrowserLoaded(getBrowserPanel());
    }

    //@@author jonleeyz
    /**
     * Executes {@code command} associated with the given keyboard shortcut.
     * Method returns after UI components have been updated.
     */
    protected void executeUsingAccelerator(KeyCode... combination) {
        rememberStates();
        // Injects a fixed clock before executing a command so that the time stamp shown in the status bar
        // after each command is predictable and also different from the previous command.
        clockRule.setInjectedClockToCurrentTime();

        mainWindowHandle.getMainMenu().useAccelerator(combination);

        waitUntilBrowserLoaded(getBrowserPanel());
    }

    /**
     * Populates the appropriate {@code command} template in the application's
     * {@code CommandBox} given a keyboard shortcut.
     */
    protected void populateUsingAccelerator(KeyCode... combination) {
        mainWindowHandle.getMainMenu().useAccelerator(combination);
    }

    /**
     * Executes {@code command} associated with the given menu item.
     * Method returns after UI components have been updated.
     */
    protected void executeUsingMenuItem(String... menuItems) {
        rememberStates();
        // Injects a fixed clock before executing a command so that the time stamp shown in the status bar
        // after each command is predictable and also different from the previous command.
        clockRule.setInjectedClockToCurrentTime();

        mainWindowHandle.getMainMenu().clickOnMenuItemsSequentially(menuItems);

        waitUntilBrowserLoaded(getBrowserPanel());
    }

    /**
     * Populates the appropriate {@code command} template in the application's
     * {@code CommandBox} given the appropriate menu item.
     */
    protected void populateUsingMenu(String... menuItems) {
        mainWindowHandle.getMainMenu().clickOnMenuItemsSequentially(menuItems);
    }
    //@@author

    /**
     * Displays all persons in the address book.
     */
    protected void showAllPersons() {
        executeCommand(ListCommand.COMMAND_WORD);
        assertEquals(getModel().getAddressBook().getPersonList().size(), getModel().getFilteredPersonList().size());
    }

    /**
     * Displays all persons with any parts of their names matching {@code keyword} (case-insensitive).
     */
    protected void showPersonsWithName(String keyword) {
        executeCommand(FindCommand.COMMAND_WORD + " " + keyword);
        assertTrue(getModel().getFilteredPersonList().size() < getModel().getAddressBook().getPersonList().size());
    }

    /**
     * Selects the person at {@code index} of the displayed list.
     */
    protected void selectPerson(Index index) {
        executeCommand(SelectCommand.COMMAND_WORD + " " + index.getOneBased());
        assertEquals(index.getZeroBased(), getPersonListPanel().getSelectedCardIndex());
    }

    /**
     * Deletes all persons in the address book.
     */
    protected void deleteAllPersons() {
        executeCommand(ClearCommand.COMMAND_WORD);
        assertEquals(0, getModel().getAddressBook().getPersonList().size());
    }

    /**
     * Asserts that the {@code CommandBox} displays {@code expectedCommandInput}, the {@code ResultDisplay} displays
     * {@code expectedResultMessage}, the model and storage contains the same person objects as {@code expectedModel}
     * and the person list panel displays the persons in the model correctly.
     */
    protected void assertApplicationDisplaysExpected(String expectedCommandInput, String expectedResultMessage,
            Model expectedModel) {
        assertEquals(expectedCommandInput, getCommandBox().getInput());
        assertEquals(expectedResultMessage, getResultDisplay().getText());
        assertEquals(expectedModel, getModel());
        //erroneous expected test output -- assertEquals(expectedModel.getAddressBook(), testApp.readStorageAddressBook
        // ());
        assertListMatching(getPersonListPanel(), expectedModel.getFilteredPersonList());
    }

    /**
     * Calls {@code BrowserPanelHandle}, {@code PersonListPanelHandle} and {@code StatusBarFooterHandle} to remember
     * their current state.
     */
    private void rememberStates() {
        StatusBarFooterHandle statusBarFooterHandle = getStatusBarFooter();
        getBrowserPanel().rememberUrl();
        statusBarFooterHandle.rememberSaveLocation();
        statusBarFooterHandle.rememberSyncStatus();
        getPersonListPanel().rememberSelectedPersonCard();
    }

    /**
     * Asserts that the previously selected card is now deselected and the browser's url remains displaying the details
     * of the previously selected person.
     * @see BrowserPanelHandle#isUrlChanged()
     */
    protected void assertSelectedCardDeselected() {
        assertFalse(getBrowserPanel().isUrlChanged());
        assertFalse(getPersonListPanel().isAnyCardSelected());
    }

    /**
     * Asserts that the browser's url is changed to display the details of the person in the person list panel at
     * {@code expectedSelectedCardIndex}, and only the card at {@code expectedSelectedCardIndex} is selected.
     * @see BrowserPanelHandle#isUrlChanged()
     * @see PersonListPanelHandle#isSelectedPersonCardChanged()
     */
    protected void assertSelectedCardChanged(Index expectedSelectedCardIndex) {
        String selectedCardName = getPersonListPanel().getHandleToSelectedCard().getName();
        assertEquals(expectedSelectedCardIndex.getZeroBased(), getPersonListPanel().getSelectedCardIndex());
    }

    /**
     * Asserts that the browser's url and the selected card in the person list panel remain unchanged.
     * @see BrowserPanelHandle#isUrlChanged()
     * @see PersonListPanelHandle#isSelectedPersonCardChanged()
     */
    protected void assertSelectedCardUnchanged() {
        assertFalse(getBrowserPanel().isUrlChanged());
        assertFalse(getPersonListPanel().isSelectedPersonCardChanged());
    }

    /**
     * Asserts that the command box and result display show the default style.
     */
    protected void assertCommandBoxAndResultDisplayShowsDefaultStyle() {
        assertEquals(COMMAND_BOX_DEFAULT_STYLE, getCommandBox().getStyleClass());
        assertEquals(defaultStyleOfResultDisplay, getResultDisplay().getStyleClass());
    }

    /**
     * Asserts that the command box and result display show the error style.
     */
    protected void assertCommandBoxAndResultDisplayShowsErrorStyle() {
        assertEquals(COMMAND_BOX_ERROR_STYLE, getCommandBox().getStyleClass());
        assertEquals(errorStyleOfResultDisplay, getResultDisplay().getStyleClass());
    }

    /**
     * Asserts that the entire status bar remains the same.
     */
    protected void assertStatusBarUnchanged() {
        StatusBarFooterHandle handle = getStatusBarFooter();
        assertFalse(handle.isSaveLocationChanged());
        assertFalse(handle.isSyncStatusChanged());
    }

    /**
     * Asserts that only the sync status in the status bar was changed to the timing of
     * {@code ClockRule#getInjectedClock()}, while the save location remains the same.
     */
    protected void assertStatusBarUnchangedExceptSyncStatus() {
        StatusBarFooterHandle handle = getStatusBarFooter();
        String timestamp = new Date(clockRule.getInjectedClock().millis()).toString();
        String expectedSyncStatus = String.format(SYNC_STATUS_UPDATED, timestamp);
        assertEquals(expectedSyncStatus, handle.getSyncStatus());
        assertFalse(handle.isSaveLocationChanged());
    }

    /**
     * Asserts that the starting state of the application is correct.
     */
    private void assertApplicationStartingStateIsCorrect() {
        try {
            assertEquals("", getCommandBox().getInput());
            assertEquals("", getResultDisplay().getText());
            assertListMatching(getPersonListPanel(), getModel().getFilteredPersonList());
            assertEquals(MainApp.class.getResource(FXML_FILE_FOLDER + DEFAULT_PAGE), getBrowserPanel().getLoadedUrl());
            assertEquals("./" + testApp.getStorageSaveLocation(), getStatusBarFooter().getSaveLocation());
            assertEquals(SYNC_STATUS_INITIAL, getStatusBarFooter().getSyncStatus());
        } catch (Exception e) {
            throw new AssertionError("Starting state is wrong.", e);
        }
    }

    /**
     * Returns a defensive copy of the current model.
     */
    protected Model getModel() {
        return testApp.getModel();
    }
}
