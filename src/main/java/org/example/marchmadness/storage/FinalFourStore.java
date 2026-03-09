package org.example.marchmadness.storage;

import org.example.marchmadness.models.FinalFour;

public class FinalFourStore {
    private FinalFour finalFour;

    public void save(FinalFour finalFour) {
        this.finalFour = finalFour;
    }

    public FinalFour get() {
        return finalFour;
    }
}
