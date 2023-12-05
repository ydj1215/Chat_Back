package com.sample.chat.entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import javax.persistence.*;

@Entity
@Table(name = "movie")
@Getter @Setter @ToString
@NoArgsConstructor
public class Movie {
    @Id
    @Column(name = "movie_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String movieRank;
    private String image;
    private String title;
    private String score;
    private String rate;
    private String reservation;
    private String date;
}
