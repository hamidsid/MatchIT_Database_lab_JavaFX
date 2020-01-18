package lab3.datamodel;

import java.sql.*;
import java.util.ArrayList;

/**
 * Database is a class that specifies the interface to the
 * movie database. Uses JDBC and the MySQL Connector/J driver.
 */
public class Database {
    /**
     * The database connection.
     */
    private Connection conn;


    /**
     * Create the database interface object. Connection to the database
     * is performed later.
     */
    public Database() {
        conn = null;
    }

    /**
     * Open a connection to the database, using the specified user name
     * and password.
     *
     * @param userName The user name.
     * @param password The user's password.
     * @return true if the connection succeeded, false if the supplied
     * user name and password were not recognized. Returns false also
     * if the JDBC driver isn't found.
     */
    public boolean openConnection(String userName, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/lab3_db", userName, password);
        } catch (SQLException e) {
            System.err.println(e);
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            System.err.println(e);
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Close the connection to the database.
     */
    public void closeConnection() {
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        conn = null;
        System.err.println("Database connection closed.");
    }

    /**
     * Check if the connection to the database has been established
     *
     * @return true if the connection has been established
     */
    public boolean isConnected() {
        return conn != null;
    }

    public Show getShowData(String mTitle, String mDate) {
        Integer mFreeSeats = 0;
        String mVenue = "";
        int capacity = 0;
        try {
            String movie = "Select free_seats, theater_name, totall_seats " +
                            "From Movies " +
                            "LEFT JOIN Theaters " +
                            "USING(theater_name)" +
                            "Where movie_name = ? " +
                            "AND movie_date = ?";
            PreparedStatement movieStmt = conn.prepareStatement(movie);
            movieStmt.setString(1, mTitle);
            movieStmt.setString(2, mDate);
            ResultSet movies = movieStmt.executeQuery();
            while (movies.next()) {
                mFreeSeats = movies.getInt("free_seats");
                mVenue = movies.getString("theater_name");
                capacity = movies.getInt("totall_seats");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        /* --- TODO: add code for database query --- */

        return new Show(mTitle, mDate, mVenue, mFreeSeats + "/" + capacity);
    }

    /* --- TODO: insert more own code here --- */

    public boolean login(String username) {
        try {
            PreparedStatement usersStmt0 = conn.prepareStatement("Select username From Users where username = ?");
            usersStmt0.setString(1, username);
            ResultSet users1 = usersStmt0.executeQuery();
            int count = 0;
            while (users1.next()) {
                System.out.println(users1.getString("username"));
                count++;
            }
            System.out.println(count);

            String getUsers = "Select * From Users";
            PreparedStatement usersStmt = conn.prepareStatement(getUsers);
            ResultSet users = usersStmt.executeQuery();
            while (users.next()) {
                if (users.getString("username").equals(username)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public ArrayList<String> getMoviesName() {
        ArrayList<String> tmp = new ArrayList<>();
        try {
            String getMovies = "Select movie_name From Movies Group BY movie_name";
            PreparedStatement moviesStmt = conn.prepareStatement(getMovies);
            ResultSet movie = moviesStmt.executeQuery();
            while (movie.next()) {
                tmp.add(movie.getString("movie_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public ArrayList<String> getMovieDates(String name) {
        ArrayList<String> tmp = new ArrayList<>();
        try {
            String getMoviesDate = "Select movie_date From Movies where movie_name = ?";
            PreparedStatement moviesStmt = conn.prepareStatement(getMoviesDate);
            moviesStmt.setString(1, name);
            ResultSet movie = moviesStmt.executeQuery();
            while (movie.next()) {
                tmp.add(movie.getString("movie_date"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public void reservation(String username, String movie_name, String movie_date) {
        try {
            conn.setAutoCommit(false);
            int nrOfSeats = -1;
            String getFreeSeatsNr = "Select free_seats From Movies Where movie_name = ? AND movie_date = ?";
            PreparedStatement freeSeatStmt = conn.prepareStatement(getFreeSeatsNr);
            freeSeatStmt.setString(1, movie_name);
            freeSeatStmt.setString(2, movie_date);
            ResultSet freeSeat = freeSeatStmt.executeQuery();
            while (freeSeat.next()) {
                nrOfSeats = freeSeat.getInt("free_seats");
            }

            if (nrOfSeats > 0) {
                String updateSeat = "UPDATE Movies SET free_seats = free_seats-1 WHERE movie_name = ? AND movie_date = ?";
                PreparedStatement ps = conn.prepareStatement(updateSeat);
                ps.setString(1, movie_name);
                ps.setString(2, movie_date);
                ps.executeUpdate();

                String reservation = "insert into Reserves(username, movie_name, movie_date) values(?, ?, ?)";
                PreparedStatement reservationStmt = conn.prepareStatement(reservation);
                reservationStmt.setString(1, username);
                reservationStmt.setString(2, movie_name);
                reservationStmt.setString(3, movie_date);
                reservationStmt.executeUpdate();
            } else {
                System.err.println("No more free Seats!!!");
            }
            conn.setAutoCommit(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int reservationNr(String username, String movie_name, String movie_date) {
        int nr = -1;
        try {
            String reservationNbr = "Select res_nr From Reserves Where username = ? AND movie_name = ? AND movie_date = ?";
            PreparedStatement reserveNr = conn.prepareStatement(reservationNbr);
            reserveNr.setString(1, username);
            reserveNr.setString(2, movie_name);
            reserveNr.setString(3, movie_date);
            ResultSet resNr = reserveNr.executeQuery();
            while (resNr.next()) {
                nr = resNr.getInt("res_nr");
            }
            return nr;
        } catch (Exception e) {
            e.printStackTrace();
            return nr;
        }
    }

}
