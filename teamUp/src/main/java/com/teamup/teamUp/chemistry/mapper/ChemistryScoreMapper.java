package com.teamup.teamUp.chemistry.mapper;

import org.springframework.stereotype.Component;

@Component
public class ChemistryScoreMapper {

    //transforma scorul din zecimal in intreg( 0.73 -> 73)
    //ceea ce va vedea userul
    public int toScore(double adjustedSimilarity){
        return (int) Math.round(adjustedSimilarity*100);
    }

    public String label(int score) {
        if (score >= 80) return "Great chemistry";
        if (score >= 60) return "Good chemistry";
        if (score >= 40) return "Average chemistry";
        return "Poor chemistry";
    }
}
