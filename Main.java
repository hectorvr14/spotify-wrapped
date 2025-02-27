import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {

    // Date format on the files
    private static DateTimeFormatter dtf;

    // Start and end date variables - to be introduced by the user
    private static LocalDateTime start, end;

    public static void main(String[] args) {

        // JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        // File reader to get the data from
        FileReader reader;

        // List of played tracks gotten from the data
        ArrayList<Song> tracks = new ArrayList<>();

        // Hashmap with the unique songs, and the number of times each has been played
        HashMap<Song, Integer> songs = new HashMap<>();

        // Keyset of the hashmap
        Set<Song> keys;

        // Flag to indicate if a song already is in the hashmap
        boolean alreadyThere;

        /*------------------- Specify time period -------------------*/

        // To read from keyboard input
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);

        // Start date
        System.out.println("From (dd/mm/yyyy): ");

        try {
            LocalDate start_date = LocalDate.parse(br.readLine(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            start = start_date.atStartOfDay();
        } catch (IOException ioException) {
            ioException.getMessage();
            System.exit(-1);
        }

        // End date
        System.out.println("Until (dd/mm/yyyy): ");

        try {
            LocalDate end_date = LocalDate.parse(br.readLine(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            end = end_date.atTime(LocalTime.MAX);
        } catch (IOException ioException) {
            ioException.getMessage();
            System.exit(-1);
        }

        /*------------------- Reading the input -------------------*/

        // We need at least two files - input and output, and the kind of data that was collected from Spotify
        if(args.length < 3) {
            System.out.println("ERROR: At least 3 arguments are needed: \n" +
                                "- File mode (0 or 1): either full streaming history or last year\n" +
                                "- Input files: list of streaming data files\n" +
                                "- Output file: name of the file to write the data on\n" +
                                "\n Style: java -jar spotify <0 or 1> <input files> <output file>");
            System.exit(-1);
        }

        // Check if filemode has an admitted value
        if(!args[0].equals("0") && !args[0].equals("1")) {
            System.out.println("ERROR: filemode must be either 0 or 1");
            System.exit(-1);
        }

        // Cast to int
        int file_mode = Integer.parseInt(args[0]);

        // Set datetime mode depending on the file mode
        if(file_mode == 0) {
            dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        }
        else{
            dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        }

        // Gathering data from the input files
        for(int i = 1; i < args.length - 1; i++) {
            try {
                // Attempt to read the file
                reader = new FileReader(args[i]);

                // Get the JSON data
                Object obj = jsonParser.parse(reader);

                // Parse it to JSON array
                JSONArray songList = (JSONArray) obj;

                // Iterate over the array
                for(int j = 0; j < songList.size(); j++) {
                    // Parse the array item and cast to a song
                    Song s = parseSongObject((JSONObject) songList.get(j), file_mode);
                    // If we manage to gather the data, add it to the track list
                    if(s != null) {
                        tracks.add(s);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        /*------------------- Finding the unique songs -------------------*/

        // If the search was non-empty
        if(tracks.size() > 0) {

            // Add the first track to the hashmap - with one play
            songs.put(tracks.get(0), 1);

            // Process the rest of tracks
            for(int k = 1; k < tracks.size(); k++) {

                // Flag set to false
                alreadyThere = false;

                // Gather the keyset of the hashmap
                keys = songs.keySet();

                // Iterate through the already found songs
                for(Song s : keys) {
                    // If the song is already there
                    if(tracks.get(k).equals(s)) {
                        // Add another play to it
                        songs.put(s, songs.get(s) + 1);
                        // Add the playing time to the song
                        s.increaseMillisecondsPlayed(tracks.get(k).getMs_played());
                        // Set the flag as true
                        alreadyThere = true;
                        break;
                    }
                }

                // If the song has not been added yet
                if(!alreadyThere) {
                    // Get it in the hashmap
                    songs.put(tracks.get(k), 1);
                }
            }

        }
        else {
            System.out.println("Sorry! No songs have been played during this period!");
            System.exit(0);
        }

        /*------------------- Sort by playing time -------------------*/

        // Arraylist to store the songs sorted by total playing time - we first load all the songs into it
        ArrayList<Song> sortedSongs = new ArrayList<>(songs.keySet());

        // Sort the songs by playing time
        sortedSongs.sort((Song s1, Song s2) -> (Long.compare(s2.getMs_played(), s1.getMs_played())));

        /*------------------- Output generation -------------------*/

        try {
            // Writer for the output file
            FileWriter myWriter = new FileWriter(args[args.length - 1]);

            // Write each song's data into the output file
            for(int i = 0; i < sortedSongs.size(); i++) {
                myWriter.write((i+1) + " - " + sortedSongs.get(i).getTrackname() + " by " + sortedSongs.get(i).getArtist() + "\n");
                myWriter.write("Number of times this song was played: " + songs.get(sortedSongs.get(i)) + "\n");
                myWriter.write("Time of play: " + convertMillisecondsToTime(sortedSongs.get(i).getMs_played()) + "\n\n");
            }

            // Close the file
            myWriter.close();
        }
        catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // Private method to parse each track into our Song object
    private static Song parseSongObject(JSONObject song, int fileMode) {

        String track, artist, dateTime;

        // Extract the data from the JSON fields - names vary depending on the kind of data
        if(fileMode == 0) {
            track = (String) song.get("master_metadata_track_name");
            artist = (String) song.get("master_metadata_album_artist_name");
            dateTime = (String) song.get("ts");
        }
        else {
            track = (String) song.get("trackName");
            artist = (String) song.get("artistName");
            dateTime = (String) song.get("endTime");
        }

        // If there are no null values - some instances appear to have them
        if(track != null && artist != null) {
            // Construct the song object and return it
            Song s = new Song(track, artist, dateTime, dtf);
            // Check for dates to actually add it
            if(s.getDate().isAfter(start) && s.getDate().isBefore(end)) {
                if(fileMode == 0) {
                    s.setMs_played((Long) song.get("ms_played"));
                }
                else {
                    s.setMs_played((Long) song.get("msPlayed"));
                }
                return s;
            }
            else return null;
        }
        else return null;
    }

    // Private method to print the playing time in a more readable format
    private static String convertMillisecondsToTime(long milliseconds) {
        // We make use of Duration class to extract hours, minutes and seconds
        Duration duration = Duration.ofMillis(milliseconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        // Return the formatted string with the three values
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}