package sk.kubena.fakenews.rating;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class RatingDTO {

    @NotNull
    @NotEmpty
    private int rating1;

    @NotNull
    @NotEmpty
    private int rating2;

    @NotNull
    @NotEmpty
    private int rating3;

    @NotNull
    @NotEmpty
    private int rating4;

    public RatingDTO(@NotNull @NotEmpty int rating1, @NotNull @NotEmpty int rating2, @NotNull @NotEmpty int rating3, @NotNull @NotEmpty int rating4) {
        this.rating1 = rating1;
        this.rating2 = rating2;
        this.rating3 = rating3;
        this.rating4 = rating4;
    }

    public int getRating1() {
        return rating1;
    }

    public void setRating1(int rating1) {
        this.rating1 = rating1;
    }

    public int getRating2() {
        return rating2;
    }

    public void setRating2(int rating2) {
        this.rating2 = rating2;
    }

    public int getRating3() {
        return rating3;
    }

    public void setRating3(int rating3) {
        this.rating3 = rating3;
    }

    public int getRating4() {
        return rating4;
    }

    public void setRating4(int rating4) {
        this.rating4 = rating4;
    }
}
