package com.sample.chat.controller;

import com.sample.chat.dto.MovieDto;
import com.sample.chat.entity.Movie;
import com.sample.chat.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.sample.chat.utils.Common.CORS_ORIGIN;

@Slf4j
@CrossOrigin(origins = CORS_ORIGIN)
@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieService movieService;

    @Autowired
    MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // 응답은 스프링 부트가 요청을 한 다른 웹 서비스(파이썬, 리액트 등)에게 자동으로 보내준다.
    @PostMapping("/insert")
    public ResponseEntity<Boolean> movieInsert(@RequestBody List<Map<String, String>> movieList) {
        log.info("movieList : {}", movieList);

        for (Map<String, String> data : movieList) {
            Movie movie = new Movie();
            movie.setMovieRank(data.get("rank"));
            movie.setImage(data.get("image"));
            movie.setTitle(data.get("title"));
            movie.setScore(data.get("score"));
            movie.setRate(data.get("eval_num"));
            movie.setReservation(data.get("reservation"));
            movie.setDate(data.get("open_date"));
            movieService.saveMovie(movie);
        }
        return ResponseEntity.ok(true);
    }

    // 영화 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<MovieDto>> movieList() {
        List<MovieDto> list = movieService.getMovieList();
        return ResponseEntity.ok(list);
    }

    // 페이지네이션
    @GetMapping("/list/page")
    public ResponseEntity<List<MovieDto>> movieList(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        List<MovieDto> list = movieService.getMovieList(page, size);
        log.info("list : {}", list);
        return ResponseEntity.ok(list);
    }

    // 페이지 수 조회
    @GetMapping("/list/count")
    public ResponseEntity<Integer> movieCount(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        int pageCnt = movieService.getMoviePage(pageRequest);
        return ResponseEntity.ok(pageCnt);
    }

    // 영화 전체 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<Boolean> movieDelete() {
        movieService.deleteAll();
        return ResponseEntity.ok(true);
    }
}
