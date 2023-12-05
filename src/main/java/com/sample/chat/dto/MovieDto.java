package com.sample.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MovieDto {
    private String rank;
    private String image;
    private String title;
    private String score;
    private String rate;
    private String reservation;
    private String date;
}