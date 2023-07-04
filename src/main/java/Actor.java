import java.util.List;

public class Actor {
    private String actorURL;
    private List<String> filmsURL;

    public Actor(String actorURL, List<String> filmsURL) {
        this.actorURL = actorURL;
        this.filmsURL = filmsURL;
    }

    public Actor(String actorURL) {
        this.actorURL = actorURL;
        filmsURL = List.of("");
    }

    public String getActorURL() {
        return actorURL;
    }

    public void setActorURL(String actorURL) {
        this.actorURL = actorURL;
    }

    public List<String> getFilmsURL() {
        return filmsURL;
    }

    public void setFilmsURL(List<String> filmsURL) {
        this.filmsURL = filmsURL;
    }

    @Override
    public String toString() {
        return actorURL + "\n" + String.join("\n", filmsURL);
    }
}
