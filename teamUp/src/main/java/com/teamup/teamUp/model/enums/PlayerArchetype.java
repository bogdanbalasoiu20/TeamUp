package com.teamup.teamUp.model.enums;

public enum PlayerArchetype {
    //atacanti
    TARGET_MAN,       // Puternic, joc de cap
    SPEEDSTER,        // Viteza, dribling
    POACHER,          // Finalizator bun

    //mijlocasi
    PLAYMAKER,        // Pase, viziune
    BOX_TO_BOX,       // Efort mare, echilibrat
    DESTROYER,        // Defensiv, recuperator

    //fundasi
    BALL_PLAYING_CB,  // Iese cu mingea la picior
    STOPPER,          // Clasic, dur, degajeaza
    WING_BACK,        // Fundas lateral ofensiv

    //portari
    SWEEPER_KEEPER,   // Participa la joc
    CLASSIC_GK,       // Sta pe linie

    //default
    BALANCED          // Cand stats-urile sunt echilibrate si nu iese nimic în evidenta
}
