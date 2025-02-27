import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Song {

    private String trackname;
    private String artist;
    private LocalDateTime date;
    private long ms_played;

    public Song(String trackname, String artist, String dateTime, DateTimeFormatter dtf) {
        this.trackname = trackname;
        this.artist = artist;
        this.date = LocalDateTime.parse(dateTime, dtf);
        this.ms_played = 0;
    }

    public long getMs_played() { return ms_played; }

    public void setMs_played(long ms_played) { this.ms_played = ms_played; }

    public String getTrackname()
    {
        return trackname;
    }

    public void setTrackname(String trackname)
    {
        this.trackname = trackname;
    }

    public String getArtist()
    {
        return artist;
    }

    public void setArtist(String artist)
    {
        this.artist = artist;
    }

    public LocalDateTime getDate()
    {
        return date;
    }

    public void setDate(LocalDateTime date)
    {
        this.date = date;
    }

    public String printSong() {
        String printSong = "";
        printSong += "{\n";
        printSong += "Track Name: " + this.trackname + "\n";
        printSong += "Artist Name: " + this.artist + "\n";
        printSong += "Date and Time of Playing: " + this.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n";
        printSong += "Time Played: " + this.ms_played + "\n";
        printSong += "}\n";

        return printSong;
    }

    public void increaseMillisecondsPlayed(long ms) { this.ms_played += ms; }

    @Override
    public boolean equals(Object o) {
        Song s = (Song) o;

        if(this.trackname.equals(s.getTrackname()) && this.artist.equals(s.getArtist())) { return true; }

        else return false;
    }

}
