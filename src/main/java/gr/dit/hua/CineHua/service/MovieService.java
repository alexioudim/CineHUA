package gr.dit.hua.CineHua.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.dit.hua.CineHua.entity.Movie;
import gr.dit.hua.CineHua.repository.MovieRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${omdb.api.key}") private String omdbApiKey;
    @Value("${omdb.api.url}") private String omdbApiUrl;
    @Value("${tmdb.api.key}") private String tmdbApiKey;
    @Value("${tmdb.api.url}") private String tmdbApiUrl;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional
    public Movie createFromImdb(String imdbId) throws Exception {
        // OMDb
        String omdbUrl = omdbApiUrl + "/?i=" + imdbId + "&apikey=" + omdbApiKey;
        String omdbResponse = restTemplate.getForObject(omdbUrl, String.class);
        Movie m = mapper.readValue(omdbResponse, Movie.class);

        // TMDb poster
        String tmdbFindUrl = tmdbApiUrl + "/find/" + imdbId +
                "?api_key=" + tmdbApiKey + "&external_source=imdb_id";
        String tmdbResponse = restTemplate.getForObject(tmdbFindUrl, String.class);
        JsonNode root = mapper.readTree(tmdbResponse);
        JsonNode results = root.path("movie_results");
        if (results.isArray() && !results.isEmpty()) {
            String posterPath = results.get(0).path("poster_path").asText();
            if (posterPath != null && !posterPath.isEmpty()) {
                m.setImage_url("https://image.tmdb.org/t/p/w500" + posterPath);
            }
        }

        // Persist
        return movieRepository.save(m);
    }

    @Transactional
    public Movie findById(long id) {
        return movieRepository.findById(id);
    }

    @Transactional
    public List<Movie> listAll() {
        return movieRepository.findAll();
    }

    @Transactional
    public List<Movie> listPublic() {
        return movieRepository.findByIsPublicTrue();
    }

    @Transactional
    public void deleteMovie(long id) {
        Movie movie = movieRepository.findById(id);
        movieRepository.delete(movie);
    }

    @Transactional
    public void changeVisibility(long id) {
        Movie movie = movieRepository.findById(id);
        movie.setPublic(!movie.isPublic());
        movieRepository.save(movie);
    }
}
