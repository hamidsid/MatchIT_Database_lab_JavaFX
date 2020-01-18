package lab3.gui;

import lab3.datamodel.CurrentUser;
import lab3.datamodel.Database;
import lab3.datamodel.Show;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;


public class BookingTab {
    // top context message
    @FXML
    private Text topContext;
    // bottom message
    @FXML
    private Text bookMsg;

    // table references
    @FXML
    private ListView<String> moviesList;
    @FXML
    private ListView<String> datesList;

    // show info references
    @FXML
    private Label showTitle;
    @FXML
    private Label showDate;
    @FXML
    private Label showVenue;
    @FXML
    private Label showFreeSeats;

    // booking button
    @FXML
    private Button bookTicket;

    private List<String> allmovies;


    private Database db;
    private Show crtShow = new Show();

    public void initialize() {
        System.out.println("Initializing BookingTab");

        // set up listeners for the movie list selection
        moviesList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldV, newV) -> {
                    // need to update the date list according to the selected movie
                    // update also the details on the right panel
                    String movie = newV;
                    fillDatesList(newV);
                    fillShow(movie, null);
                });

        // set up listeners for the date list selection
        datesList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldV, newV) -> {
                    // need to update the details according to the selected date
                    String movie = moviesList.getSelectionModel().getSelectedItem();
                    String date = newV;
                    fillShow(movie, date);
                });

        // set up booking button listener
        // one can either use this method (setup a handler in initialize)
        // or directly give a handler name in the fxml, as in the LoginTab class
        bookTicket.setOnAction(
                (event) -> {
                    String movie = moviesList.getSelectionModel().getSelectedItem();
                    String date = datesList.getSelectionModel().getSelectedItem();

                    db.reservation(CurrentUser.instance().getCurrentUserId(), movie, date);
                    int resNr = db.reservationNr(CurrentUser.instance().getCurrentUserId(), movie, date);
                    /* --- TODO: should attempt to book a ticket via the database --- */
                    /* --- do not forget to report booking number! --- */
                    /* --- update the displayed details (free seats) --- */
                    fillShow(movie, date);
                    report("Booked one ticket to " + movie + " on " + date);
                    report("Reservation number is = " + resNr);
                });

        report("Ready.");
    }

    // helpers
    // updates user display
    private void fillStatus(String usr) {
        if (usr.isEmpty()) topContext.setText("You must log in as a known user!");
        else topContext.setText("Currently logged in as " + usr);
    }

    private void report(String msg) {
        bookMsg.setText(msg);
    }

    public void setDatabase(Database db) {
        this.db = db;
    }

    private void fillNamesList() {
        allmovies = new ArrayList<String>(db.getMoviesName());

        // query the database via db
        /* --- TODO: replace with own code --- */
//		allmovies.add("Pulp Fiction");
//		allmovies.add("The Big Lebowski");
//		allmovies.add("Whiplash");
        /* --- END TODO --- */

        moviesList.setItems(FXCollections.observableList(allmovies));
        // remove any selection
        moviesList.getSelectionModel().clearSelection();
    }

    private void fillDatesList(String m) {
        List<String> alldates = new ArrayList<String>();
        if (m != null) {
            alldates = new ArrayList<String>(db.getMovieDates(m));
        }
        datesList.setItems(FXCollections.observableList(alldates));
        // remove any selection
        datesList.getSelectionModel().clearSelection();
    }

    private void fillShow(String movie, String date) {
        if (movie == null) // no movie selected
            crtShow = new Show();
        else if (date == null) // no date selected yet
            crtShow = new Show(movie);
        else // query the database via db
            crtShow = db.getShowData(movie, date);

        showTitle.setText(crtShow.getTitle());
        showDate.setText(crtShow.getDate());
        showVenue.setText(crtShow.getVenue());
        showFreeSeats.setText(crtShow.getSeats());
    }

    // called in case the user logged in changed
    public void userChanged() {
        fillStatus(CurrentUser.instance().getCurrentUserId());
        fillNamesList();
        fillDatesList(null);
        fillShow(null, null);
    }

}