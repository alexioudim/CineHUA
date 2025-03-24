package gr.dit.hua.CineHua.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AuditoriumRequest {

    @NotBlank
    private String name;

    @NotNull
    private int rows;

    @NotNull
    private int columns;

    public AuditoriumRequest() {
    }

    public @NotBlank String getName() {
        return name;
    }

    public void setName(@NotBlank String name) {
        this.name = name;
    }

    @NotNull
    public int getRows() {
        return rows;
    }

    public void setRows(@NotNull int rows) {
        this.rows = rows;
    }

    @NotNull
    public int getColumns() {
        return columns;
    }

    public void setColumns(@NotNull int columns) {
        this.columns = columns;
    }
}
