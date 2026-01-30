package com.teamup.teamUp.chemistry;

//masoara cat de asemenatori sunt 2 jucatori
public class CosineSimilarity {
    //primeste ca arg vectorii cu valori normalizate din PlayerVectorBuilder
    public static double compute(double[] a, double[] b) {
        double dot = 0;  //produsul scalar

        //lungimea vectorilor
        double normA = 0;
        double normB = 0;

        for(int i=0; i<a.length; i++){
            dot += a[i]*b[i];
            normA += a[i]*a[i];
            normB += b[i]*b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        //formula finala
        return dot/(Math.sqrt(normA) * Math.sqrt(normB));
    }
}
