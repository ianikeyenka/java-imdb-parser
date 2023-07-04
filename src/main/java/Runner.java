import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Runner {
    public static void main(String[] args) throws IOException, InterruptedException {
        try (Scanner sc = new Scanner(System.in)) {
            // user input surnames
            List<Actor> actors = new ArrayList<>();
            System.out.println("Enter the surname of the first actor!");
            String firstUserInput = sc.next();
            addActor(firstUserInput, actors, sc);
            System.out.println("Enter the surname of the second actor!");
            String secondUserInput = sc.next();
            addActor(secondUserInput, actors, sc);

            for (Actor actor : actors) {
                addFilms(actor);
            }
            // check that the actors list is not empty
            if (actors.size() != Constants.ZERO) {
                List<String> result = actors.get(0).getFilmsURL();
                for (Actor actor : actors.subList(1, actors.size())) {
                    result.retainAll(actor.getFilmsURL());
                }
                // check if there are common movies
                if (result.size() != Constants.ZERO) {
                    System.out.println(Constants.LINE_SEPARATOR);
                    result.forEach(System.out::println);
                } else {
                    System.out.println(Constants.MESSAGE_NO_COMMON_FILMS);
                }
            } else {
                System.out.println(Constants.MESSAGE_NO_COMMON_FILMS);
            }
        }
    }

    private static void addActor(String userInput, List<Actor> actors, Scanner sc) throws IOException {
        Document doc = Jsoup.connect(Constants.FIRST_PART_FIND_URL + userInput + Constants.LAST_PART_FIND_URL).get();
        // get the list of actors from the page
        Elements actorsLinks = doc.select("[class=ipc-metadata-list-summary-item__t]");
        boolean isFound = false;
        for (Element actor : actorsLinks) {
            String actorName = actor.text();
            // check if the actor name contains the user input
            if (actorName.contains(userInput)) {
                System.out.printf(Constants.QUESTION_MESSAGE_ACTOR_FOUND,
                        Constants.BASE_URL + actor.attr("href"));
                String userAnswer;
                do {
                    userAnswer = sc.next();
                    if (!userAnswer.equals(Constants.YES) && !userAnswer.equals(Constants.NO)) {
                        System.out.println(Constants.ERROR_MESSAGE_INVALID_INPUT);
                    }
                } while (!userAnswer.equals(Constants.YES) && !userAnswer.equals(Constants.NO));
                if (userAnswer.equals(Constants.YES)) {
                    // add the actor to the list of actors
                    isFound = true;
                    actors.add(new Actor(Constants.BASE_URL + actor.attr("href")));
                    break;
                }
            }
        }
        if (!isFound) {
            System.out.printf(Constants.ERROR_MESSAGE_NO_ACTOR_FOUND + userInput);
        }
    }

    private static void addFilms(Actor actor) throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "selenium\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.get(actor.getActorURL());
        // find the button to expand the list of movies
        WebElement element = driver.findElement(By.cssSelector("button[data-testid='nm-flmg-all-accordion-expander']"));
        // scroll to the button to make it clickable
        int elementPosition = element.getLocation().getY();
        String js = String.format("window.scroll(0, %s)", elementPosition);
        ((JavascriptExecutor) driver).executeScript(js);
        Thread.sleep(1000);
        element.click();
        // parse the page source to get the list of movies
        Thread.sleep(1000);
        String pageSource = driver.getPageSource();
        Document doc = Jsoup.parse(pageSource);
        List<String> films = new ArrayList<>();
        Elements filmsLink = doc.select("[data-testid^=nm_flmg_credit_actor_]").select("[class=ipc-lockup-overlay ipc-focusable]");
        for (Element filmLink : filmsLink) {
            // add the movie URL to the list of movies
            films.add(Constants.BASE_URL + filmLink.attr("href").split("\\?")[Constants.ZERO]);
        }
        actor.setFilmsURL(films);
        driver.close();
    }
}
